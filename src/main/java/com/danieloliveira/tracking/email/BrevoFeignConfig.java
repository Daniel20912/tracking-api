package com.danieloliveira.tracking.email;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BrevoFeignConfig {

    @Value("${brevo.api.key}")
    private String apiKey;

    @Bean
    public RequestInterceptor brevoRequestInterceptor() {
        return requestTemplate -> {
            requestTemplate.header("api-key", apiKey);
            requestTemplate.header("Content-Type", "application/json");
            requestTemplate.header("accept", "application/json");
        };
    }
}
