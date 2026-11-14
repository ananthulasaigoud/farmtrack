package com.farmtrack.farmtrack.dto;

import com.farmtrack.farmtrack.entity.Farmer;

public record FarmerResponse(Long id, String name, String phone, String village, int totalCrops) {

    public static FarmerResponse from(Farmer farmer) {
        return new FarmerResponse(farmer.getId(), farmer.getName(), farmer.getPhone(),
                farmer.getVillage(), farmer.getCrops().size());
    }
}
