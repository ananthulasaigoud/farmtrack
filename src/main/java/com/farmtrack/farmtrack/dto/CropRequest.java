package com.farmtrack.farmtrack.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/** Dates use the format yyyy-MM-dd, e.g. "2026-06-15". */
public record CropRequest(
        @NotBlank(message = "Crop name is required")
        @Size(max = 100, message = "Crop name must be at most 100 characters")
        String cropName,

        @Size(max = 255, message = "Land details must be at most 255 characters")
        String landDetails,

        @Size(max = 50, message = "Season must be at most 50 characters")
        String season,

        LocalDate sowingDate,

        LocalDate expectedHarvestDate,

        @Size(max = 1000, message = "Notes must be at most 1000 characters")
        String additionalNotes
) {
}
