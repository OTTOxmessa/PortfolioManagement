package com.example.portfolio.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "index_price_history",
        uniqueConstraints = @UniqueConstraint(name = "uq_index_price_history", columnNames = {"market_index_id", "price_date"}),
        indexes = @Index(name = "idx_index_price_history_date", columnList = "market_index_id, price_date"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IndexPriceHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "market_index_id", nullable = false)
    private MarketIndex marketIndex;

    @Column(name = "price_date", nullable = false)
    private LocalDate priceDate;

    @Column(name = "close_value", nullable = false, precision = 18, scale = 4)
    private BigDecimal closeValue;
}
