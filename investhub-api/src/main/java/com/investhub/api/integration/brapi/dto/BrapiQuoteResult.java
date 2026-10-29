package com.investhub.api.integration.brapi.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record BrapiQuoteResult(String symbol, Double regularMarketPrice) {}
