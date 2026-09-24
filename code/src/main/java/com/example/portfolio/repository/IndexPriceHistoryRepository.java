package com.example.portfolio.repository;

import com.example.portfolio.domain.entity.IndexPriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface IndexPriceHistoryRepository extends JpaRepository<IndexPriceHistory, Long> {

    List<IndexPriceHistory> findByMarketIndexIdAndPriceDateBetweenOrderByPriceDateAsc(
            Long marketIndexId, LocalDate start, LocalDate end);

    Optional<IndexPriceHistory> findTopByMarketIndexIdAndPriceDateLessThanEqualOrderByPriceDateDesc(
            Long marketIndexId, LocalDate date);
}
