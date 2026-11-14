package com.farmtrack.farmtrack.dto;

import java.math.BigDecimal;

/** month is 1-12. */
public record MonthlySummary(int year, int month, String monthName, BigDecimal totalIncome,
                             BigDecimal totalExpenses, BigDecimal profit) {
}
