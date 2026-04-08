package com.danieloliveira.tracking.trackingClient;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {

    @Value("${config.api.key}")
    private String apiKey;

    @Bean
    public RequestInterceptor requestInterceptor() {
        return template ->
                template.header("Authorization", "Bearer " + apiKey);
    }
}
