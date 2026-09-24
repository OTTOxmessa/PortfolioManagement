package com.example.portfolio.controller.api;

import com.example.portfolio.service.analysis.MovingAverageBandCalculator;
import com.example.portfolio.service.analysis.PivotPointCalculator;
import com.example.portfolio.service.analysis.SupportResistanceLevels;
import com.example.portfolio.service.analysis.SupportResistanceService;
import com.example.portfolio.service.analysis.SupportResistanceStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/assets/{assetId}/support-resistance")
@RequiredArgsConstructor
public class PriceAnalysisController {

    private final SupportResistanceService supportResistanceService;
    private final PivotPointCalculator pivotPointCalculator;
    private final MovingAverageBandCalculator movingAverageBandCalculator;

    // ?method=pivot|ma&from=&to= — default เป็น pivot
    @GetMapping
    public SupportResistanceLevels calculate(
            @PathVariable Long assetId,
            @RequestParam(defaultValue = "pivot") String method,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        SupportResistanceStrategy strategy = "ma".equals(method) ? movingAverageBandCalculator : pivotPointCalculator;
        return supportResistanceService.calculate(assetId, from, to, strategy);
    }
}
