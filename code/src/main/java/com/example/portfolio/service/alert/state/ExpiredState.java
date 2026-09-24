package com.example.portfolio.service.alert.state;

import com.example.portfolio.domain.entity.PriceAlert;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

// สถานะสุดท้าย (terminal) — alert หมดอายุแล้ว ไม่ต้องตรวจสอบอะไรต่อ
// หมายเหตุ: PriceAlert entity ปัจจุบันยังไม่มีฟิลด์ expiresAt ดังนั้นตอนนี้การเข้าสถานะนี้ต้องสั่งด้วยมือ (manual cancel/expire)
// ถ้าต้องการให้ระบบเปลี่ยนเป็น EXPIRED เองตามเวลา ต้องเพิ่มฟิลด์วันหมดอายุใน PriceAlert ก่อน
@Component
public class ExpiredState implements AlertState {

    @Override
    public boolean isConditionMet(PriceAlert alert, BigDecimal currentPrice) {
        return false;
    }

    @Override
    public void handle(PriceAlert alert, BigDecimal currentPrice) {
        // no-op — terminal state ไม่ทำอะไรต่อแล้ว
    }
}
