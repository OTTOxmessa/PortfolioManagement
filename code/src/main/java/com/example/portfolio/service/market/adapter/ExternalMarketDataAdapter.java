package com.example.portfolio.service.market.adapter;

import com.example.portfolio.service.market.MarketDataProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Map;

// Adapter pattern: แปลง response จาก API ตลาดหุ้นภายนอก (เช่น Alpha Vantage/Yahoo Finance)
// ให้เข้ากับ MarketDataProvider interface ที่ระบบเราออกแบบไว้เอง
// เปลี่ยนผู้ให้บริการ API ในอนาคต แก้แค่คลาสนี้ ไม่ต้องแก้ Service ที่เรียกใช้ MarketDataProvider เลย
@Component
public class ExternalMarketDataAdapter implements MarketDataProvider {

    private final RestTemplate restTemplate;

    @Value("${market-data.api-base-url:https://www.alphavantage.co/query}")
    private String apiBaseUrl;

    @Value("${market-data.api-key:demo}")
    private String apiKey;

    public ExternalMarketDataAdapter(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public BigDecimal getLatestPrice(String symbol) {
        // TODO: ปรับ query param / response parsing ให้ตรงกับ API จริงที่ทีมเลือกใช้
        // ตอนนี้เป็นโครงตัวอย่างที่ยังไม่ได้ทดสอบกับ API จริง เพราะยังไม่ตัดสินใจเลือกผู้ให้บริการ
        String url = String.format("%s?function=GLOBAL_QUOTE&symbol=%s&apikey=%s",
                apiBaseUrl, symbol, apiKey);

        Map<?, ?> response = restTemplate.getForObject(url, Map.class);
        if (response == null) {
            throw new IllegalStateException("ไม่ได้รับข้อมูลราคาจาก external API สำหรับ " + symbol);
        }

        // โครง response ของ Alpha Vantage: { "Global Quote": { "05. price": "123.45", ... } }
        Object quote = response.get("Global Quote");
        if (!(quote instanceof Map<?, ?> quoteMap) || quoteMap.get("05. price") == null) {
            throw new IllegalStateException("รูปแบบ response ไม่ตรงตามที่คาดไว้สำหรับ " + symbol);
        }
        return new BigDecimal(quoteMap.get("05. price").toString());
    }
}
