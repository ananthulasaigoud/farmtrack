package com.farmtrack.farmtrack.controller;

import com.farmtrack.farmtrack.dto.CropFinancialSummary;
import com.farmtrack.farmtrack.dto.DashboardSummary;
import com.farmtrack.farmtrack.dto.MonthlySummary;
import com.farmtrack.farmtrack.service.DashboardService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Farmers always see their own numbers. Admins pass ?farmerId=. */
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/summary")
    public DashboardSummary getSummary(@RequestParam(required = false) Long farmerId) {
        return dashboardService.getSummary(farmerId);
    }

    @GetMapping("/crops/{cropId}")
    public CropFinancialSummary getCropSummary(@PathVariable Long cropId) {
        return dashboardService.getCropSummary(cropId);
    }

    @GetMapping("/monthly")
    public List<MonthlySummary> getMonthlySummary(@RequestParam(required = false) Long farmerId,
                                                  @RequestParam(required = false) Integer year) {
        return dashboardService.getMonthlySummary(farmerId, year);
    }
}
