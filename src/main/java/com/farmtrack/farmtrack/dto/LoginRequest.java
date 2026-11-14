package com.farmtrack.farmtrack.dto;

import jakarta.validation.constraints.NotBlank;

/** identifier = phone number or email. */
public record LoginRequest(
        @NotBlank(message = "Phone or email is required") String identifier,
        @NotBlank(message = "Password is required") String password
) {
}
