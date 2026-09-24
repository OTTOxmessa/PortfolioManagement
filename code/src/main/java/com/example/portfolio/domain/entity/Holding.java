package com.example.portfolio.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "holdings",
        uniqueConstraints = @UniqueConstraint(name = "uq_holding_portfolio_asset", columnNames = {"portfolio_id", "asset_id"}),
        indexes = {
                @Index(name = "idx_holdings_portfolio_id", columnList = "portfolio_id"),
                @Index(name = "idx_holdings_asset_id", columnList = "asset_id")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Holding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Holding คือ join entity ที่ทำให้ Portfolio <-> Asset เป็นความสัมพันธ์ N:M ได้จริง
    // (พร้อม field เพิ่มเติมอย่าง quantity/avgCost ที่ M:N ธรรมดาเก็บไม่ได้)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id", nullable = false)
    private Portfolio portfolio;

    // ไม่ cascade ไปยัง Asset — ลบ holding ต้องไม่ลบ asset เพราะ asset ถูกพอร์ตอื่นอ้างถึงด้วย
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id", nullable = false)
    private Asset asset;

    @Column(nullable = false, precision = 18, scale = 6)
    private BigDecimal quantity; // scale 6 รองรับเศษหุ้น/คริปโตทศนิยมเยอะ

    @Column(name = "avg_cost", nullable = false, precision = 18, scale = 4)
    private BigDecimal avgCost;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    protected void onChange() {
        this.updatedAt = LocalDateTime.now();
    }
}
