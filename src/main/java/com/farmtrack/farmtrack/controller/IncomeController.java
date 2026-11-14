package com.farmtrack.farmtrack.controller;

import com.farmtrack.farmtrack.dto.IncomeRequest;
import com.farmtrack.farmtrack.dto.IncomeResponse;
import com.farmtrack.farmtrack.service.IncomeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class IncomeController {

    private final IncomeService incomeService;

    public IncomeController(IncomeService incomeService) {
        this.incomeService = incomeService;
    }

    @PostMapping("/crops/{cropId}/incomes")
    public ResponseEntity<IncomeResponse> addIncome(@PathVariable Long cropId,
                                                    @Valid @RequestBody IncomeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(incomeService.addIncome(cropId, request));
    }

    @GetMapping("/crops/{cropId}/incomes")
    public List<IncomeResponse> getCropIncomes(@PathVariable Long cropId) {
        return incomeService.getIncomesByCrop(cropId);
    }

    /** Sales history of the logged-in farmer. Admins add ?farmerId=. */
    @GetMapping("/incomes")
    public List<IncomeResponse> getSalesHistory(@RequestParam(required = false) Long farmerId) {
        return incomeService.getSalesHistory(farmerId);
    }

    @GetMapping("/incomes/{id}")
    public IncomeResponse getIncome(@PathVariable Long id) {
        return incomeService.getIncome(id);
    }

    @PutMapping("/incomes/{id}")
    public IncomeResponse updateIncome(@PathVariable Long id, @Valid @RequestBody IncomeRequest request) {
        return incomeService.updateIncome(id, request);
    }

    @DeleteMapping("/incomes/{id}")
    public ResponseEntity<Void> deleteIncome(@PathVariable Long id) {
        incomeService.deleteIncome(id);
        return ResponseEntity.noContent().build();
    }
}
