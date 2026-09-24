package com.example.portfolio.service.alert.state;

import com.example.portfolio.domain.entity.PriceAlert;
import com.example.portfolio.domain.enums.AlertStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

// สถานะเริ่มต้น — รอตรวจสอบว่าราคาถึงเงื่อนไขที่ตั้งไว้หรือยัง
@Component
public class PendingState implements AlertState {

    @Override
    public boolean isConditionMet(PriceAlert alert, BigDecimal currentPrice) {
        return switch (alert.getCondition()) {
            case PRICE_ABOVE -> currentPrice.compareTo(alert.getTargetPrice()) >= 0;
            case PRICE_BELOW -> currentPrice.compareTo(alert.getTargetPrice()) <= 0;
            // TODO: PCT_CHANGE ต้องมีราคาอ้างอิงตั้งต้นเก็บไว้เพิ่ม ถึงจะคำนวณ % เปลี่ยนแปลงได้จริง
            case PCT_CHANGE -> false;
        };
    }

    @Override
    public void handle(PriceAlert alert, BigDecimal currentPrice) {
        if (isConditionMet(alert, currentPrice)) {
            alert.setStatus(AlertStatus.TRIGGERED);
        }
        // ยังไม่เข้าเงื่อนไข -> คงสถานะ PENDING ต่อไป ไม่ต้องทำอะไร
    }
}
