package com.farmtrack.farmtrack.dto;

import com.farmtrack.farmtrack.entity.Crop;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CropResponse(
        Long id,
        String cropName,
        String landDetails,
        String season,
        LocalDate sowingDate,
        LocalDate expectedHarvestDate,
        String additionalNotes,
        Long farmerId,
        BigDecimal totalExpenses,
        BigDecimal totalIncome,
        BigDecimal profit
) {
    public static CropResponse from(Crop crop) {
        return new CropResponse(
                crop.getId(),
                crop.getCropName(),
                crop.getLandDetails(),
                crop.getSeason(),
                crop.getSowingDate(),
                crop.getExpectedHarvestDate(),
                crop.getAdditionalNotes(),
                crop.getFarmer().getId(),
                crop.getTotalExpenses(),
                crop.getTotalIncome(),
                crop.getProfit());
    }
}
