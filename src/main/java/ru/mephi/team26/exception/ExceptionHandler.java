package ru.mephi.team26.exception;

import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.HttpResponseException;

import java.util.Map;

public class ExceptionHandler {

    public void init(Javalin app) {
        app.exception(HttpResponseException.class, this::handle);
    }

    private void handle(HttpResponseException ex, Context ctx) {
        ctx.status(ex.getStatus());
        ctx.json(Map.of("error", ex.getMessage()));
    }
}
