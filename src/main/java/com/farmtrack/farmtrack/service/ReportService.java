package com.farmtrack.farmtrack.service;

import com.farmtrack.farmtrack.dto.*;
import com.farmtrack.farmtrack.exception.BadRequestException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

/** Builds downloadable CSV reports (open them in Excel or Google Sheets). */
@Service
public class ReportService {

    /** Report types accepted by GET /api/reports/download?type= */
    public static final List<String> REPORT_TYPES = List.of("crops", "monthly", "expenses", "incomes");

    private final DashboardService dashboardService;
    private final ExpenseService expenseService;
    private final IncomeService incomeService;

    public ReportService(DashboardService dashboardService, ExpenseService expenseService,
                         IncomeService incomeService) {
        this.dashboardService = dashboardService;
        this.expenseService = expenseService;
        this.incomeService = incomeService;
    }

    public record CsvReport(String fileName, String content) {
    }

    @Transactional(readOnly = true)
    public CsvReport buildCsv(String type, Long farmerId, Integer year) {
        String reportType = type == null ? "" : type.trim().toLowerCase(Locale.ROOT);
        return switch (reportType) {
            case "crops" -> new CsvReport("farmtrack-crop-profit-report.csv", cropReport(farmerId));
            case "monthly" -> {
                int reportYear = year != null ? year : LocalDate.now().getYear();
                yield new CsvReport("farmtrack-monthly-report-" + reportYear + ".csv", monthlyReport(farmerId, reportYear));
            }
            case "expenses" -> new CsvReport("farmtrack-expenses.csv", expenseReport(farmerId));
            case "incomes" -> new CsvReport("farmtrack-sales.csv", incomeReport(farmerId));
            default -> throw new BadRequestException("Report type must be one of " + REPORT_TYPES);
        };
    }

    private String cropReport(Long farmerId) {
        DashboardSummary summary = dashboardService.getSummary(farmerId);
        StringBuilder csv = new StringBuilder("Crop,Season,Total Expenses,Total Income,Profit/Loss,Status\n");
        for (CropFinancialSummary crop : summary.crops()) {
            row(csv, text(crop.cropName()), text(crop.season()), num(crop.totalExpenses()),
                    num(crop.totalIncome()), num(crop.profit()), text(crop.status()));
        }
        row(csv, "TOTAL", "", num(summary.totalExpenses()), num(summary.totalIncome()),
                num(summary.profit()), text(summary.status()));
        return csv.toString();
    }

    private String monthlyReport(Long farmerId, int year) {
        StringBuilder csv = new StringBuilder("Year,Month,Total Income,Total Expenses,Profit/Loss\n");
        for (MonthlySummary month : dashboardService.getMonthlySummary(farmerId, year)) {
            row(csv, String.valueOf(month.year()), month.monthName(), num(month.totalIncome()),
                    num(month.totalExpenses()), num(month.profit()));
        }
        return csv.toString();
    }

    private String expenseReport(Long farmerId) {
        StringBuilder csv = new StringBuilder("Date,Crop,Category,Amount,Description\n");
        for (ExpenseResponse e : expenseService.getExpenseHistory(farmerId, null)) {
            row(csv, String.valueOf(e.expenseDate()), text(e.cropName()), e.category().name(),
                    num(e.amount()), text(e.description()));
        }
        return csv.toString();
    }

    private String incomeReport(Long farmerId) {
        StringBuilder csv = new StringBuilder("Date,Crop,Quantity,Unit,Price Per Unit,Total Amount,Description\n");
        for (IncomeResponse i : incomeService.getSalesHistory(farmerId)) {
            row(csv, String.valueOf(i.saleDate()), text(i.cropName()), num(i.quantity()), text(i.unit()),
                    num(i.pricePerUnit()), num(i.totalAmount()), text(i.description()));
        }
        return csv.toString();
    }

    private static void row(StringBuilder csv, String... cells) {
        csv.append(String.join(",", cells)).append('\n');
    }

    private static String num(BigDecimal value) {
        return value == null ? "0" : value.toPlainString();
    }

    /**
     * Quotes text cells and blocks spreadsheet formula injection
     * (a value such as =HYPERLINK(...) typed by a user must not run in Excel).
     */
    private static String text(String value) {
        if (value == null) {
            return "";
        }
        String safe = value;
        if (!safe.isEmpty() && "=+-@\t\r".indexOf(safe.charAt(0)) >= 0) {
            safe = "'" + safe;
        }
        return "\"" + safe.replace("\"", "\"\"") + "\"";
    }
}
