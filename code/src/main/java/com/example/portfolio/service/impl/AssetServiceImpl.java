package com.example.portfolio.service.impl;

import com.example.portfolio.domain.entity.Asset;
import com.example.portfolio.domain.entity.PriceHistory;
import com.example.portfolio.domain.enums.AssetType;
import com.example.portfolio.exception.ResourceNotFoundException;
import com.example.portfolio.repository.AssetRepository;
import com.example.portfolio.repository.PriceHistoryRepository;
import com.example.portfolio.service.AssetService;
import com.example.portfolio.service.market.SymbolDirectory;
import com.example.portfolio.service.market.SymbolDirectory.SymbolInfo;
import com.example.portfolio.service.market.SymbolSuggestion;
import com.example.portfolio.service.market.SyntheticPriceHistoryGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AssetServiceImpl implements AssetService {

    private final AssetRepository assetRepository;
    private final PriceHistoryRepository priceHistoryRepository;
    private final SymbolDirectory symbolDirectory;
    private final SyntheticPriceHistoryGenerator priceHistoryGenerator;

    @Override
    public List<Asset> getAll() {
        return assetRepository.findAll();
    }

    @Override
    public Asset getById(Long id) {
        return assetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found: " + id));
    }

    @Override
    @Transactional
    public Asset create(Asset asset) {
        asset.setSymbol(normalize(asset.getSymbol()));
        if (assetRepository.existsBySymbol(asset.getSymbol())) {
            throw new IllegalStateException("มีสินทรัพย์ symbol " + asset.getSymbol() + " อยู่แล้ว");
        }
        Asset saved = assetRepository.save(asset);
        priceHistoryGenerator.backfillIfMissing(saved, null);
        return saved;
    }

    @Override
    @Transactional
    public Asset ensure(String symbol, String name, AssetType assetType, String exchange, BigDecimal referencePrice) {
        String normalized = normalize(symbol);
        Asset asset = assetRepository.findBySymbol(normalized).orElseGet(() -> {
            Optional<SymbolInfo> info = symbolDirectory.findBySymbol(normalized);
            String resolvedName = hasText(name) ? name.trim() : info.map(SymbolInfo::name).orElse(null);
            if (resolvedName == null) {
                throw new IllegalArgumentException(
                        "ไม่พบ " + normalized + " ในรายชื่อหุ้นอ้างอิง — กรุณาเพิ่มในหน้าสินทรัพย์พร้อมระบุชื่อ");
            }
            AssetType resolvedType = assetType != null ? assetType
                    : info.map(SymbolInfo::assetType).orElse(AssetType.STOCK);
            String resolvedExchange = hasText(exchange) ? exchange.trim()
                    : info.map(SymbolInfo::exchange).orElse(null);
            return assetRepository.save(Asset.builder()
                    .symbol(normalized)
                    .name(resolvedName)
                    .assetType(resolvedType)
                    .exchange(resolvedExchange)
                    .build());
        });
        // สินทรัพย์เก่าที่ยังไม่มีราคา ก็ได้ราคาจำลองด้วย (idempotent: มีข้อมูลแล้วจะไม่ทำอะไร)
        priceHistoryGenerator.backfillIfMissing(asset, referencePrice);
        return asset;
    }

    @Override
    public List<SymbolSuggestion> searchSymbols(String query, int limit) {
        String q = query == null ? "" : query.trim().toUpperCase(Locale.ROOT);
        if (q.isEmpty()) {
            return List.of();
        }

        record Ranked(int rank, SymbolSuggestion suggestion) {
        }

        List<Ranked> ranked = new ArrayList<>();
        Set<String> inSystem = new HashSet<>();

        // 1) หุ้นที่อยู่ในระบบแล้ว (ตาราง assets มีขนาดเล็ก จึงกรองในหน่วยความจำได้)
        for (Asset a : assetRepository.findAll()) {
            int rank = SymbolDirectory.matchRank(a.getSymbol(), a.getName(), q);
            if (rank < SymbolDirectory.NO_MATCH) {
                ranked.add(new Ranked(rank, new SymbolSuggestion(
                        a.getSymbol(), a.getName(), a.getAssetType(), a.getExchange(), a.getId())));
                inSystem.add(a.getSymbol());
            }
        }
        // 2) รายชื่ออ้างอิงที่ยังไม่อยู่ในระบบ
        for (SymbolInfo s : symbolDirectory.search(q, limit * 2)) {
            if (!inSystem.contains(s.symbol())) {
                ranked.add(new Ranked(SymbolDirectory.matchRank(s.symbol(), s.name(), q),
                        new SymbolSuggestion(s.symbol(), s.name(), s.assetType(), s.exchange(), null)));
            }
        }

        return ranked.stream()
                .sorted(Comparator.comparingInt(Ranked::rank)
                        .thenComparingInt((Ranked r) -> r.suggestion().symbol().length())
                        .thenComparing(r -> r.suggestion().symbol()))
                .limit(limit)
                .map(Ranked::suggestion)
                .toList();
    }

    @Override
    public List<PriceHistory> getPriceHistory(Long assetId, LocalDate from, LocalDate to) {
        getById(assetId);
        return priceHistoryRepository.findByAssetIdAndPriceDateBetweenOrderByPriceDateAsc(assetId, from, to);
    }

    private static String normalize(String symbol) {
        return symbol.trim().toUpperCase(Locale.ROOT);
    }

    private static boolean hasText(String s) {
        return s != null && !s.isBlank();
    }
}
