package com.farmtrack.farmtrack.service;

import com.farmtrack.farmtrack.dto.CropFinancialSummary;
import com.farmtrack.farmtrack.dto.DashboardSummary;
import com.farmtrack.farmtrack.dto.MonthlySummary;
import com.farmtrack.farmtrack.entity.Crop;
import com.farmtrack.farmtrack.entity.Expense;
import com.farmtrack.farmtrack.entity.ExpenseCategory;
import com.farmtrack.farmtrack.entity.Income;
import com.farmtrack.farmtrack.exception.BadRequestException;
import com.farmtrack.farmtrack.repository.CropRepository;
import com.farmtrack.farmtrack.repository.ExpenseRepository;
import com.farmtrack.farmtrack.repository.IncomeRepository;
import com.farmtrack.farmtrack.security.CurrentUserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.*;

/**
 * All profit and loss calculations.
 * profit = totalIncome - totalExpenses (a negative profit is a loss).
 */
@Service
public class DashboardService {

    private final CropRepository cropRepository;
    private final ExpenseRepository expenseRepository;
    private final IncomeRepository incomeRepository;
    private final CropService cropService;
    private final CurrentUserService currentUser;

    public DashboardService(CropRepository cropRepository, ExpenseRepository expenseRepository,
                            IncomeRepository incomeRepository, CropService cropService,
                            CurrentUserService currentUser) {
        this.cropRepository = cropRepository;
        this.expenseRepository = expenseRepository;
        this.incomeRepository = incomeRepository;
        this.cropService = cropService;
        this.currentUser = currentUser;
    }

    /** Overall summary for a farmer: totals, crop-wise profit, expenses by category and best crop. */
    @Transactional(readOnly = true)
    public DashboardSummary getSummary(Long requestedFarmerId) {
        Long farmerId = currentUser.resolveFarmerId(requestedFarmerId);
        List<Crop> crops = cropRepository.findByFarmerIdOrderByIdDesc(farmerId);

        List<CropFinancialSummary> cropSummaries = crops.stream().map(this::toCropSummary).toList();

        BigDecimal totalExpenses = BigDecimal.ZERO;
        BigDecimal totalIncome = BigDecimal.ZERO;
        Map<ExpenseCategory, BigDecimal> expensesByCategory = emptyCategoryMap();
        for (CropFinancialSummary summary : cropSummaries) {
            totalExpenses = totalExpenses.add(summary.totalExpenses());
            totalIncome = totalIncome.add(summary.totalIncome());
            summary.expensesByCategory().forEach((category, amount) -> expensesByCategory.merge(category, amount, BigDecimal::add));
        }
        BigDecimal profit = totalIncome.subtract(totalExpenses);

        return new DashboardSummary(farmerId, crops.size(), totalExpenses, totalIncome, profit, status(profit),
                expensesByCategory, cropSummaries, findBestCrop(cropSummaries));
    }

    @Transactional(readOnly = true)
    public CropFinancialSummary getCropSummary(Long cropId) {
        return toCropSummary(cropService.findOwnedCrop(cropId));
    }

    /** Income, expenses and profit for each of the 12 months of a year (default: current year). */
    @Transactional(readOnly = true)
    public List<MonthlySummary> getMonthlySummary(Long requestedFarmerId, Integer year) {
        Long farmerId = currentUser.resolveFarmerId(requestedFarmerId);
        int reportYear = year != null ? year : LocalDate.now().getYear();
        if (reportYear < 1900 || reportYear > 3000) {
            throw new BadRequestException("Year must be between 1900 and 3000");
        }

        LocalDate from = LocalDate.of(reportYear, 1, 1);
        LocalDate to = LocalDate.of(reportYear, 12, 31);

        BigDecimal[] incomeByMonth = zeroArray();
        BigDecimal[] expenseByMonth = zeroArray();

        for (Income income : incomeRepository.findByCropFarmerIdAndSaleDateBetween(farmerId, from, to)) {
            int m = income.getSaleDate().getMonthValue() - 1;
            incomeByMonth[m] = incomeByMonth[m].add(income.getTotalAmount());
        }
        for (Expense expense : expenseRepository.findByCropFarmerIdAndExpenseDateBetween(farmerId, from, to)) {
            int m = expense.getExpenseDate().getMonthValue() - 1;
            expenseByMonth[m] = expenseByMonth[m].add(expense.getAmount());
        }

        List<MonthlySummary> months = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            String monthName = Month.of(i + 1).getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
            months.add(new MonthlySummary(reportYear, i + 1, monthName, incomeByMonth[i], expenseByMonth[i],
                    incomeByMonth[i].subtract(expenseByMonth[i])));
        }
        return months;
    }

    /** The crop with the highest profit among crops that have recorded sales; null if none has sales. */
    CropFinancialSummary findBestCrop(List<CropFinancialSummary> crops) {
        return crops.stream()
                .filter(c -> c.totalIncome().signum() > 0)
                .max(Comparator.comparing(CropFinancialSummary::profit))
                .orElse(null);
    }

    private CropFinancialSummary toCropSummary(Crop crop) {
        Map<ExpenseCategory, BigDecimal> byCategory = emptyCategoryMap();
        for (Expense expense : crop.getExpenses()) {
            byCategory.merge(expense.getCategory(), expense.getAmount(), BigDecimal::add);
        }
        BigDecimal profit = crop.getProfit();
        return new CropFinancialSummary(crop.getId(), crop.getCropName(), crop.getSeason(),
                crop.getTotalExpenses(), crop.getTotalIncome(), profit, status(profit), byCategory);
    }

    public static String status(BigDecimal profit) {
        return switch (profit.signum()) {
            case 1 -> "PROFIT";
            case -1 -> "LOSS";
            default -> "BREAK_EVEN";
        };
    }

    private static Map<ExpenseCategory, BigDecimal> emptyCategoryMap() {
        Map<ExpenseCategory, BigDecimal> map = new EnumMap<>(ExpenseCategory.class);
        for (ExpenseCategory category : ExpenseCategory.values()) {
            map.put(category, BigDecimal.ZERO);
        }
        return map;
    }

    private static BigDecimal[] zeroArray() {
        BigDecimal[] values = new BigDecimal[12];
        Arrays.fill(values, BigDecimal.ZERO);
        return values;
    }
}
