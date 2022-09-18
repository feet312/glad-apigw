package com.kt.icis.sa.icisapigw.filters;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.OrderedGatewayFilter;
import org.springframework.cloud.gateway.filter.RouteToRequestUrlFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.kt.icis.sa.icisapigw.common.exception.GwRuntimeException;
import com.kt.icis.sa.icisapigw.common.redis.RedisOperator;
import com.kt.icis.sa.icisapigw.common.utils.CacheUtil;
import com.kt.icis.sa.icisapigw.transform.model.CommonHeader;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ChannelGatewayFilterFactory extends AbstractGatewayFilterFactory<ChannelGatewayFilterFactory.Config> {
	
	@Autowired
	private RedisOperator<Object> redisOperator;
	
	@Value("${spring.profiles.active}")
	private String activeProfile;
	
	@Autowired
	private Environment env;
	
	public ChannelGatewayFilterFactory() {
		super(Config.class);
	}
	
	@Override
	public GatewayFilter apply(Config config) {
		return new OrderedGatewayFilter(((exchange, chain) -> {
			
			CommonHeader commonHeader = exchange.getAttribute("commonHeader");
			
			if(activeProfile.equals("local")) {
				return chain.filter(exchange);
			}
			
			String checkChFlag = "";
			String checkIpList = "";
			String checkChId = "";
			
			checkChFlag = CacheUtil.getComvarCache(redisOperator, "SOAP_DENY_CH_FLAG");
			checkChId = CacheUtil.getComvarCache(redisOperator, "SOAP_DENY_CH_ID_LIST");
			checkIpList = CacheUtil.getEsbChipListChidCache(redisOperator, checkChId);
			
			if(checkChFlag == null || checkChId == null || checkIpList == null) {
				if(env.getProperty("icis.use.cacheapi.flag").equalsIgnoreCase("ture")) {
					checkChFlag = CacheUtil.getComvarWhenRedisDisable("SOAP_DENY_CH_FLAG");
					checkChId = CacheUtil.getComvarWhenRedisDisable("SOAP_DENY_CH_ID_LIST");
					checkIpList = CacheUtil.getEsbChipListChidWhenRedisDisable(checkChId);
				}
			}
			
			if(!checkChFlag.equalsIgnoreCase("false")) {
				String ipAddr = commonHeader.getClntIp();
				String[] checkIpArr = checkIpList.split(",");
				
				for(String checkIp:checkIpArr) {
					if(checkIp.equals(ipAddr) || checkIp.equals("*")) {
						return chain.filter(exchange);
					} else {
						throw new GwRuntimeException("ICCS1003");
					}	
				}
			}
			return chain.filter(exchange);
			
		}), (RouteToRequestUrlFilter.ROUTE_TO_URL_FILTER_ORDER + 2)); 
	}
	
	public static class Config {
	}
}
