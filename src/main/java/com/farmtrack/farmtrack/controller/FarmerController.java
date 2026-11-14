package com.farmtrack.farmtrack.controller;

import com.farmtrack.farmtrack.dto.FarmerRequest;
import com.farmtrack.farmtrack.dto.FarmerResponse;
import com.farmtrack.farmtrack.service.FarmerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Farmer records.
 * POST and GET-all are admin-only (configured in SecurityConfig); farmers sign up through /api/auth/register.
 * GET/PUT/DELETE by id: the farmer themself or an admin.
 */
@RestController
@RequestMapping("/api/farmers")
public class FarmerController {

    private final FarmerService farmerService;

    public FarmerController(FarmerService farmerService) {
        this.farmerService = farmerService;
    }

    @PostMapping
    public ResponseEntity<FarmerResponse> createFarmer(@Valid @RequestBody FarmerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(farmerService.createFarmer(request));
    }

    @GetMapping
    public List<FarmerResponse> getAllFarmers() {
        return farmerService.getAllFarmers();
    }

    @GetMapping("/{id}")
    public FarmerResponse getFarmerById(@PathVariable Long id) {
        return farmerService.getFarmerById(id);
    }

    @PutMapping("/{id}")
    public FarmerResponse updateFarmer(@PathVariable Long id, @Valid @RequestBody FarmerRequest request) {
        return farmerService.updateFarmer(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFarmer(@PathVariable Long id) {
        farmerService.deleteFarmer(id);
        return ResponseEntity.noContent().build();
    }
}
