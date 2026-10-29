package com.investhub.api.controller;

import com.investhub.api.dto.response.PortfolioSummaryResponse;
import com.investhub.api.service.PortfolioService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/portfolio")
public class PortfolioController {

	private final PortfolioService portfolioService;

	public PortfolioController(PortfolioService portfolioService) {
		this.portfolioService = portfolioService;
	}

	@GetMapping("/summary")
	public PortfolioSummaryResponse summary() {
		return portfolioService.getSummary();
	}
}
