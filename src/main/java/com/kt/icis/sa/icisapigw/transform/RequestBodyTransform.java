package com.kt.icis.sa.icisapigw.transform;

import org.apache.commons.lang3.StringUtils;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.gateway.filter.factory.rewrite.RewriteFunction;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.kt.icis.sa.icisapigw.common.constant.Constant;
import com.kt.icis.sa.icisapigw.common.redis.RedisOperator;
import com.kt.icis.sa.icisapigw.common.utils.CacheUtil;
import com.kt.icis.sa.icisapigw.common.utils.CommonUtil;
import com.kt.icis.sa.icisapigw.common.utils.TransformUtil;
import com.kt.icis.sa.icisapigw.monitoring.MonitoringService;
import com.kt.icis.sa.icisapigw.transform.model.CommonHeader;
import com.kt.icis.sa.icisapigw.transform.model.SaSecurity;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@RefreshScope
@CircuitBreaker(name = "apigw")
@Component
@Slf4j
public class RequestBodyTransform implements RewriteFunction<String, String> {
	
	@Autowired
	private ObjectMapper mapper;
	
	@Autowired
	private MonitoringService monitoringService;
	
	@Autowired
	private RedisOperator<Object> redisOperator;
	
	@Value("${router.global.service.url}")
	private String routerGlobalServiceUrl;
	
	@Value("${spring.profiles.active}")
	private String activeProfile;
	
	@Autowired
	Environment env;
	
	@Override
	public Publisher<String> apply(ServerWebExchange exchange, String requestBody) {
		String requestBodyStr = "";
		String encCharSet = "utf-8";
		String hashFlag = null;
		
		if(activeProfile.equals("local")) {
			hashFlag = "choice";
		} else {
			hashFlag = CacheUtil.getComvarCache(redisOperator, "UI_REQUEST_HASH_FLAG");
			if(hashFlag == null) {
				if(env.getProperty("icis.use.cacheapi.flag").equalsIgnoreCase("true")) {
					hashFlag = CacheUtil.getComvarWhenRedisDisable("SOAP_DENY_CH_FLAG");
				}
			}
		}
		
		requestBodyStr = TransformUtil.getRequestXmlStr(requestBody, encCharSet, hashFlag);
		
		if(requestBodyStr == null || requestBodyStr.equals("")) {
			throw new IllegalArgumentException("ICSS1001");
		}
		
		String jsonBody = null;
		if(requestBodyStr.contains("<typeHeader")) {
			jsonBody = TransformUtil.xmlToJsonArray(mapper, requestBodyStr);
		} else {
			jsonBody = TransformUtil.xmlToJson(mapper, requestBodyStr);
		}
		
		String modifiedJsonBody = jsonBody;
		CommonHeader commonHeader = TransformUtil.jsonToObject(mapper, jsonBody, Constant.REQUEST_COMMON_HEADER_PATH, new TypeReference<CommonHeader>() {});
		SaSecurity saSecurity = TransformUtil.jsonToObject(mapper, jsonBody, Constant.REQUEST_SA_SECURITY_PATH, new TypeReference<SaSecurity>() {});
		
		if(jsonBody == null) {
			throw new IllegalArgumentException("ICSS1001");
		}
		
		if(!activeProfile.equals("local")) {
			
			// 시간 동기화 (lgDateTime)
			if(env.getProperty("icis.ui.lgdatetime.flag").equalsIgnoreCase("true")) {
				modifiedJsonBody = modifyJsonBody(modifiedJsonBody);
			}
			
			// BMON 로깅 
			if(env.getProperty("icis.bmon.logging.flag").equalsIgnoreCase("true")) {
				monitoringService.sendReqAsync(commonHeader.getGlobalNo(), jsonBody);
			}
			
			String routerServiceUrl = "";
			// 성능테스트를 위한 처리 
			if(commonHeader.getSvcName().startsWith("/ppon/test")) {
				routerServiceUrl = "http://icis-apigw-active.sa-app.svc:8800";
				TransformUtil.changeRequestUrl(exchange, routerServiceUrl, commonHeader.getSvcName());
			} else {
				TransformUtil.changeRequestUrl(exchange, routerGlobalServiceUrl, commonHeader.getSvcName());
			}
		} else {
			String localPort = null;
			if(saSecurity != null) {
				localPort = saSecurity.getLocalPort();
			}
			
			String routeServiceUrl = "http://icis-samp-ppon.sa-app.svc";
			if(StringUtils.isNotBlank(localPort)) {
				routeServiceUrl = String.format("http://localhost:%s", localPort);
			}
			
			TransformUtil.changeRequestUrl(exchange, routeServiceUrl, commonHeader.getSvcName());
		}
		exchange.getAttributes().put("commonHeader", commonHeader);
		
		return Mono.just(modifiedJsonBody);
	}
	
	private String modifyJsonBody(String jsonBody) {
		JsonNode node;
		try {
			node = mapper.readTree(jsonBody);
		} catch (JsonProcessingException e) {
			log.error(e.getMessage());
			return "";
		}
		
		// CommonHeader 의 lgDateTime 값을 현재 서버시간으로 대체 
		JsonNode commonHeaderNode = node.at(Constant.REQUEST_COMMON_HEADER_PATH);
		String originLgDateTime = commonHeaderNode.get("lgDateTime").asText();
		
		ObjectNode objCommonHeaderNode = (ObjectNode) commonHeaderNode;
		objCommonHeaderNode.remove("lgDateTime");
		objCommonHeaderNode.put("lgDateTime", CommonUtil.getDateTime("YYYYMMDDHHMISS"));
		
		return node.toPrettyString();
	}

}
