package com.kt.icis.sa.icisapigw.logging;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.channels.Channels;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;

import com.kt.icis.sa.icisapigw.common.utils.CommonBeanUtil;
import com.kt.icis.sa.icisapigw.monitoring.MonitoringService;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

@Slf4j
public class RequestLoggingDecorator extends ServerHttpRequestDecorator {
	
	@Autowired
	private MonitoringService monitoringService;
	
	public RequestLoggingDecorator(ServerHttpRequest delegate) {
		super(delegate);
		this.monitoringService = (MonitoringService) CommonBeanUtil.getBean(MonitoringService.class);
	}
	
	@Override
	public Flux<DataBuffer> getBody() {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		return super.getBody()
				.doOnNext(dataBuffer -> {
					try {
						Channels.newChannel(baos).write(dataBuffer.asByteBuffer().asReadOnlyBuffer());
					} catch (IOException e) {
						log.error(e.getMessage());
					} finally {
						try {
							baos.close();
						} catch (IOException e) {
							log.error(e.getMessage());
						}
					}
				});
	}

}
