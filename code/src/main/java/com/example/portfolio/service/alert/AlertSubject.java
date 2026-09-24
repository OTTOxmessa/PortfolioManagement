package com.example.portfolio.service.alert;

import com.example.portfolio.domain.entity.PriceAlert;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

// Subject ฝั่ง Observer pattern — Spring inject ทุก bean ที่ implement AlertObserver มาเป็น List ให้อัตโนมัติ
// เพิ่ม notifier ใหม่ (เช่น SMS, LINE Notify) แค่สร้างคลาส implement AlertObserver ใหม่ ไม่ต้องแก้ไฟล์นี้เลย (OCP)
@Component
@RequiredArgsConstructor
public class AlertSubject {

    private final List<AlertObserver> observers;

    public void notifyAll(PriceAlert alert, BigDecimal currentPrice) {
        for (AlertObserver observer : observers) {
            observer.onAlertTriggered(alert, currentPrice);
        }
    }
}
