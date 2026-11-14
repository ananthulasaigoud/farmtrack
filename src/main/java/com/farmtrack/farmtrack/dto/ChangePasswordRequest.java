package com.farmtrack.farmtrack.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
        @NotBlank(message = "Current password is required") String currentPassword,

        @NotBlank(message = "New password is required")
        @Size(min = 6, max = 72, message = "Password must be 6 to 72 characters")
        String newPassword
) {
}
