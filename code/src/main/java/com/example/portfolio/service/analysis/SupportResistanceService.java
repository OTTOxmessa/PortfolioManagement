package com.example.portfolio.service.analysis;

import com.example.portfolio.domain.entity.PriceHistory;
import com.example.portfolio.repository.PriceHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

// เพิ่มเข้ามาเหมือน AllocationService — จุดกลางที่ PriceAnalysisController เรียกใช้
// ดึง price_history จริงจาก DB แล้วส่งให้ strategy ที่เลือกไว้คำนวณ (DIP ผ่าน SupportResistanceStrategy)
@Service
@RequiredArgsConstructor
public class SupportResistanceService {

    private final PriceHistoryRepository priceHistoryRepository;

    public SupportResistanceLevels calculate(Long assetId, LocalDate from, LocalDate to,
                                              SupportResistanceStrategy strategy) {
        List<PriceHistory> history = priceHistoryRepository
                .findByAssetIdAndPriceDateBetweenOrderByPriceDateAsc(assetId, from, to);

        if (history.isEmpty()) {
            throw new IllegalStateException("ไม่มีข้อมูลราคาของ asset " + assetId + " ในช่วงเวลาที่ระบุ");
        }
        return strategy.calculate(history);
    }
}
