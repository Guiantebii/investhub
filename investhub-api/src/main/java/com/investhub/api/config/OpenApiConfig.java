package com.investhub.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

	@Bean
	public OpenAPI investHubOpenApi() {
		return new OpenAPI()
				.info(new Info()
						.title("InvestHub API")
						.version("1.0.0")
						.description(
								"API REST do InvestHub — gerenciador e agregador de investimentos. "
										+ "Consolidação de carteira e transações com precisão financeira via BigDecimal."));
	}
}
