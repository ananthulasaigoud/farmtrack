package com.farmtrack.farmtrack.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Body for creating/updating a farmer record. */
public record FarmerRequest(
        @NotBlank(message = "Name is required")
        @Size(max = 100, message = "Name must be at most 100 characters")
        String name,

        @NotBlank(message = "Phone is required")
        @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must contain 10 digits")
        String phone,

        @NotBlank(message = "Village is required")
        @Size(max = 100, message = "Village must be at most 100 characters")
        String village
) {
}
