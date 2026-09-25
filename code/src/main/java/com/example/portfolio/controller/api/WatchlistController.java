package com.example.portfolio.controller.api;

import com.example.portfolio.dto.request.WatchlistRequest;
import com.example.portfolio.service.WatchlistService;
import com.example.portfolio.service.market.Quote;
import com.example.portfolio.service.market.QuoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/watchlist")
@RequiredArgsConstructor
public class WatchlistController {

    private final WatchlistService watchlistService;
    private final QuoteService quoteService;

    // คืนราคาล่าสุด + % เปลี่ยนแปลงรายวันของทุกตัวใน watchlist
    @GetMapping
    public List<Quote> list(@AuthenticationPrincipal Long userId) {
        return watchlistService.getWatchlist(userId).stream()
                .map(quoteService::getQuote)
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Quote add(@AuthenticationPrincipal Long userId, @Valid @RequestBody WatchlistRequest request) {
        return quoteService.getQuote(watchlistService.add(userId, request.assetId()));
    }

    @DeleteMapping("/{assetId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(@AuthenticationPrincipal Long userId, @PathVariable Long assetId) {
        watchlistService.remove(userId, assetId);
    }
}
