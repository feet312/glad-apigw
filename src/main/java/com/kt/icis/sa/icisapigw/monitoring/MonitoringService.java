package com.kt.icis.sa.icisapigw.monitoring;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MonitoringService {

	@Autowired
	private MonOpenFeign mon;
	
	@Autowired
	Environment env;
	
	@Async
	public void sendReqAsync(String globalNo, String reqBody) {
		try {
			mon.bmonReqSendXml(reqBody, env.getProperty("app-info.log-point"));
		} catch (Exception e) {
			log.error(e.getMessage());
		}
	}
	
	@Async
	public void sendResAsync(String globalNo, String reqBody) {
		try {
			mon.bmonResSendXml(reqBody, env.getProperty("app-info.log-point"));
		} catch (Exception e) {
			log.error(e.getMessage());
		}
	}
}
