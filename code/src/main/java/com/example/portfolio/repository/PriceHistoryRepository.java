package com.example.portfolio.repository;

import com.example.portfolio.domain.entity.PriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PriceHistoryRepository extends JpaRepository<PriceHistory, Long> {

    List<PriceHistory> findByAssetIdAndPriceDateBetweenOrderByPriceDateAsc(
            Long assetId, LocalDate start, LocalDate end);

    Optional<PriceHistory> findTopByAssetIdOrderByPriceDateDesc(Long assetId);

    // ราคาปิดล่าสุด "ณ หรือก่อน" วันที่กำหนด — กันกรณีวันที่เลือกไม่มีข้อมูลราคา (วันหยุด/ข้อมูลยังไม่อัปเดต)
    Optional<PriceHistory> findTopByAssetIdAndPriceDateLessThanEqualOrderByPriceDateDesc(
            Long assetId, LocalDate date);
}
