package com.farmtrack.farmtrack.service;

import com.farmtrack.farmtrack.dto.IncomeRequest;
import com.farmtrack.farmtrack.dto.IncomeResponse;
import com.farmtrack.farmtrack.entity.Crop;
import com.farmtrack.farmtrack.entity.Income;
import com.farmtrack.farmtrack.exception.ResourceNotFoundException;
import com.farmtrack.farmtrack.repository.IncomeRepository;
import com.farmtrack.farmtrack.security.CurrentUserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.RoundingMode;
import java.util.List;

@Service
public class IncomeService {

    private final IncomeRepository incomeRepository;
    private final CropService cropService;
    private final CurrentUserService currentUser;

    public IncomeService(IncomeRepository incomeRepository, CropService cropService,
                         CurrentUserService currentUser) {
        this.incomeRepository = incomeRepository;
        this.cropService = cropService;
        this.currentUser = currentUser;
    }

    @Transactional
    public IncomeResponse addIncome(Long cropId, IncomeRequest request) {
        Crop crop = cropService.findOwnedCrop(cropId);
        Income income = new Income();
        income.setCrop(crop);
        copyFields(request, income);
        return IncomeResponse.from(incomeRepository.save(income));
    }

    @Transactional(readOnly = true)
    public List<IncomeResponse> getIncomesByCrop(Long cropId) {
        cropService.findOwnedCrop(cropId);
        return incomeRepository.findByCropIdOrderBySaleDateDesc(cropId).stream()
                .map(IncomeResponse::from)
                .toList();
    }

    /** Sales history across all crops of a farmer. */
    @Transactional(readOnly = true)
    public List<IncomeResponse> getSalesHistory(Long requestedFarmerId) {
        Long farmerId = currentUser.resolveFarmerId(requestedFarmerId);
        return incomeRepository.findByCropFarmerIdOrderBySaleDateDesc(farmerId).stream()
                .map(IncomeResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public IncomeResponse getIncome(Long id) {
        return IncomeResponse.from(findOwnedIncome(id));
    }

    @Transactional
    public IncomeResponse updateIncome(Long id, IncomeRequest request) {
        Income income = findOwnedIncome(id);
        copyFields(request, income);
        return IncomeResponse.from(income);
    }

    @Transactional
    public void deleteIncome(Long id) {
        incomeRepository.delete(findOwnedIncome(id));
    }

    private Income findOwnedIncome(Long id) {
        Income income = incomeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Income not found with id " + id));
        currentUser.checkFarmerAccess(income.getCrop().getFarmer().getId());
        return income;
    }

    private void copyFields(IncomeRequest request, Income income) {
        income.setQuantity(request.quantity());
        income.setUnit(request.unit());
        income.setPricePerUnit(request.pricePerUnit());
        // Business rule: total sales amount = quantity x price per unit (rounded to paise)
        income.setTotalAmount(request.quantity().multiply(request.pricePerUnit()).setScale(2, RoundingMode.HALF_UP));
        income.setSaleDate(request.saleDate());
        income.setDescription(request.description());
    }
}
