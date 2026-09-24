package com.example.portfolio.scheduler;

import com.example.portfolio.domain.entity.PriceAlert;
import com.example.portfolio.domain.enums.AlertStatus;
import com.example.portfolio.repository.PriceAlertRepository;
import com.example.portfolio.service.alert.PriceAlertService;
import com.example.portfolio.service.market.MarketDataProvider;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PriceAlertScheduler {

    private static final Logger log = LoggerFactory.getLogger(PriceAlertScheduler.class);

    private final PriceAlertRepository priceAlertRepository;
    private final PriceAlertService priceAlertService;
    private final MarketDataProvider marketDataProvider;

    // ความถี่ตั้งผ่าน property alert.check-interval-ms (default 5 นาที, dev ตั้งเป็น 30 วินาที)
    // @Transactional จำเป็น: scheduler ไม่มี request/session เหมือน controller
    // ถ้าไม่มี การอ่าน alert.getAsset().getSymbol() (lazy) จะเกิด LazyInitializationException
    @Scheduled(fixedRateString = "${alert.check-interval-ms:300000}", initialDelay = 10000)
    @Transactional
    public void checkPendingAlerts() {
        List<PriceAlert> pendingAlerts = priceAlertRepository.findByStatus(AlertStatus.PENDING);
        for (PriceAlert alert : pendingAlerts) {
            try {
                BigDecimal currentPrice = marketDataProvider.getLatestPrice(alert.getAsset().getSymbol());
                priceAlertService.checkAndNotify(alert, currentPrice);
            } catch (Exception e) {
                log.error("ตรวจสอบ alert #{} ไม่สำเร็จ: {}", alert.getId(), e.getMessage());
            }
        }
    }
}
