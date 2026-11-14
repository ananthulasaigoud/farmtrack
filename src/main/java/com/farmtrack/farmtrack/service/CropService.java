package com.farmtrack.farmtrack.service;

import com.farmtrack.farmtrack.dto.CropRequest;
import com.farmtrack.farmtrack.dto.CropResponse;
import com.farmtrack.farmtrack.entity.Crop;
import com.farmtrack.farmtrack.entity.Farmer;
import com.farmtrack.farmtrack.exception.BadRequestException;
import com.farmtrack.farmtrack.exception.CropNotFoundException;
import com.farmtrack.farmtrack.repository.CropRepository;
import com.farmtrack.farmtrack.security.CurrentUserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CropService {

    private final CropRepository cropRepository;
    private final FarmerService farmerService;
    private final CurrentUserService currentUser;

    public CropService(CropRepository cropRepository, FarmerService farmerService,
                       CurrentUserService currentUser) {
        this.cropRepository = cropRepository;
        this.farmerService = farmerService;
        this.currentUser = currentUser;
    }

    @Transactional
    public CropResponse addCrop(Long farmerId, CropRequest request) {
        currentUser.checkFarmerAccess(farmerId);
        Farmer farmer = farmerService.findFarmer(farmerId);
        validateDates(request);

        Crop crop = new Crop();
        crop.setFarmer(farmer);
        copyFields(request, crop);
        return CropResponse.from(cropRepository.save(crop));
    }

    @Transactional(readOnly = true)
    public List<CropResponse> getCropsByFarmerId(Long farmerId) {
        currentUser.checkFarmerAccess(farmerId);
        farmerService.findFarmer(farmerId); // 404 if the farmer does not exist
        return cropRepository.findByFarmerIdOrderByIdDesc(farmerId).stream()
                .map(CropResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public CropResponse getCropById(Long cropId) {
        return CropResponse.from(findOwnedCrop(cropId));
    }

    @Transactional
    public CropResponse updateCrop(Long cropId, CropRequest request) {
        Crop crop = findOwnedCrop(cropId);
        validateDates(request);
        copyFields(request, crop);
        return CropResponse.from(crop);
    }

    /** Also deletes the crop's expenses and incomes (cascade). */
    @Transactional
    public void deleteCrop(Long cropId) {
        cropRepository.delete(findOwnedCrop(cropId));
    }

    /** Loads a crop and checks that the logged-in user owns it. Used by the expense and income services too. */
    Crop findOwnedCrop(Long cropId) {
        Crop crop = cropRepository.findById(cropId).orElseThrow(() -> new CropNotFoundException(cropId));
        currentUser.checkFarmerAccess(crop.getFarmer().getId());
        return crop;
    }

    private void validateDates(CropRequest request) {
        if (request.sowingDate() != null && request.expectedHarvestDate() != null
                && request.expectedHarvestDate().isBefore(request.sowingDate())) {
            throw new BadRequestException("Expected harvest date cannot be before the sowing date");
        }
    }

    private void copyFields(CropRequest request, Crop crop) {
        crop.setCropName(request.cropName().trim());
        crop.setLandDetails(request.landDetails());
        crop.setSeason(request.season());
        crop.setSowingDate(request.sowingDate());
        crop.setExpectedHarvestDate(request.expectedHarvestDate());
        crop.setAdditionalNotes(request.additionalNotes());
    }
}
