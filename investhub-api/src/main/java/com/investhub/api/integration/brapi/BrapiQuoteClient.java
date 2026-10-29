package com.investhub.api.integration.brapi;

import com.investhub.api.integration.brapi.dto.BrapiQuoteResponse;
import java.math.BigDecimal;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Component
public class BrapiQuoteClient {

	private static final Logger log = LoggerFactory.getLogger(BrapiQuoteClient.class);

	private final RestClient brapiRestClient;

	public BrapiQuoteClient(RestClient brapiRestClient) {
		this.brapiRestClient = brapiRestClient;
	}

	public Optional<BigDecimal> fetchRegularMarketPrice(String ticker) {
		String normalizedTicker = ticker.trim().toUpperCase();
		try {
			BrapiQuoteResponse response = brapiRestClient
					.get()
					.uri("/quote/{ticker}", normalizedTicker)
					.retrieve()
					.body(BrapiQuoteResponse.class);

			if (response == null || response.results() == null || response.results().isEmpty()) {
				log.warn("Brapi retornou payload vazio para ticker {}", normalizedTicker);
				return Optional.empty();
			}

			Double price = response.results().get(0).regularMarketPrice();
			if (price == null || price <= 0) {
				log.warn("Brapi retornou preço inválido para ticker {}: {}", normalizedTicker, price);
				return Optional.empty();
			}

			return Optional.of(BigDecimal.valueOf(price));
		} catch (RestClientResponseException ex) {
			log.error(
					"Erro HTTP ao consultar Brapi para {}: status={} body={}",
					normalizedTicker,
					ex.getStatusCode().value(),
					ex.getResponseBodyAsString());
			return Optional.empty();
		} catch (RestClientException ex) {
			log.error("Falha de comunicação com Brapi para ticker {}: {}", normalizedTicker, ex.getMessage());
			return Optional.empty();
		}
	}
}
