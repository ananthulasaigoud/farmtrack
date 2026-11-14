package com.farmtrack.farmtrack.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Body of POST /api/auth/register. Creates a Farmer and a login account together. */
public record RegisterRequest(
        @NotBlank(message = "Name is required")
        @Size(max = 100, message = "Name must be at most 100 characters")
        String name,

        @NotBlank(message = "Phone is required")
        @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must contain 10 digits")
        String phone,

        @NotBlank(message = "Village is required")
        @Size(max = 100, message = "Village must be at most 100 characters")
        String village,

        @Email(message = "Enter a valid email address")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 6, max = 72, message = "Password must be 6 to 72 characters")
        String password
) {
}
