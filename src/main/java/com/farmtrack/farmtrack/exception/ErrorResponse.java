package com.farmtrack.farmtrack.exception;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Same JSON shape for every error, so the frontend can always read "message".
 * fieldErrors is filled only for validation errors, e.g. {"phone": "Phone number must contain 10 digits"}.
 */
public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        Map<String, String> fieldErrors
) {
}
