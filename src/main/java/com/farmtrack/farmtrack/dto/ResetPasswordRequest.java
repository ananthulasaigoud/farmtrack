package com.farmtrack.farmtrack.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
        @NotBlank(message = "Reset token is required") String token,

        @NotBlank(message = "New password is required")
        @Size(min = 6, max = 72, message = "Password must be 6 to 72 characters")
        String newPassword
) {
}
