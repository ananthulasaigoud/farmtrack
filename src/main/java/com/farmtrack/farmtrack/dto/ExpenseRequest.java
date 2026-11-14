package com.farmtrack.farmtrack.dto;

import com.farmtrack.farmtrack.entity.ExpenseCategory;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ExpenseRequest(
        @NotNull(message = "Category is required")
        ExpenseCategory category,

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
        @Digits(integer = 10, fraction = 2, message = "Amount can have at most 2 decimal places")
        BigDecimal amount,

        @NotNull(message = "Expense date is required")
        LocalDate expenseDate,

        @Size(max = 500, message = "Description must be at most 500 characters")
        String description
) {
}
