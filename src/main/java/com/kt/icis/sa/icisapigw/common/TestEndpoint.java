package com.kt.icis.sa.icisapigw.common;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestEndpoint {

	@GetMapping("/ppon/test/retvByPage")
	public String getTestResponse() {
		// Test 전문인데 너무 길어서 다 옮기지 않음. 
		return "{\"service_response\":{\"commonHeader\":{\"appName\":\"NBSS_CST\",\"svcName\":\"/ppon/ex/retvById\",\"fnName\":\"service\",\"fnCd\":\"\"}}}";
	}
}
