package com.example.portfolio.controller.api;

import com.example.portfolio.domain.entity.Portfolio;
import com.example.portfolio.dto.request.PortfolioRequest;
import com.example.portfolio.dto.response.PortfolioResponse;
import com.example.portfolio.mapper.PortfolioMapper;
import com.example.portfolio.service.PortfolioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

// อัปเดตรอบนี้: เปลี่ยนจาก @RequestHeader("X-User-Id") ชั่วคราว มาเป็น @AuthenticationPrincipal
// ที่ JwtAuthenticationFilter ตั้งค่าไว้ให้จริงจากการ login ผ่าน /api/v1/auth/login
@RestController
@RequestMapping("/api/v1/portfolios")
@RequiredArgsConstructor
public class PortfolioController {

    private final PortfolioService portfolioService;

    @GetMapping
    public Page<PortfolioResponse> list(@AuthenticationPrincipal Long userId, Pageable pageable) {
        return portfolioService.listForUser(userId, pageable).map(PortfolioMapper::toResponse);
    }

    @GetMapping("/{id}")
    public PortfolioResponse get(@AuthenticationPrincipal Long userId, @PathVariable Long id) {
        return PortfolioMapper.toResponse(portfolioService.getByIdForUser(id, userId));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PortfolioResponse create(@AuthenticationPrincipal Long userId,
                                     @Valid @RequestBody PortfolioRequest request) {
        Portfolio portfolio = Portfolio.builder()
                .name(request.name())
                .baseCurrency(request.baseCurrency())
                .build();
        return PortfolioMapper.toResponse(portfolioService.create(userId, portfolio));
    }

    @PutMapping("/{id}")
    public PortfolioResponse update(@AuthenticationPrincipal Long userId, @PathVariable Long id,
                                     @Valid @RequestBody PortfolioRequest request) {
        Portfolio updates = Portfolio.builder()
                .name(request.name())
                .baseCurrency(request.baseCurrency())
                .build();
        return PortfolioMapper.toResponse(portfolioService.update(id, userId, updates));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal Long userId, @PathVariable Long id) {
        portfolioService.delete(id, userId);
    }
}
