package com.investhub.api.integration.brapi;

import com.investhub.api.config.CacheConfig;
import java.math.BigDecimal;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class BrapiQuoteService {

	private final BrapiQuoteClient brapiQuoteClient;

	public BrapiQuoteService(BrapiQuoteClient brapiQuoteClient) {
		this.brapiQuoteClient = brapiQuoteClient;
	}

	@Cacheable(value = CacheConfig.STOCK_QUOTES_CACHE, key = "#symbol", unless = "#result == null")
	public BigDecimal getCachedRegularMarketPrice(String symbol) {
		return brapiQuoteClient.fetchRegularMarketPrice(symbol).orElse(null);
	}
}
