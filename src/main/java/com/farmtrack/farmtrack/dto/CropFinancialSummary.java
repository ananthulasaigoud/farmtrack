package com.farmtrack.farmtrack.dto;

import com.farmtrack.farmtrack.entity.ExpenseCategory;

import java.math.BigDecimal;
import java.util.Map;

/** Profit/loss of one crop. status is PROFIT, LOSS or BREAK_EVEN. */
public record CropFinancialSummary(
        Long cropId,
        String cropName,
        String season,
        BigDecimal totalExpenses,
        BigDecimal totalIncome,
        BigDecimal profit,
        String status,
        Map<ExpenseCategory, BigDecimal> expensesByCategory
) {
}
