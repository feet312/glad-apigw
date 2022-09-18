package com.kt.icis.sa.icisapigw.transform;

import java.io.ByteArrayOutputStream;
import java.util.Map;

import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.gateway.filter.factory.rewrite.RewriteFunction;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kt.icis.sa.icisapigw.common.constant.Constant;
import com.kt.icis.sa.icisapigw.common.redis.RedisOperator;
import com.kt.icis.sa.icisapigw.common.security.PayloadHash;
import com.kt.icis.sa.icisapigw.common.utils.CacheUtil;
import com.kt.icis.sa.icisapigw.common.utils.TransformUtil;
import com.kt.icis.sa.icisapigw.monitoring.MonitoringService;
import com.kt.icis.sa.icisapigw.transform.model.CommonHeader;

import com.nexacro.xapi.data.PlatformData;
import com.nexacro.xapi.tx.PlatformResponse;
import com.nexacro.xapi.tx.PlatformType;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@RefreshScope
@Component
@Slf4j
public class ResponseBodyTransform implements RewriteFunction<String, String> {
	
	@Autowired
	private ObjectMapper mapper;
	
	@Autowired
	Environment env;
	
	@Autowired
	private MonitoringService monitoringService;
	
	@Autowired
	private RedisOperator<Object> redisOperator;
	
	@Value("${spring.profiles.active}")
	private String activeProfile;
	
	
	@SneakyThrows
	@Override
	public Publisher<String> apply(ServerWebExchange exchange, String responseBody) {
		
		if(responseBody == null) {
			log.error("response is empty");
			throw new IllegalArgumentException("ICSS1001");
		}
		
		Map<String, Object> serviceResponseMap = null;
		Map<String, Object> errorResponseMap = null;
		String errorCode = "0";
		
		String xmlResponseBody = null;
		String modifiedJson = modifyJsonBody(responseBody);
		serviceResponseMap = TransformUtil.jsonToObject(mapper, modifiedJson, Constant.RESPONSE_PATH, new TypeReference<Map<String, Object>>() {});
		errorResponseMap = TransformUtil.jsonToObject(mapper, modifiedJson, "", new TypeReference<Map<String, Object>>() {});
		xmlResponseBody = TransformUtil.getXmlResponseBody(serviceResponseMap);
		CommonHeader commonHeader = TransformUtil.jsonToObject(mapper, modifiedJson, Constant.RESPONSE_COMMON_HEADER_PATH, new TypeReference<CommonHeader>() {});
		
		if(env.getProperty("icis.bmon.logging.flag").equalsIgnoreCase("true")) {
			// 로깅처리 
			if(commonHeader != null) {
				monitoringService.sendResAsync(commonHeader.getGlobalNo(), modifiedJson);
			}
		}
		
		if(xmlResponseBody == null || errorResponseMap == null) {
			log.error("response is not valid");
			throw new IllegalArgumentException("ICSS1001");
		}
		
		String uiDtoFormat = "";
		String strData = "";
		PlatformData platformData = null;
		if(errorResponseMap.containsKey("status") && !errorResponseMap.get("status").equals("200")) {
			log.error("response is not valid");
			throw new IllegalArgumentException("ICSS1002"); 
		} else {
			if(!commonHeader.getResponseType().equals("I") && !commonHeader.getResponseType().equals("D")) errorCode = "-99";
			
			uiDtoFormat = TransformUtil.getXml2UiFormatXml(xmlResponseBody);
			platformData = TransformUtil.convertPlatformData(uiDtoFormat, false);
			
			com.nexacro.xapi.data.VariableList variableList = new com.nexacro.xapi.data.VariableList();
			variableList.add("Errorcode", errorCode);
			
			ByteArrayOutputStream aByteArrayOutputStream = new ByteArrayOutputStream();
			PlatformResponse res;
			if("true".equalsIgnoreCase(env.getProperty("icis.ui.ssv.flag"))) {
				res = new PlatformResponse(aByteArrayOutputStream, PlatformType.CONTENT_TYPE_SSV);
			} else {
				res = new PlatformResponse(aByteArrayOutputStream, PlatformType.CONTENT_TYPE_XML);
			}
			
			res.setData(platformData);
			res.sendData();
			aByteArrayOutputStream.close();
			strData = aByteArrayOutputStream.toString("UTF-8");
		}
		
		TransformUtil.setResponseContentType(exchange, MediaType.APPLICATION_XML);
		exchange.getResponse().setStatusCode(HttpStatus.OK);
		
		String responseHash = "";
		if(activeProfile.equals("local")) {
			
		} else {
			responseHash = getResponseHash(platformData, strData);
		}
		
		return Mono.just(responseHash+strData);
	}
	
	private String modifyJsonBody(String jsonBody) {
		return jsonBody;
	}
	
	protected String getResponseHash(final PlatformData platformData, final String uiXml) throws Exception {
		String hash = "";
		String hashFlag = CacheUtil.getComvarCache(redisOperator, "UI_RESPONSE_HASH_FLAG");
		if(hashFlag == null) {
			if(env.getProperty("icis.use.cacheapi.flag").equalsIgnoreCase("true")) {
				hashFlag = CacheUtil.getComvarWhenRedisDisable("UI_RESPONSE_HASH_FLAG");
			}
		}
		if(hashFlag.equalsIgnoreCase("true") && platformData.getVariable("ErrorCode").getInt() >= 0) {
			String serviceName = platformData.getDataSet("commonHeader").getString(0, "svcName");
			if(PayloadHash.isResponseHash(redisOperator, serviceName)) {
				hash = PayloadHash.getResponseHash(uiXml);
			}
		}
		return hash;
	}

}
