package com.example.portfolio.service.impl;

import com.example.portfolio.domain.entity.Asset;
import com.example.portfolio.domain.entity.PriceHistory;
import com.example.portfolio.exception.ResourceNotFoundException;
import com.example.portfolio.repository.AssetRepository;
import com.example.portfolio.repository.PriceHistoryRepository;
import com.example.portfolio.service.AssetService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AssetServiceImpl implements AssetService {

    private final AssetRepository assetRepository;
    private final PriceHistoryRepository priceHistoryRepository;

    @Override
    public List<Asset> getAll() {
        return assetRepository.findAll();
    }

    @Override
    public Asset getById(Long id) {
        return assetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found: " + id));
    }

    @Override
    public Asset create(Asset asset) {
        asset.setSymbol(asset.getSymbol().trim().toUpperCase());
        // เช็คก่อน save ให้ได้ 409 พร้อมข้อความชัดเจน แทนที่จะปล่อยให้ DB unique constraint โยน 500
        if (assetRepository.existsBySymbol(asset.getSymbol())) {
            throw new IllegalStateException("มีสินทรัพย์ symbol " + asset.getSymbol() + " อยู่แล้ว");
        }
        return assetRepository.save(asset);
    }

    @Override
    public List<PriceHistory> getPriceHistory(Long assetId, LocalDate from, LocalDate to) {
        getById(assetId); // asset ไม่มีจริง -> 404
        return priceHistoryRepository.findByAssetIdAndPriceDateBetweenOrderByPriceDateAsc(assetId, from, to);
    }
}
