package com.kt.icis.sa.icisapigw.filters;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.NettyWriteResponseFilter;
import org.springframework.cloud.gateway.filter.factory.rewrite.ModifyResponseBodyGatewayFilterFactory;
import org.springframework.core.Ordered;
import org.springframework.web.server.ServerWebExchange;

import com.kt.icis.sa.icisapigw.transform.ResponseBodyTransform;

import reactor.core.publisher.Mono;

public class ResponseTransformGlobalFilter implements GlobalFilter, Ordered {

	@Autowired
	private ModifyResponseBodyGatewayFilterFactory modifyResponseBodyFilter;
	
	@Autowired
	private ResponseBodyTransform responseBodyTrans;
	
	@Override
	public int getOrder() {
		return NettyWriteResponseFilter.WRITE_RESPONSE_FILTER_ORDER - 1;
	}

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		return modifyResponseBodyFilter
				.apply(new ModifyResponseBodyGatewayFilterFactory.Config().setRewriteFunction(String.class, String.class, responseBodyTrans))
				.filter(exchange, chain);
	}

}
