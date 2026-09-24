package com.example.portfolio.controller.api;

import com.example.portfolio.dto.request.AllocationTargetRequest;
import com.example.portfolio.dto.response.AllocationTargetResponse;
import com.example.portfolio.mapper.AllocationTargetMapper;
import com.example.portfolio.service.PortfolioService;
import com.example.portfolio.service.allocation.AllocationComparison;
import com.example.portfolio.service.allocation.AllocationService;
import com.example.portfolio.service.allocation.AllocationStrategy;
import com.example.portfolio.service.allocation.EqualWeightAllocationStrategy;
import com.example.portfolio.service.allocation.RiskBasedAllocationStrategy;
import com.example.portfolio.service.allocation.TargetPercentageAllocationStrategy;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/portfolios/{portfolioId}/allocation")
@RequiredArgsConstructor
public class AllocationController {

    private final AllocationService allocationService;
    private final PortfolioService portfolioService;
    private final EqualWeightAllocationStrategy equalWeightStrategy;
    private final TargetPercentageAllocationStrategy targetPercentageStrategy;
    private final RiskBasedAllocationStrategy riskBasedStrategy;

    // ?method=equal|target|risk — default target (ตามมูลค่าตลาดจริง) — จุดที่เลือก Strategy
    @GetMapping
    public List<AllocationComparison> compare(@AuthenticationPrincipal Long userId,
                                               @PathVariable Long portfolioId,
                                               @RequestParam(defaultValue = "target") String method) {
        portfolioService.getByIdForUser(portfolioId, userId);
        AllocationStrategy strategy = switch (method) {
            case "equal" -> equalWeightStrategy;
            case "risk" -> riskBasedStrategy;
            default -> targetPercentageStrategy;
        };
        return allocationService.compare(portfolioId, strategy);
    }

    @GetMapping("/targets")
    public List<AllocationTargetResponse> getTargets(@AuthenticationPrincipal Long userId,
                                                      @PathVariable Long portfolioId) {
        portfolioService.getByIdForUser(portfolioId, userId);
        return allocationService.getTargets(portfolioId).stream()
                .map(AllocationTargetMapper::toResponse)
                .toList();
    }

    @PutMapping("/targets")
    public AllocationTargetResponse setTarget(@AuthenticationPrincipal Long userId,
                                               @PathVariable Long portfolioId,
                                               @Valid @RequestBody AllocationTargetRequest request) {
        portfolioService.getByIdForUser(portfolioId, userId);
        var target = allocationService.upsertTarget(portfolioId, request.assetId(), request.targetPercent());
        return AllocationTargetMapper.toResponse(target);
    }
}
