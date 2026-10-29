package com.investhub.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "investhub.brapi")
public record BrapiProperties(String baseUrl, int connectTimeoutMs, int readTimeoutMs) {}
