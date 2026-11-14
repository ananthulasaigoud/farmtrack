package com.farmtrack.farmtrack.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

/** totalAmount is not sent by the client: the server calculates quantity x pricePerUnit. */
public record IncomeRequest(
        @NotNull(message = "Quantity is required")
        @DecimalMin(value = "0.01", message = "Quantity must be greater than 0")
        @Digits(integer = 10, fraction = 2, message = "Quantity can have at most 2 decimal places")
        BigDecimal quantity,

        @Size(max = 20, message = "Unit must be at most 20 characters")
        String unit,

        @NotNull(message = "Price per unit is required")
        @DecimalMin(value = "0.01", message = "Price per unit must be greater than 0")
        @Digits(integer = 10, fraction = 2, message = "Price can have at most 2 decimal places")
        BigDecimal pricePerUnit,

        @NotNull(message = "Sale date is required")
        LocalDate saleDate,

        @Size(max = 500, message = "Description must be at most 500 characters")
        String description
) {
}
