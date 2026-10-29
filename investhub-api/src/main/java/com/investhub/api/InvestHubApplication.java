package com.investhub.api;

import com.investhub.api.config.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
public class InvestHubApplication {

	public static void main(String[] args) {
		SpringApplication.run(InvestHubApplication.class, args);
	}

}
