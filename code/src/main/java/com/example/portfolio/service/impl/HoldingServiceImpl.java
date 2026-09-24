package com.example.portfolio.service.impl;

import com.example.portfolio.domain.entity.Asset;
import com.example.portfolio.domain.entity.Holding;
import com.example.portfolio.domain.entity.Portfolio;
import com.example.portfolio.domain.enums.TransactionType;
import com.example.portfolio.exception.ResourceNotFoundException;
import com.example.portfolio.repository.AssetRepository;
import com.example.portfolio.repository.HoldingRepository;
import com.example.portfolio.repository.PortfolioRepository;
import com.example.portfolio.service.HoldingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HoldingServiceImpl implements HoldingService {

    private final HoldingRepository holdingRepository;
    private final PortfolioRepository portfolioRepository;
    private final AssetRepository assetRepository;

    @Override
    public List<Holding> getByPortfolio(Long portfolioId) {
        return holdingRepository.findByPortfolioId(portfolioId);
    }

    @Override
    @Transactional
    public Holding applyTransaction(Long portfolioId, Long assetId, TransactionType type,
                                     BigDecimal quantity, BigDecimal price) {

        Holding holding = holdingRepository.findByPortfolioIdAndAssetId(portfolioId, assetId)
                .orElseGet(() -> createEmptyHolding(portfolioId, assetId));

        switch (type) {
            case BUY -> applyBuy(holding, quantity, price);
            case SELL -> applySell(holding, quantity);
            default -> throw new IllegalArgumentException(
                    "HoldingService ไม่จัดการ transaction type นี้: " + type); // DIVIDEND/DEPOSIT/WITHDRAWAL ไม่กระทบ holding โดยตรง
        }

        return holdingRepository.save(holding);
    }

    private Holding createEmptyHolding(Long portfolioId, Long assetId) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new ResourceNotFoundException("Portfolio not found: " + portfolioId));
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found: " + assetId));

        return Holding.builder()
                .portfolio(portfolio)
                .asset(asset)
                .quantity(BigDecimal.ZERO)
                .avgCost(BigDecimal.ZERO)
                .build();
    }

    // ถัวเฉลี่ยต้นทุนแบบ weighted average: avgCost ใหม่ถ่วงน้ำหนักด้วยจำนวนหุ้นเดิม + ใหม่
    private void applyBuy(Holding holding, BigDecimal quantity, BigDecimal price) {
        BigDecimal oldQty = holding.getQuantity();
        BigDecimal oldCostTotal = oldQty.multiply(holding.getAvgCost());
        BigDecimal newCostTotal = quantity.multiply(price);
        BigDecimal newQty = oldQty.add(quantity);

        BigDecimal newAvgCost = newQty.compareTo(BigDecimal.ZERO) == 0
                ? BigDecimal.ZERO
                : oldCostTotal.add(newCostTotal).divide(newQty, 4, RoundingMode.HALF_UP);

        holding.setQuantity(newQty);
        holding.setAvgCost(newAvgCost);
    }

    private void applySell(Holding holding, BigDecimal quantity) {
        BigDecimal newQty = holding.getQuantity().subtract(quantity);
        if (newQty.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalStateException("ขายเกินจำนวนที่ถืออยู่ (มี " + holding.getQuantity() + ")");
        }
        holding.setQuantity(newQty); // avgCost ไม่เปลี่ยนตอนขาย ตาม convention ทั่วไป
    }
}
