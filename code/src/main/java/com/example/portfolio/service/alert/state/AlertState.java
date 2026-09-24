package com.example.portfolio.service.alert.state;

import com.example.portfolio.domain.entity.PriceAlert;

import java.math.BigDecimal;

// State pattern: แต่ละสถานะของ PriceAlert รู้วิธีจัดการตัวเองว่าจะเปลี่ยนไปสถานะไหนต่อ
// แทนการเขียน if-else ซ้อนตาม status ใน PriceAlertService
public interface AlertState {

    boolean isConditionMet(PriceAlert alert, BigDecimal currentPrice);

    void handle(PriceAlert alert, BigDecimal currentPrice);
}
