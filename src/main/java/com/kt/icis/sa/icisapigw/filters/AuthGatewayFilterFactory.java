package com.kt.icis.sa.icisapigw.filters;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.OrderedGatewayFilter;
import org.springframework.cloud.gateway.filter.RouteToRequestUrlFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import com.kt.icis.sa.icisapigw.common.exception.GwRuntimeException;
import com.kt.icis.sa.icisapigw.transform.model.CommonHeader;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class AuthGatewayFilterFactory extends AbstractGatewayFilterFactory<AuthGatewayFilterFactory.Config> {

	@Autowired
	private Environment env;
	
	@Value("${spring.profiles.active}")
	private String activeProfile;
	
	public AuthGatewayFilterFactory() {
		super(Config.class);
	}
	
	@Override
	public GatewayFilter apply(Config config) {
		return new OrderedGatewayFilter(((exchange, chain) -> {
			
			CommonHeader commonHeader = exchange.getAttribute("commonHeader");
			
			if(activeProfile.equals("local")) {
				return chain.filter(exchange);
			}
			
			if(env.getProperty("icis.auth.flag").equalsIgnoreCase("false")) {
				return chain.filter(exchange);
			}
			
			String authRequest = "";
			
			return WebClient.create().post().uri(env.getProperty("icis.web.auth.url"))
					.body(BodyInserters.fromValue(authRequest)).retrieve()
					.bodyToMono(String.class)
					.flatMap(s -> {
						if(!s.contains("<resltCd>S")) {
							throw new GwRuntimeException("ICSS1004");
						} else {
							log.debug(authRequest);
						}
						
						return chain.filter(exchange);
					});
		}), (RouteToRequestUrlFilter.ROUTE_TO_URL_FILTER_ORDER + 4));
	}
	
	public static class Config {
	}
}
