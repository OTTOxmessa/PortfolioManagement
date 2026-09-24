package com.example.portfolio.service.rebalance;

import com.example.portfolio.domain.entity.AllocationTarget;
import com.example.portfolio.domain.entity.Holding;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

// Strategy pattern: สลับวิธีตัดสินใจว่าจะ rebalance เมื่อไหร่/แค่ไหน
public interface RebalanceStrategy {

    // เทียบ holding ปัจจุบันกับ target แล้วคืนรายการ TradeOrder ที่ควรทำเพื่อกลับไปตาม target
    List<TradeOrder> computeTrades(List<Holding> holdings, List<AllocationTarget> targets,
                                    Map<Long, BigDecimal> latestPrices);
}
