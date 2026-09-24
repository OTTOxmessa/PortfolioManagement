package com.example.portfolio.mapper;

import com.example.portfolio.domain.entity.PriceAlert;
import com.example.portfolio.dto.response.PriceAlertResponse;

public class PriceAlertMapper {

    private PriceAlertMapper() {
    }

    public static PriceAlertResponse toResponse(PriceAlert alert) {
        return new PriceAlertResponse(
                alert.getId(), alert.getAsset().getId(), alert.getAsset().getSymbol(),
                alert.getCondition(), alert.getTargetPrice(), alert.getStatus(), alert.getCreatedAt());
    }
}
