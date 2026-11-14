package com.farmtrack.farmtrack.repository;

import com.farmtrack.farmtrack.entity.Expense;
import com.farmtrack.farmtrack.entity.ExpenseCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    // "Crop_Id" walks the relationship: expense.crop.id
    List<Expense> findByCropIdOrderByExpenseDateDesc(Long cropId);

    // expense.crop.farmer.id
    List<Expense> findByCropFarmerIdOrderByExpenseDateDesc(Long farmerId);

    List<Expense> findByCropFarmerIdAndCategoryOrderByExpenseDateDesc(Long farmerId, ExpenseCategory category);

    List<Expense> findByCropFarmerIdAndExpenseDateBetween(Long farmerId, LocalDate from, LocalDate to);
}
