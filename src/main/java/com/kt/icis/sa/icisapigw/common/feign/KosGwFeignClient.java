package com.kt.icis.sa.icisapigw.common.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.kt.icis.sa.icisapigw.common.dto.AuthToknInDs;

@FeignClient(name="kosGwClient", url="${api.kosgw.url}")
public interface KosGwFeignClient {
	@PostMapping("/kos-gw/auth/authTokn")
	public String authTokn(@RequestBody AuthToknInDs authToknInDs);
	
	@GetMapping("/system/healthz")
	public String getSystemHealth();
}
