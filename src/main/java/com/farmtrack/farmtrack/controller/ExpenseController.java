package com.farmtrack.farmtrack.controller;

import com.farmtrack.farmtrack.dto.ExpenseRequest;
import com.farmtrack.farmtrack.dto.ExpenseResponse;
import com.farmtrack.farmtrack.entity.ExpenseCategory;
import com.farmtrack.farmtrack.service.ExpenseService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @PostMapping("/crops/{cropId}/expenses")
    public ResponseEntity<ExpenseResponse> addExpense(@PathVariable Long cropId,
                                                      @Valid @RequestBody ExpenseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(expenseService.addExpense(cropId, request));
    }

    @GetMapping("/crops/{cropId}/expenses")
    public List<ExpenseResponse> getCropExpenses(@PathVariable Long cropId) {
        return expenseService.getExpensesByCrop(cropId);
    }

    /** Expense history of the logged-in farmer. Optional: ?category=SEEDS. Admins add ?farmerId=. */
    @GetMapping("/expenses")
    public List<ExpenseResponse> getExpenseHistory(@RequestParam(required = false) Long farmerId,
                                                   @RequestParam(required = false) ExpenseCategory category) {
        return expenseService.getExpenseHistory(farmerId, category);
    }

    @GetMapping("/expenses/{id}")
    public ExpenseResponse getExpense(@PathVariable Long id) {
        return expenseService.getExpense(id);
    }

    @PutMapping("/expenses/{id}")
    public ExpenseResponse updateExpense(@PathVariable Long id, @Valid @RequestBody ExpenseRequest request) {
        return expenseService.updateExpense(id, request);
    }

    @DeleteMapping("/expenses/{id}")
    public ResponseEntity<Void> deleteExpense(@PathVariable Long id) {
        expenseService.deleteExpense(id);
        return ResponseEntity.noContent().build();
    }
}
