package com.farmtrack.farmtrack.dto;

import com.farmtrack.farmtrack.entity.Expense;
import com.farmtrack.farmtrack.entity.ExpenseCategory;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ExpenseResponse(Long id, ExpenseCategory category, BigDecimal amount, LocalDate expenseDate,
                              String description, Long cropId, String cropName) {

    public static ExpenseResponse from(Expense expense) {
        return new ExpenseResponse(expense.getId(), expense.getCategory(), expense.getAmount(),
                expense.getExpenseDate(), expense.getDescription(),
                expense.getCrop().getId(), expense.getCrop().getCropName());
    }
}
