package com.investhub.api.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

	private static final String BEARER_AUTH = "bearerAuth";

	@Bean
	public OpenAPI investHubOpenApi() {
		return new OpenAPI()
				.info(new Info()
						.title("InvestHub API")
						.version("1.0.0")
						.description(
								"API REST do InvestHub — gerenciador e agregador de investimentos. "
										+ "Consolidação de carteira e transações com precisão financeira via BigDecimal."))
				.components(new Components()
						.addSecuritySchemes(
								BEARER_AUTH,
								new SecurityScheme()
										.type(SecurityScheme.Type.HTTP)
										.scheme("bearer")
										.bearerFormat("JWT")
										.description("Token obtido em POST /auth/login")))
				.addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH));
	}
}
