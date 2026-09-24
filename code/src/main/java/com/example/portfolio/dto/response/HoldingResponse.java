package com.example.portfolio.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record HoldingResponse(Long assetId, String symbol, BigDecimal quantity, BigDecimal avgCost,
                               LocalDateTime updatedAt) {
}
