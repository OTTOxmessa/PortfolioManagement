package com.example.portfolio.controller.api;

import com.example.portfolio.dto.response.HoldingResponse;
import com.example.portfolio.mapper.HoldingMapper;
import com.example.portfolio.service.HoldingService;
import com.example.portfolio.service.PortfolioService;
import com.example.portfolio.service.market.QuoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// ไม่มี POST/PUT โดยตั้งใจ — Holding เปลี่ยนแปลงผ่าน TransactionController เท่านั้น
@RestController
@RequestMapping("/api/v1/portfolios/{portfolioId}/holdings")
@RequiredArgsConstructor
public class HoldingController {

    private final HoldingService holdingService;
    private final PortfolioService portfolioService;
    private final QuoteService quoteService;

    @GetMapping
    public List<HoldingResponse> list(@AuthenticationPrincipal Long userId, @PathVariable Long portfolioId) {
        portfolioService.getByIdForUser(portfolioId, userId);
        return holdingService.getByPortfolio(portfolioId).stream()
                .filter(h -> h.getQuantity().signum() > 0) // ขายหมดแล้วไม่ต้องแสดง
                .map(h -> HoldingMapper.toResponse(h,
                        quoteService.latestPrice(h.getAsset().getId()).orElse(null)))
                .toList();
    }
}
