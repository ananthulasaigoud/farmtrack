package com.farmtrack.farmtrack.service;

import com.farmtrack.farmtrack.dto.ExpenseRequest;
import com.farmtrack.farmtrack.dto.ExpenseResponse;
import com.farmtrack.farmtrack.entity.Crop;
import com.farmtrack.farmtrack.entity.Expense;
import com.farmtrack.farmtrack.entity.ExpenseCategory;
import com.farmtrack.farmtrack.exception.ResourceNotFoundException;
import com.farmtrack.farmtrack.repository.ExpenseRepository;
import com.farmtrack.farmtrack.security.CurrentUserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final CropService cropService;
    private final CurrentUserService currentUser;

    public ExpenseService(ExpenseRepository expenseRepository, CropService cropService,
                          CurrentUserService currentUser) {
        this.expenseRepository = expenseRepository;
        this.cropService = cropService;
        this.currentUser = currentUser;
    }

    @Transactional
    public ExpenseResponse addExpense(Long cropId, ExpenseRequest request) {
        Crop crop = cropService.findOwnedCrop(cropId);
        Expense expense = new Expense();
        expense.setCrop(crop);
        copyFields(request, expense);
        return ExpenseResponse.from(expenseRepository.save(expense));
    }

    @Transactional(readOnly = true)
    public List<ExpenseResponse> getExpensesByCrop(Long cropId) {
        cropService.findOwnedCrop(cropId);
        return expenseRepository.findByCropIdOrderByExpenseDateDesc(cropId).stream()
                .map(ExpenseResponse::from)
                .toList();
    }

    /** Expense history across all crops of a farmer, optionally filtered by category. */
    @Transactional(readOnly = true)
    public List<ExpenseResponse> getExpenseHistory(Long requestedFarmerId, ExpenseCategory category) {
        Long farmerId = currentUser.resolveFarmerId(requestedFarmerId);
        List<Expense> expenses = category == null
                ? expenseRepository.findByCropFarmerIdOrderByExpenseDateDesc(farmerId)
                : expenseRepository.findByCropFarmerIdAndCategoryOrderByExpenseDateDesc(farmerId, category);
        return expenses.stream().map(ExpenseResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public ExpenseResponse getExpense(Long id) {
        return ExpenseResponse.from(findOwnedExpense(id));
    }

    @Transactional
    public ExpenseResponse updateExpense(Long id, ExpenseRequest request) {
        Expense expense = findOwnedExpense(id);
        copyFields(request, expense);
        return ExpenseResponse.from(expense);
    }

    @Transactional
    public void deleteExpense(Long id) {
        expenseRepository.delete(findOwnedExpense(id));
    }

    private Expense findOwnedExpense(Long id) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found with id " + id));
        currentUser.checkFarmerAccess(expense.getCrop().getFarmer().getId());
        return expense;
    }

    private void copyFields(ExpenseRequest request, Expense expense) {
        expense.setCategory(request.category());
        expense.setAmount(request.amount());
        expense.setExpenseDate(request.expenseDate());
        expense.setDescription(request.description());
    }
}
