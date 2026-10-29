package com.investhub.api.integration.brapi;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.investhub.api.AbstractPostgresIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

class BrapiQuoteServiceCacheIntegrationTest extends AbstractPostgresIntegrationTest {

	@RegisterExtension
	static WireMockExtension wireMock = WireMockExtension.newInstance().build();

	@DynamicPropertySource
	static void registerBrapiBaseUrl(DynamicPropertyRegistry registry) {
		registry.add("investhub.brapi.base-url", wireMock::baseUrl);
	}

	@Autowired
	private BrapiQuoteService brapiQuoteService;

	@BeforeEach
	void stubBrapi() {
		wireMock.resetAll();
		wireMock.stubFor(get(urlPathEqualTo("/quote/PETR4"))
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Type", "application/json")
						.withBody(
								"""
								{"results":[{"symbol":"PETR4","regularMarketPrice":11.11}]}
								""")));
	}

	@Test
	void shouldCacheSuccessfulQuotesToAvoidRepeatedExternalCalls() {
		var first = brapiQuoteService.getCachedRegularMarketPrice("PETR4");
		var second = brapiQuoteService.getCachedRegularMarketPrice("PETR4");

		assertThat(first).isEqualByComparingTo("11.11");
		assertThat(second).isEqualByComparingTo("11.11");
		wireMock.verify(1, getRequestedFor(urlPathEqualTo("/quote/PETR4")));
	}
}
