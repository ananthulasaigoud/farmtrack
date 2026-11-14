package com.farmtrack.farmtrack.controller;

import com.farmtrack.farmtrack.dto.CropFinancialSummary;
import com.farmtrack.farmtrack.dto.MonthlySummary;
import com.farmtrack.farmtrack.service.DashboardService;
import com.farmtrack.farmtrack.service.ReportService;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final DashboardService dashboardService;
    private final ReportService reportService;

    public ReportController(DashboardService dashboardService, ReportService reportService) {
        this.dashboardService = dashboardService;
        this.reportService = reportService;
    }

    /** Crop-wise profit and loss. */
    @GetMapping("/crops")
    public List<CropFinancialSummary> getCropWiseReport(@RequestParam(required = false) Long farmerId) {
        return dashboardService.getSummary(farmerId).crops();
    }

    /** Monthly income and expenses. */
    @GetMapping("/monthly")
    public List<MonthlySummary> getMonthlyReport(@RequestParam(required = false) Long farmerId,
                                                 @RequestParam(required = false) Integer year) {
        return dashboardService.getMonthlySummary(farmerId, year);
    }

    /** Best-performing crop. 204 No Content when no crop has sales yet. */
    @GetMapping("/best-crop")
    public ResponseEntity<CropFinancialSummary> getBestCrop(@RequestParam(required = false) Long farmerId) {
        CropFinancialSummary best = dashboardService.getSummary(farmerId).bestCrop();
        return best == null ? ResponseEntity.noContent().build() : ResponseEntity.ok(best);
    }

    /** Download a CSV report: type = crops | monthly | expenses | incomes. */
    @GetMapping("/download")
    public ResponseEntity<byte[]> download(@RequestParam String type,
                                           @RequestParam(required = false) Long farmerId,
                                           @RequestParam(required = false) Integer year) {
        ReportService.CsvReport report = reportService.buildCsv(type, farmerId, year);
        // The BOM lets Excel open the file as UTF-8 (needed for names written in Telugu, Hindi, ...)
        byte[] body = ("﻿" + report.content()).getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(report.fileName()).build().toString())
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(body);
    }
}
