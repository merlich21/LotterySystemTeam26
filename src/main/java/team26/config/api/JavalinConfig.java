package team26.config;

import io.javalin.Javalin;
import team26.api.ApiRoutes;
import team26.exceptions.ApiException;
import team26.exceptions.ErrorResponse;
import team26.exceptions.UnauthorizedException;

public class JavalinConfig {

    public static Javalin createApp() {
        Javalin app = Javalin.create(cfg -> {
            cfg.http.defaultContentType = "application/json";
        });

        app.before("/api/*", ctx -> {
            String token = ctx.header("Authorization");
            if (token == null || !token.equals("Bearer secret-token")) {
                throw new UnauthorizedException("Invalid or missing token");
            }
            ctx.attribute("user", "team26-user");
        });

        app.exception(ApiException.class, (ex, ctx) -> {
            ctx.status(ex.getStatus());
            ctx.json(new ErrorResponse(
                    ex.getCode(),
                    ex.getMessage(),
                    ex.getStatus(),
                    ctx.path()
            ));
        });

        app.exception(Exception.class, (ex, ctx) -> {
            ctx.status(500);
            ctx.json(new ErrorResponse(
                    "INTERNAL_ERROR",
                    ex.getMessage(),
                    500,
                    ctx.path()
            ));
        });

        ApiRoutes.register(app);
        return app;
    }
}