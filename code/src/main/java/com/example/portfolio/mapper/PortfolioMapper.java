package com.example.portfolio.mapper;

import com.example.portfolio.domain.entity.Portfolio;
import com.example.portfolio.dto.response.PortfolioResponse;

public class PortfolioMapper {

    private PortfolioMapper() {
    }

    public static PortfolioResponse toResponse(Portfolio portfolio) {
        return new PortfolioResponse(
                portfolio.getId(), portfolio.getName(), portfolio.getBaseCurrency(), portfolio.getCreatedAt());
    }
}
