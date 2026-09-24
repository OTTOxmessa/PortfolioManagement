package com.example.portfolio.service.rebalance;

import com.example.portfolio.domain.entity.AllocationTarget;
import com.example.portfolio.domain.entity.Holding;
import com.example.portfolio.domain.entity.Portfolio;
import com.example.portfolio.domain.entity.RebalanceLog;
import com.example.portfolio.domain.enums.RebalanceMethod;
import com.example.portfolio.domain.enums.TransactionType;
import com.example.portfolio.exception.ResourceNotFoundException;
import com.example.portfolio.repository.AllocationTargetRepository;
import com.example.portfolio.repository.HoldingRepository;
import com.example.portfolio.repository.PortfolioRepository;
import com.example.portfolio.repository.PriceHistoryRepository;
import com.example.portfolio.repository.RebalanceLogRepository;
import com.example.portfolio.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// ผสาน 3 pattern: Strategy (เลือกวิธีคำนวณ), Chain of Responsibility (ตรวจก่อนทำจริง), Command (ซื้อขายจริง)
@Service
@RequiredArgsConstructor
public class RebalanceService {

    private final PortfolioRepository portfolioRepository;
    private final HoldingRepository holdingRepository;
    private final AllocationTargetRepository allocationTargetRepository;
    private final PriceHistoryRepository priceHistoryRepository;
    private final RebalanceLogRepository rebalanceLogRepository;
    private final TransactionService transactionService;

    public List<TradeOrder> preview(Long portfolioId, RebalanceStrategy strategy) {
        List<Holding> holdings = holdingRepository.findByPortfolioId(portfolioId);
        List<AllocationTarget> targets = allocationTargetRepository.findByPortfolioId(portfolioId);
        return strategy.computeTrades(holdings, targets, latestPricesOf(holdings));
    }

    @Transactional
    public RebalanceLog execute(Long portfolioId, RebalanceStrategy strategy, RebalanceMethod method) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new ResourceNotFoundException("Portfolio not found: " + portfolioId));
        List<TradeOrder> trades = preview(portfolioId, strategy);

        RebalanceValidationHandler chain = new MinimumHoldingsValidationHandler();
        chain.validate(portfolio, trades);

        // ขายก่อนซื้อ — ให้ลำดับตรงกับการรีบาลานซ์จริง (ได้เงินจากการขายมาใช้ซื้อ)
        List<TradeOrder> ordered = new ArrayList<>();
        trades.stream().filter(t -> t.type() == TransactionType.SELL).forEach(ordered::add);
        trades.stream().filter(t -> t.type() == TransactionType.BUY).forEach(ordered::add);

        for (TradeOrder order : ordered) {
            RebalanceCommand command = switch (order.type()) {
                case BUY -> new BuyCommand(transactionService, portfolioId, order.assetId(),
                        order.quantity(), order.estimatedPrice());
                case SELL -> new SellCommand(transactionService, portfolioId, order.assetId(),
                        order.quantity(), order.estimatedPrice());
                default -> throw new IllegalStateException("RebalanceService รองรับแค่ BUY/SELL");
            };
            command.execute();
        }

        RebalanceLog log = RebalanceLog.builder()
                .portfolio(portfolio)
                .method(method)
                .details(buildDetailsJson(ordered))
                .build();
        return rebalanceLogRepository.save(log);
    }

    private Map<Long, BigDecimal> latestPricesOf(List<Holding> holdings) {
        Map<Long, BigDecimal> prices = new HashMap<>();
        for (Holding h : holdings) {
            priceHistoryRepository.findTopByAssetIdOrderByPriceDateDesc(h.getAsset().getId())
                    .ifPresent(ph -> prices.put(h.getAsset().getId(), ph.getClose()));
        }
        return prices;
    }

    private String buildDetailsJson(List<TradeOrder> trades) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < trades.size(); i++) {
            TradeOrder t = trades.get(i);
            sb.append(String.format(
                    "{\"symbol\":\"%s\",\"type\":\"%s\",\"quantity\":%s,\"price\":%s}",
                    t.symbol(), t.type(), t.quantity().toPlainString(), t.estimatedPrice().toPlainString()));
            if (i < trades.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }
}
