package com.farmtrack.farmtrack.service;

import com.farmtrack.farmtrack.config.GuestAccount;
import com.farmtrack.farmtrack.dto.FarmerRequest;
import com.farmtrack.farmtrack.dto.FarmerResponse;
import com.farmtrack.farmtrack.entity.Farmer;
import com.farmtrack.farmtrack.exception.DuplicateResourceException;
import com.farmtrack.farmtrack.exception.FarmerNotFoundException;
import com.farmtrack.farmtrack.repository.FarmerRepository;
import com.farmtrack.farmtrack.repository.UserRepository;
import com.farmtrack.farmtrack.security.CurrentUserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FarmerService {

    private final FarmerRepository farmerRepository;
    private final UserRepository userRepository;
    private final CurrentUserService currentUser;
    private final GuestAccount guestAccount;

    public FarmerService(FarmerRepository farmerRepository, UserRepository userRepository,
                         CurrentUserService currentUser, GuestAccount guestAccount) {
        this.farmerRepository = farmerRepository;
        this.userRepository = userRepository;
        this.currentUser = currentUser;
        this.guestAccount = guestAccount;
    }

    /** Admin only: create a farmer record without a login account (self sign-up uses /api/auth/register). */
    @Transactional
    public FarmerResponse createFarmer(FarmerRequest request) {
        ensurePhoneIsFree(request.phone(), null);
        Farmer farmer = new Farmer(request.name().trim(), request.phone(), request.village().trim());
        return FarmerResponse.from(farmerRepository.save(farmer));
    }

    /** Admin only. */
    @Transactional(readOnly = true)
    public List<FarmerResponse> getAllFarmers() {
        return farmerRepository.findAll().stream().map(FarmerResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public FarmerResponse getFarmerById(Long id) {
        currentUser.checkFarmerAccess(id);
        return FarmerResponse.from(findFarmer(id));
    }

    @Transactional
    public FarmerResponse updateFarmer(Long id, FarmerRequest request) {
        currentUser.checkFarmerAccess(id);
        Farmer farmer = findFarmer(id);
        guestAccount.checkNotGuest(farmer, "change its details");
        ensurePhoneIsFree(request.phone(), id);

        farmer.setName(request.name().trim());
        farmer.setPhone(request.phone());
        farmer.setVillage(request.village().trim());
        return FarmerResponse.from(farmer); // saved automatically at the end of the transaction
    }

    /** Deletes the farmer, their login account, crops, expenses and incomes. */
    @Transactional
    public void deleteFarmer(Long id) {
        currentUser.checkFarmerAccess(id);
        Farmer farmer = findFarmer(id);
        guestAccount.checkNotGuest(farmer, "delete it");
        userRepository.findByFarmerId(id).ifPresent(userRepository::delete);
        farmerRepository.delete(farmer);
    }

    Farmer findFarmer(Long id) {
        return farmerRepository.findById(id).orElseThrow(() -> new FarmerNotFoundException(id));
    }

    /** currentFarmerId = null when creating a new farmer. */
    private void ensurePhoneIsFree(String phone, Long currentFarmerId) {
        boolean usedByOther = currentFarmerId == null
                ? farmerRepository.existsByPhone(phone)
                : farmerRepository.existsByPhoneAndIdNot(phone, currentFarmerId);
        if (usedByOther) {
            throw new DuplicateResourceException("Phone number " + phone + " is already registered");
        }
    }
}
