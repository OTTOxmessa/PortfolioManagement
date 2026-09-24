package com.example.portfolio.service.alert.state;

import com.example.portfolio.domain.entity.PriceAlert;
import com.example.portfolio.domain.enums.AlertStatus;
import com.example.portfolio.service.alert.AlertSubject;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

// สถานะที่ราคาตรงเงื่อนไขแล้ว แต่ยังไม่ได้แจ้งเตือนจริง — handle() คือจุดที่เรียก Observer ให้ส่งแจ้งเตือน
// (จุดที่ State pattern กับ Observer pattern ทำงานร่วมกัน)
@Component
@RequiredArgsConstructor
public class TriggeredState implements AlertState {

    private final AlertSubject alertSubject;

    @Override
    public boolean isConditionMet(PriceAlert alert, BigDecimal currentPrice) {
        return true; // ถือว่า trigger ไปแล้ว ไม่ต้องเช็คซ้ำ
    }

    @Override
    public void handle(PriceAlert alert, BigDecimal currentPrice) {
        alertSubject.notifyAll(alert, currentPrice);
        alert.setStatus(AlertStatus.NOTIFIED);
    }
}
