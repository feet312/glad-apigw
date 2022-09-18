package com.kt.icis.sa.icisapigw.common;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SystemEndpoint {

	@GetMapping("/sa/system/healthz")
	public String getSystemHealth() {
		return "Service Healthy";
	}
}
