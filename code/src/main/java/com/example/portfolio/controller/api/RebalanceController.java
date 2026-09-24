package com.example.portfolio.controller.api;

import com.example.portfolio.domain.enums.RebalanceMethod;
import com.example.portfolio.dto.response.RebalanceLogResponse;
import com.example.portfolio.mapper.RebalanceLogMapper;
import com.example.portfolio.service.PortfolioService;
import com.example.portfolio.service.rebalance.CalendarRebalanceStrategy;
import com.example.portfolio.service.rebalance.RebalanceService;
import com.example.portfolio.service.rebalance.RebalanceStrategy;
import com.example.portfolio.service.rebalance.ThresholdRebalanceStrategy;
import com.example.portfolio.service.rebalance.TradeOrder;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/portfolios/{portfolioId}/rebalance")
@RequiredArgsConstructor
public class RebalanceController {

    private final RebalanceService rebalanceService;
    private final PortfolioService portfolioService;
    private final ThresholdRebalanceStrategy thresholdStrategy;
    private final CalendarRebalanceStrategy calendarStrategy;

    // ?method=threshold|calendar — คำนวณแผนอย่างเดียว ยังไม่ซื้อขายจริง
    @GetMapping("/preview")
    public List<TradeOrder> preview(@AuthenticationPrincipal Long userId,
                                     @PathVariable Long portfolioId,
                                     @RequestParam(defaultValue = "threshold") String method) {
        portfolioService.getByIdForUser(portfolioId, userId);
        return rebalanceService.preview(portfolioId, resolveStrategy(method));
    }

    // ซื้อขายจริงตามแผน แล้วบันทึก RebalanceLog
    @PostMapping("/execute")
    public RebalanceLogResponse execute(@AuthenticationPrincipal Long userId,
                                         @PathVariable Long portfolioId,
                                         @RequestParam(defaultValue = "threshold") String method) {
        portfolioService.getByIdForUser(portfolioId, userId);
        RebalanceMethod rebalanceMethod = "calendar".equals(method)
                ? RebalanceMethod.CALENDAR
                : RebalanceMethod.THRESHOLD;
        var log = rebalanceService.execute(portfolioId, resolveStrategy(method), rebalanceMethod);
        return RebalanceLogMapper.toResponse(log);
    }

    private RebalanceStrategy resolveStrategy(String method) {
        return "calendar".equals(method) ? calendarStrategy : thresholdStrategy;
    }
}
