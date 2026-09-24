package com.example.portfolio.controller.api;

import com.example.portfolio.service.PortfolioService;
import com.example.portfolio.service.performance.BenchmarkComparisonService;
import com.example.portfolio.service.performance.PerformanceReport;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/portfolios/{portfolioId}/performance")
@RequiredArgsConstructor
public class PerformanceController {

    private final BenchmarkComparisonService benchmarkComparisonService;
    private final PortfolioService portfolioService;

    // ตัวอย่าง: GET /performance?benchmark=SET&from=2026-01-05&to=2026-09-24
    @GetMapping
    public PerformanceReport getReport(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long portfolioId,
            @RequestParam String benchmark,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        portfolioService.getByIdForUser(portfolioId, userId);
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("วันเริ่มต้นต้องไม่อยู่หลังวันสิ้นสุด");
        }
        return benchmarkComparisonService.generateReport(portfolioId, benchmark, from, to);
    }
}
