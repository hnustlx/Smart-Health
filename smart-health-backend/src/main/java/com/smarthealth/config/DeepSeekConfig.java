package com.smarthealth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Data
@Configuration
@ConfigurationProperties(prefix = "deepseek")
public class DeepSeekConfig {

    private String apiKey;
    private String model;
    private String url;

    @Bean
    public RestTemplate deepSeekRestTemplate() {
        return new RestTemplate();
    }
}
