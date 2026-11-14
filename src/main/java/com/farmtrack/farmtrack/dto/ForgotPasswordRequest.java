package com.farmtrack.farmtrack.dto;

import jakarta.validation.constraints.NotBlank;

public record ForgotPasswordRequest(
        @NotBlank(message = "Phone or email is required") String identifier
) {
}
