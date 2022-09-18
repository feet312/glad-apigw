package com.kt.icis.sa.icisapigw.filters;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.RouteToRequestUrlFilter;
import org.springframework.cloud.gateway.filter.factory.rewrite.ModifyRequestBodyGatewayFilterFactory;
import org.springframework.core.Ordered;
import org.springframework.web.server.ServerWebExchange;

import com.kt.icis.sa.icisapigw.transform.RequestBodyTransform;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
public class RequestTransformGlobalFilter implements GlobalFilter, Ordered {
	
	@Autowired
	private ModifyRequestBodyGatewayFilterFactory modifyRequestBodyFilter;
	
	@Autowired
	private RequestBodyTransform requestBodyTransform;
	
	@Override
	public int getOrder() {
		return RouteToRequestUrlFilter.ROUTE_TO_URL_FILTER_ORDER + 1;
	}

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		log.debug("xml 요청 처리");
		return modifyRequestBodyFilter.apply(new ModifyRequestBodyGatewayFilterFactory.Config()
				.setRewriteFunction(String.class, String.class, requestBodyTransform)).filter(exchange, chain);
	}

}
