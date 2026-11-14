package com.farmtrack.farmtrack.dto;

import com.farmtrack.farmtrack.entity.ExpenseCategory;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/** Everything the dashboard page needs in one call. bestCrop is null when there are no crops yet. */
public record DashboardSummary(
        Long farmerId,
        long totalCrops,
        BigDecimal totalExpenses,
        BigDecimal totalIncome,
        BigDecimal profit,
        String status,
        Map<ExpenseCategory, BigDecimal> expensesByCategory,
        List<CropFinancialSummary> crops,
        CropFinancialSummary bestCrop
) {
}
