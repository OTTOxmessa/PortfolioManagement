package com.example.portfolio.mapper;

import com.example.portfolio.domain.entity.Holding;
import com.example.portfolio.dto.response.HoldingResponse;

public class HoldingMapper {

    private HoldingMapper() {
    }

    public static HoldingResponse toResponse(Holding holding) {
        return new HoldingResponse(
                holding.getAsset().getId(), holding.getAsset().getSymbol(),
                holding.getQuantity(), holding.getAvgCost(), holding.getUpdatedAt());
    }
}
