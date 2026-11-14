package com.farmtrack.farmtrack.repository;

import com.farmtrack.farmtrack.entity.Income;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface IncomeRepository extends JpaRepository<Income, Long> {

    List<Income> findByCropIdOrderBySaleDateDesc(Long cropId);

    List<Income> findByCropFarmerIdOrderBySaleDateDesc(Long farmerId);

    List<Income> findByCropFarmerIdAndSaleDateBetween(Long farmerId, LocalDate from, LocalDate to);
}
