package com.example.portfolio.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI portfolioSystemOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Portfolio Management System API")
                        .description("CP353002 Principles of Software Design and Development — Investment Portfolio Management System")
                        .version("v1.0"));
    }
}
