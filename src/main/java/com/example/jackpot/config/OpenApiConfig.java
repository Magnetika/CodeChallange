package com.example.jackpot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;

/**
 * OpenAPI/Swagger configuration for API documentation.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI jackpotOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Jackpot Service API")
                        .description("API for managing jackpots, placing bets, and tracking wins")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Jackpot Service")
                                .email("support@jackpot.example.com")));
    }
}
