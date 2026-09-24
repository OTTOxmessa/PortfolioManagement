package com.example.portfolio.service.allocation;

import com.example.portfolio.domain.entity.AllocationTarget;
import com.example.portfolio.domain.entity.Asset;
import com.example.portfolio.domain.entity.Holding;
import com.example.portfolio.domain.entity.Portfolio;
import com.example.portfolio.exception.ResourceNotFoundException;
import com.example.portfolio.repository.AllocationTargetRepository;
import com.example.portfolio.repository.AssetRepository;
import com.example.portfolio.repository.HoldingRepository;
import com.example.portfolio.repository.PortfolioRepository;
import com.example.portfolio.repository.PriceHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Service นี้ไม่ได้อยู่ใน package tree ที่ออกแบบไว้ตอนแรก แต่จำเป็นต้องมี เพราะ AllocationController
// ต้องมีจุดกลางที่เลือก AllocationStrategy มาใช้ แล้วเทียบผลกับ allocation_targets (Strategy pattern ผ่าน DIP)
// อัปเดตรอบนี้: เพิ่ม getTargets()/upsertTarget() รองรับ endpoint GET/PUT allocation-targets ของ AllocationController
@Service
@RequiredArgsConstructor
public class AllocationService {

    private final HoldingRepository holdingRepository;
    private final AllocationTargetRepository allocationTargetRepository;
    private final PriceHistoryRepository priceHistoryRepository;
    private final PortfolioRepository portfolioRepository;
    private final AssetRepository assetRepository;

    public List<AllocationComparison> compare(Long portfolioId, AllocationStrategy strategy) {
        List<Holding> holdings = holdingRepository.findByPortfolioId(portfolioId);
        List<AllocationTarget> targets = allocationTargetRepository.findByPortfolioId(portfolioId);

        Map<Long, BigDecimal> latestPrices = new HashMap<>();
        for (Holding h : holdings) {
            priceHistoryRepository.findTopByAssetIdOrderByPriceDateDesc(h.getAsset().getId())
                    .ifPresent(ph -> latestPrices.put(h.getAsset().getId(), ph.getClose()));
        }

        Map<Long, BigDecimal> currentAllocation = strategy.calculateCurrentAllocation(holdings, latestPrices);

        Map<Long, BigDecimal> targetAllocation = new HashMap<>();
        for (AllocationTarget t : targets) {
            targetAllocation.put(t.getAsset().getId(), t.getTargetPercent());
        }

        List<AllocationComparison> result = new ArrayList<>();
        for (Holding h : holdings) {
            Long assetId = h.getAsset().getId();
            BigDecimal current = currentAllocation.getOrDefault(assetId, BigDecimal.ZERO);
            BigDecimal target = targetAllocation.getOrDefault(assetId, BigDecimal.ZERO);
            result.add(new AllocationComparison(
                    assetId, h.getAsset().getSymbol(), current, target, current.subtract(target)));
        }
        return result;
    }

    public List<AllocationTarget> getTargets(Long portfolioId) {
        return allocationTargetRepository.findByPortfolioId(portfolioId);
    }

    @Transactional
    public AllocationTarget upsertTarget(Long portfolioId, Long assetId, BigDecimal targetPercent) {
        AllocationTarget target = allocationTargetRepository.findByPortfolioIdAndAssetId(portfolioId, assetId)
                .orElseGet(() -> {
                    Portfolio portfolio = portfolioRepository.findById(portfolioId)
                            .orElseThrow(() -> new ResourceNotFoundException("Portfolio not found: " + portfolioId));
                    Asset asset = assetRepository.findById(assetId)
                            .orElseThrow(() -> new ResourceNotFoundException("Asset not found: " + assetId));
                    return AllocationTarget.builder().portfolio(portfolio).asset(asset).build();
                });
        target.setTargetPercent(targetPercent);
        return allocationTargetRepository.save(target);
    }
}
