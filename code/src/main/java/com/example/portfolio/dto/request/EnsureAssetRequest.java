package com.example.portfolio.dto.request;

import com.example.portfolio.domain.enums.AssetType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

// ใช้ "หา หรือ สร้าง" สินทรัพย์จาก symbol — name/assetType/exchange ไม่บังคับถ้า symbol อยู่ในรายชื่ออ้างอิง
public record EnsureAssetRequest(
        @NotBlank String symbol,
        String name,
        AssetType assetType,
        String exchange,
        @DecimalMin(value = "0", inclusive = false) BigDecimal referencePrice
) {
}
