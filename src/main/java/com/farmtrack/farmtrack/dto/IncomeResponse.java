package com.farmtrack.farmtrack.dto;

import com.farmtrack.farmtrack.entity.Income;

import java.math.BigDecimal;
import java.time.LocalDate;

public record IncomeResponse(Long id, BigDecimal quantity, String unit, BigDecimal pricePerUnit,
                             BigDecimal totalAmount, LocalDate saleDate, String description,
                             Long cropId, String cropName) {

    public static IncomeResponse from(Income income) {
        return new IncomeResponse(income.getId(), income.getQuantity(), income.getUnit(),
                income.getPricePerUnit(), income.getTotalAmount(), income.getSaleDate(),
                income.getDescription(), income.getCrop().getId(), income.getCrop().getCropName());
    }
}
