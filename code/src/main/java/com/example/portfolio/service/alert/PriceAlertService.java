package com.example.portfolio.service.alert;

import com.example.portfolio.domain.entity.Asset;
import com.example.portfolio.domain.entity.Portfolio;
import com.example.portfolio.domain.entity.PriceAlert;
import com.example.portfolio.domain.enums.AlertStatus;
import com.example.portfolio.exception.ResourceNotFoundException;
import com.example.portfolio.repository.AssetRepository;
import com.example.portfolio.repository.PortfolioRepository;
import com.example.portfolio.repository.PriceAlertRepository;
import com.example.portfolio.service.alert.state.PendingState;
import com.example.portfolio.service.alert.state.TriggeredState;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

// จุดกลางของฟีเจอร์แจ้งเตือน: ผสาน State pattern (เปลี่ยนสถานะ) กับ Observer pattern (แจ้งเตือน)
@Service
@RequiredArgsConstructor
public class PriceAlertService {

    private final PriceAlertRepository priceAlertRepository;
    private final PortfolioRepository portfolioRepository;
    private final AssetRepository assetRepository;
    private final PendingState pendingState;
    private final TriggeredState triggeredState;

    @Transactional
    public PriceAlert create(Long portfolioId, Long assetId, PriceAlert alert) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new ResourceNotFoundException("Portfolio not found: " + portfolioId));
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found: " + assetId));

        alert.setPortfolio(portfolio);
        alert.setAsset(asset);
        alert.setStatus(AlertStatus.PENDING);
        return priceAlertRepository.save(alert);
    }

    public List<PriceAlert> getByPortfolio(Long portfolioId) {
        return priceAlertRepository.findByPortfolioId(portfolioId);
    }

    @Transactional
    public void delete(Long portfolioId, Long alertId) {
        PriceAlert alert = priceAlertRepository.findById(alertId)
                .filter(a -> a.getPortfolio().getId().equals(portfolioId))
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found: " + alertId));
        priceAlertRepository.delete(alert);
    }

    // เรียกโดย PriceAlertScheduler ทุกรอบ
    @Transactional
    public void checkAndNotify(PriceAlert alert, BigDecimal currentPrice) {
        if (alert.getStatus() == AlertStatus.PENDING) {
            pendingState.handle(alert, currentPrice);
            if (alert.getStatus() == AlertStatus.TRIGGERED) {
                triggeredState.handle(alert, currentPrice);
            }
            priceAlertRepository.save(alert);
        }
    }
}
