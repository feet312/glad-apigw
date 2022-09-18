package com.kt.icis.sa.icisapigw.monitoring;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name="monitoring", url="${api.mon.url}")
public interface MonOpenFeign {

	@PostMapping("/monitoring")
	public void monoitoring(String data);
	
	@RequestMapping(method=RequestMethod.POST, value="/bmon/reqSendXml", consumes="text/plain;charset=UTF-8")
	public void bmonReqSendXml(@RequestBody String reqBody, @RequestParam String logPoint);
	
	@RequestMapping(method=RequestMethod.POST, value="/bmon/resSendXml", consumes="text/plain;charset=UTF-8")
	public void bmonResSendXml(@RequestBody String reqBody, @RequestParam String logPoint);
}
