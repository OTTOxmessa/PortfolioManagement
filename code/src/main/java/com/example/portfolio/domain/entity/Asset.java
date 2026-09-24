package com.example.portfolio.domain.entity;

import com.example.portfolio.domain.enums.AssetType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "assets", indexes = {
        @Index(name = "idx_assets_symbol", columnList = "symbol", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Asset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String symbol; // เช่น PTT, AAPL, BTC

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "asset_type", nullable = false, length = 20)
    private AssetType assetType;

    @Column(length = 50)
    private String exchange; // เช่น SET, NASDAQ

    // ตั้งใจไม่ใส่ @OneToMany ย้อนกลับไปหา Holding/Transaction/PriceHistory ที่นี่
    // เพราะ Asset เป็น "reference data" ที่ถูกหลายฝั่งอ้างถึง ไม่ใช่ aggregate root
    // ต้องการข้อมูลพวกนั้นให้ query ผ่าน repository ของฝั่งนั้นแทน (เช่น HoldingRepository.findByAssetId)
}
