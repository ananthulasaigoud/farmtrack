package com.farmtrack.farmtrack.dto;

/**
 * resetToken is only filled when app.auth.expose-reset-token=true (development),
 * because no email/SMS service is connected yet.
 */
public record ForgotPasswordResponse(String message, String resetToken) {
}
