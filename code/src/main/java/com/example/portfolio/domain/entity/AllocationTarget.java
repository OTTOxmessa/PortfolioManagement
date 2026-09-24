package com.example.portfolio.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "allocation_targets",
        uniqueConstraints = @UniqueConstraint(name = "uq_alloc_target_portfolio_asset", columnNames = {"portfolio_id", "asset_id"}),
        indexes = @Index(name = "idx_alloc_targets_portfolio_id", columnList = "portfolio_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AllocationTarget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id", nullable = false)
    private Portfolio portfolio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id", nullable = false)
    private Asset asset;

    @Column(name = "target_percent", nullable = false, precision = 5, scale = 2)
    private BigDecimal targetPercent; // เช่น 25.00 หมายถึง 25%
}
