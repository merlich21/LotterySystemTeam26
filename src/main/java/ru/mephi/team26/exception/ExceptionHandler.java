package ru.mephi.team26.exception;

import io.javalin.Javalin;
import io.javalin.http.Context;

import java.util.Map;

public class ExceptionHandler {

    public void init(Javalin app) {
        app.exception(ApiException.class, this::handle);
    }

    private void handle(ApiException ex, Context ctx) {
        ctx.status(ex.getStatusCode());
        ctx.json(Map.of("error", ex.getMessage()));
    }
}
