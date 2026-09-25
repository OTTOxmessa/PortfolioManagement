package com.example.portfolio.controller.api;

import com.example.portfolio.service.AssetService;
import com.example.portfolio.service.market.SymbolSuggestion;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Auto-complete: GET /api/v1/symbols/search?q=aa -> AA, AAL, AAP, AAOI, AAPL ...
@RestController
@RequestMapping("/api/v1/symbols")
@RequiredArgsConstructor
public class SymbolController {

    private final AssetService assetService;

    @GetMapping("/search")
    public List<SymbolSuggestion> search(@RequestParam("q") String query,
                                         @RequestParam(defaultValue = "10") int limit) {
        return assetService.searchSymbols(query, Math.min(Math.max(limit, 1), 20));
    }
}
