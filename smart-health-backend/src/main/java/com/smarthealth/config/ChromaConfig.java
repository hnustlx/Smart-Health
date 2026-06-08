package com.smarthealth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "chroma")
public class ChromaConfig {

    private String url;
    private String collectionName;
}
