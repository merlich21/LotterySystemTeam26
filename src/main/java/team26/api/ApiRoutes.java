package team26.api;

import io.javalin.Javalin;
import team26.exceptions.UnauthorizedException;

public class ApiRoutes {

    public static void register(Javalin app) {

        app.get("/health", ctx -> ctx.json("OK"));

        app.get("/api/v1/lotteries", ctx -> {
            ctx.json(new String[]{"Daily", "Weekly", "Mega"});
        });

        app.get("/api/v1/profile", ctx -> {
            String user = ctx.attribute("user");
            ctx.json(java.util.Map.of("user", user));
        });

        app.post("/api/v1/tickets", ctx -> {
            ctx.status(201);
            ctx.json(java.util.Map.of("message", "Ticket created"));
        });
    }
}