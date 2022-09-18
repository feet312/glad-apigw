package com.kt.icis.sa.icisapigw.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.kt.icis.sa.icisapigw.filters.LoggingWebFilter;

@Configuration
public class GatewayConfig {

	@Bean
	public LoggingWebFilter loggingWebFilter() {
		return new LoggingWebFilter();
	}
}
