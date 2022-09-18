package com.kt.icis.sa.icisapigw.route;

import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.kt.icis.sa.icisapigw.filters.AuthGatewayFilterFactory;
import com.kt.icis.sa.icisapigw.filters.ChannelGatewayFilterFactory;
import com.kt.icis.sa.icisapigw.filters.RequestTransformGlobalFilter;
import com.kt.icis.sa.icisapigw.filters.ResponseTransformGlobalFilter;
import com.kt.icis.sa.icisapigw.filters.ServiceGatewayFilterFactory;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class IcisRouterLocator {

	private final AuthGatewayFilterFactory authGatewayFilterFactory;
	private final ChannelGatewayFilterFactory channelGatewayFilterFactory;
	private final ServiceGatewayFilterFactory serviceGatewayFilterFactory;
	
	@Bean
	public RouteLocator customRouterLocator(RouteLocatorBuilder builder) {
		return builder.routes()
				.route("apigw", r -> r.path("/sa/xml/**")
						.filters(f -> f.filter(authGatewayFilterFactory.apply(new AuthGatewayFilterFactory.Config()))
							.filter(channelGatewayFilterFactory.apply(new ChannelGatewayFilterFactory.Config()))
							.filter(serviceGatewayFilterFactory.apply(new ServiceGatewayFilterFactory.Config()))
							.removeRequestHeader("Content-Type")
							.addRequestHeader("Content-type", "application/json")
						)
						.uri("http://localhost:8088")
				)
				.route("apigw", r -> r.path("/order/xml/**")
						.filters(f -> f.filter(authGatewayFilterFactory.apply(new AuthGatewayFilterFactory.Config()))
							.filter(channelGatewayFilterFactory.apply(new ChannelGatewayFilterFactory.Config()))
							.filter(serviceGatewayFilterFactory.apply(new ServiceGatewayFilterFactory.Config()))
							.removeRequestHeader("Content-Type")
							.addRequestHeader("Content-type", "application/json")
						)
						.uri("http://localhost:8088")
				)
				.build();
				
	}
	
	@Bean
	public GlobalFilter requestTransformationGlobalFilter() {
		return new RequestTransformGlobalFilter();
	}
	
	@Bean
	public GlobalFilter responseTransformationGlobalFilter() {
		return new ResponseTransformGlobalFilter();
	}
}
