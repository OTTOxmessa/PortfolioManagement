package com.example.portfolio.service;

import com.example.portfolio.domain.entity.Asset;
import com.example.portfolio.domain.entity.PriceHistory;

import java.time.LocalDate;
import java.util.List;

public interface AssetService {

    List<Asset> getAll();

    Asset getById(Long id);

    Asset create(Asset asset);

    List<PriceHistory> getPriceHistory(Long assetId, LocalDate from, LocalDate to);
}
