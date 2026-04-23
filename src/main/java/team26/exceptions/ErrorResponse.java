package team26.exceptions;

public record ErrorResponse(
        String code,
        String message,
        int status,
        String path
) {}
