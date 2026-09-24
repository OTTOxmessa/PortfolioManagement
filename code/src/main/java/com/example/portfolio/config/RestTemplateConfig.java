package com.example.portfolio.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

// ไฟล์ใหม่ที่ไม่ได้อยู่ใน package tree เดิม — ตอนเขียน ExternalMarketDataAdapter ไว้เคยเตือนแล้วว่าต้องมี
// RestTemplate bean ก่อนแอปจะ start ได้ นี่คือจุดที่ประกาศ bean นั้นจริง
@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
