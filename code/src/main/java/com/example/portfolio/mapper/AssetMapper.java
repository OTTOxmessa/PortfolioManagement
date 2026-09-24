package com.example.portfolio.mapper;

import com.example.portfolio.domain.entity.Asset;
import com.example.portfolio.dto.response.AssetResponse;

public class AssetMapper {

    private AssetMapper() {
    }

    public static AssetResponse toResponse(Asset asset) {
        return new AssetResponse(
                asset.getId(), asset.getSymbol(), asset.getName(), asset.getAssetType(), asset.getExchange());
    }
}
