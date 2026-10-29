package com.investhub.api.integration.brapi;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.web.client.RestClient;

class BrapiQuoteClientWireMockTest {

	@RegisterExtension
	static WireMockExtension wireMock = WireMockExtension.newInstance().build();

	private BrapiQuoteClient client;

	@BeforeEach
	void setUp() {
		client = new BrapiQuoteClient(RestClient.builder().baseUrl(wireMock.baseUrl()).build());
	}

	@Test
	void shouldReturnPriceWhenBrapiRespondsSuccessfully() {
		wireMock.stubFor(get(urlPathEqualTo("/quote/PETR4"))
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Type", "application/json")
						.withBody(
								"""
								{"results":[{"symbol":"PETR4","regularMarketPrice":52.41}]}
								""")));

		Optional<BigDecimal> price = client.fetchRegularMarketPrice("petr4");

		assertThat(price).contains(new BigDecimal("52.41"));
	}

	@Test
	void shouldReturnEmptyWhenBrapiReturnsHttp500() {
		wireMock.stubFor(get(urlPathEqualTo("/quote/PETR4")).willReturn(aResponse().withStatus(500)));

		Optional<BigDecimal> price = client.fetchRegularMarketPrice("PETR4");

		assertThat(price).isEmpty();
	}

	@Test
	void shouldReturnEmptyWhenBrapiReturnsHttp404() {
		wireMock.stubFor(get(urlPathEqualTo("/quote/INVALID")).willReturn(aResponse().withStatus(404)));

		Optional<BigDecimal> price = client.fetchRegularMarketPrice("INVALID");

		assertThat(price).isEmpty();
	}

	@Test
	void shouldReturnEmptyWhenResultsPayloadIsEmpty() {
		wireMock.stubFor(get(urlPathEqualTo("/quote/XYZW11"))
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Type", "application/json")
						.withBody("{\"results\":[]}")));

		Optional<BigDecimal> price = client.fetchRegularMarketPrice("XYZW11");

		assertThat(price).isEmpty();
	}

	@Test
	void shouldNormalizeTickerToUpperCaseInRequestPath() {
		wireMock.stubFor(get(urlPathEqualTo("/quote/PETR4"))
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Type", "application/json")
						.withBody(
								"""
								{"results":[{"symbol":"PETR4","regularMarketPrice":10.0}]}
								""")));

		client.fetchRegularMarketPrice("  petr4 ");

		wireMock.verify(getRequestedFor(urlPathEqualTo("/quote/PETR4")));
	}
}
