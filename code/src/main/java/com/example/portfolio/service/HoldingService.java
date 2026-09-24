package com.example.portfolio.service;

import com.example.portfolio.domain.entity.Holding;
import com.example.portfolio.domain.enums.TransactionType;

import java.math.BigDecimal;
import java.util.List;

public interface HoldingService {

    List<Holding> getByPortfolio(Long portfolioId);

    // คำนวณ holding ใหม่จาก transaction ที่เกิดขึ้น (BUY ถัวเฉลี่ยต้นทุน, SELL ลดจำนวน)
    Holding applyTransaction(Long portfolioId, Long assetId, TransactionType type,
                              BigDecimal quantity, BigDecimal price);
}
