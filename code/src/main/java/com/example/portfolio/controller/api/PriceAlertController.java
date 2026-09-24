package com.example.portfolio.controller.api;

import com.example.portfolio.domain.entity.PriceAlert;
import com.example.portfolio.dto.request.PriceAlertRequest;
import com.example.portfolio.dto.response.PriceAlertResponse;
import com.example.portfolio.mapper.PriceAlertMapper;
import com.example.portfolio.service.PortfolioService;
import com.example.portfolio.service.alert.PriceAlertService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/portfolios/{portfolioId}/alerts")
@RequiredArgsConstructor
public class PriceAlertController {

    private final PriceAlertService priceAlertService;
    private final PortfolioService portfolioService;

    @GetMapping
    public List<PriceAlertResponse> list(@AuthenticationPrincipal Long userId, @PathVariable Long portfolioId) {
        portfolioService.getByIdForUser(portfolioId, userId);
        return priceAlertService.getByPortfolio(portfolioId).stream()
                .map(PriceAlertMapper::toResponse)
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PriceAlertResponse create(@AuthenticationPrincipal Long userId,
                                      @PathVariable Long portfolioId,
                                      @Valid @RequestBody PriceAlertRequest request) {
        portfolioService.getByIdForUser(portfolioId, userId);
        PriceAlert alert = PriceAlert.builder()
                .condition(request.condition())
                .targetPrice(request.targetPrice())
                .build();
        return PriceAlertMapper.toResponse(priceAlertService.create(portfolioId, request.assetId(), alert));
    }

    @DeleteMapping("/{alertId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal Long userId,
                       @PathVariable Long portfolioId,
                       @PathVariable Long alertId) {
        portfolioService.getByIdForUser(portfolioId, userId);
        // service เช็คต่ออีกชั้นว่า alert นี้อยู่ในพอร์ตนี้จริง (กันการลบ alert ของพอร์ตอื่นด้วยการเดา alertId)
        priceAlertService.delete(portfolioId, alertId);
    }
}
