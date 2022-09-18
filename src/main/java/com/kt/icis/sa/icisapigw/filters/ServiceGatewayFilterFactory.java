package com.kt.icis.sa.icisapigw.filters;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.OrderedGatewayFilter;
import org.springframework.cloud.gateway.filter.RouteToRequestUrlFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;

import com.kt.icis.cmmnfrwk.gwadm.esbmeta.repository.dto.MetaServiceInfoServiceDto;
import com.kt.icis.sa.icisapigw.common.exception.GwRuntimeException;
import com.kt.icis.sa.icisapigw.common.redis.RedisOperator;
import com.kt.icis.sa.icisapigw.common.utils.CacheUtil;
import com.kt.icis.sa.icisapigw.transform.model.CommonHeader;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ServiceGatewayFilterFactory extends AbstractGatewayFilterFactory<ServiceGatewayFilterFactory.Config> {

	@Autowired
	private RedisOperator<Object> redisOperator;
	
	@Value("${spring.profiles.active}")
	private String activeProfile;
	
	public ServiceGatewayFilterFactory() {
		super(Config.class);
	}
	

	@Override
	public GatewayFilter apply(Config config) {
		return new OrderedGatewayFilter(((exchange, chain) -> {
			CommonHeader commonHeader = exchange.getAttribute("commonHeader");
			
			if(activeProfile.equals("local")) {
				return chain.filter(exchange);
			}
			
			final Calendar currentDate = Calendar.getInstance();
			String cacheKey = commonHeader.getAppName() + ":" + commonHeader.getSvcName() + ":" + commonHeader.getFnName();
			
			MetaServiceInfoServiceDto item = CacheUtil.getServiceInfoCache(redisOperator, cacheKey);
			
			if(item != null) {
				if(item.getStDt() != null && item.getFnsDt() != null 
						&& commonHeader.getSvcName().equals(item.getSvcNm()) 
						&& commonHeader.getFnName().equals(item.getFuncNm())) {
					final Calendar sDate = Calendar.getInstance();
					Instant instant = item.getStDt().atZone(ZoneId.systemDefault()).toInstant();
					sDate.setTime(Date.from(instant));
					instant = item.getFnsDt().atZone(ZoneId.systemDefault()).toInstant();
					final Calendar eDate = Calendar.getInstance();
					eDate.setTime(Date.from(instant));
					
					Date curD = currentDate.getTime();	// 현재 날짜 
					Date stD = sDate.getTime();			// 시작 날짜 
					Date fnD = eDate.getTime();			// 종료 날짜 
					DateFormat dfd = new SimpleDateFormat("yyyymmdd");
					
					if("N".equals(item.getUseYn())									// 사용여부가 N 이고 
							&& (dfd.format(curD).equals(dfd.format(stD))			// 오늘이 시작일인 경우 
									|| (curD.after(stD) && curD.before(fnD))		// 오늘이 시작일 ~ 종료일 사이인 경우 
									|| dfd.format(curD).equals(dfd.format(fnD)) ) 	// 오늘이 종료일인 경우 
					) {
						throw new GwRuntimeException("ICSS1006");
						
					}
					if("Y".equals(item.getUseYn())									// 사용여부가 Y 이고 
							&& (dfd.format(curD).equals(dfd.format(stD))			// 오늘이 시작일인 경우 
									|| (curD.after(stD) && curD.before(fnD))		// 오늘이 시작일 ~ 종료일 사이인 경우 
									|| dfd.format(curD).equals(dfd.format(fnD)) ) 	// 오늘이 종료일인 경우 
					) {
						throw new GwRuntimeException("ICSS1006");
						
					}
				}
			}
			return chain.filter(exchange);
		}), (RouteToRequestUrlFilter.ROUTE_TO_URL_FILTER_ORDER + 3));
	}
	
	public static class Config{}
}
