package com.farmtrack.farmtrack.controller;

import com.farmtrack.farmtrack.dto.CropRequest;
import com.farmtrack.farmtrack.dto.CropResponse;
import com.farmtrack.farmtrack.service.CropService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class CropController {

    // Constructor injection
    private final CropService cropService;

    public CropController(CropService cropService) {
        this.cropService = cropService;
    }

    @PostMapping("/farmers/{farmerId}/crops")
    public ResponseEntity<CropResponse> addCrop(@PathVariable Long farmerId,
                                                @Valid @RequestBody CropRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cropService.addCrop(farmerId, request));
    }

    @GetMapping("/farmers/{farmerId}/crops")
    public List<CropResponse> getCropsByFarmerId(@PathVariable Long farmerId) {
        return cropService.getCropsByFarmerId(farmerId);
    }

    @GetMapping("/crops/{cropId}")
    public CropResponse getCropById(@PathVariable Long cropId) {
        return cropService.getCropById(cropId);
    }

    @PutMapping("/crops/{cropId}")
    public CropResponse updateCrop(@PathVariable Long cropId, @Valid @RequestBody CropRequest request) {
        return cropService.updateCrop(cropId, request);
    }

    @DeleteMapping("/crops/{cropId}")
    public ResponseEntity<Void> deleteCrop(@PathVariable Long cropId) {
        cropService.deleteCrop(cropId);
        return ResponseEntity.noContent().build();
    }
}
