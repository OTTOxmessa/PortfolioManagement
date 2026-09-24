package com.example.portfolio.controller.api;

import com.example.portfolio.domain.entity.Asset;
import com.example.portfolio.dto.request.AssetRequest;
import com.example.portfolio.dto.response.AssetResponse;
import com.example.portfolio.dto.response.PricePointResponse;
import com.example.portfolio.mapper.AssetMapper;
import com.example.portfolio.mapper.PriceHistoryMapper;
import com.example.portfolio.service.AssetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/assets")
@RequiredArgsConstructor
public class AssetController {

    private final AssetService assetService;

    @GetMapping
    public List<AssetResponse> list() {
        return assetService.getAll().stream().map(AssetMapper::toResponse).toList();
    }

    @GetMapping("/{id}")
    public AssetResponse get(@PathVariable Long id) {
        return AssetMapper.toResponse(assetService.getById(id));
    }

    // ราคาย้อนหลังสำหรับวาดกราฟ (ใช้คู่กับแนวรับ-แนวต้าน)
    @GetMapping("/{id}/prices")
    public List<PricePointResponse> prices(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return assetService.getPriceHistory(id, from, to).stream()
                .map(PriceHistoryMapper::toResponse)
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AssetResponse create(@Valid @RequestBody AssetRequest request) {
        Asset asset = Asset.builder()
                .symbol(request.symbol())
                .name(request.name())
                .assetType(request.assetType())
                .exchange(request.exchange())
                .build();
        return AssetMapper.toResponse(assetService.create(asset));
    }
}
