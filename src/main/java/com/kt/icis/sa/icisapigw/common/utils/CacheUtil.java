package com.kt.icis.sa.icisapigw.common.utils;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.kt.icis.cmmnfrwk.gwadm.esbmeta.repository.domain.MetaChannelIpListServiceDomain;
import com.kt.icis.cmmnfrwk.gwadm.esbmeta.repository.domain.MetaComnVarServiceDomain;
import com.kt.icis.cmmnfrwk.gwadm.esbmeta.repository.dto.MetaChannelIpListServiceDto;
import com.kt.icis.cmmnfrwk.gwadm.esbmeta.repository.dto.MetaComnVarServiceDto;
import com.kt.icis.cmmnfrwk.gwadm.esbmeta.repository.dto.MetaServiceInfoServiceDto;
import com.kt.icis.cmmnfrwk.gwadm.esbmeta.repository.payload.in.QueryMetaChIpListInDs;
import com.kt.icis.cmmnfrwk.gwadm.esbmeta.repository.payload.in.QueryMetaComnVarInDs;
import com.kt.icis.sa.icisapigw.common.constant.Constant;
import com.kt.icis.sa.icisapigw.common.feign.GwAdmFeignClient;
import com.kt.icis.sa.icisapigw.common.redis.RedisOperator;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class CacheUtil {

	private static GwAdmFeignClient gwAdmFeignClient;
	public static GwAdmFeignClient getGwAdmFeignClient() {
		if(gwAdmFeignClient == null) {
			gwAdmFeignClient = (GwAdmFeignClient) CommonBeanUtil.getBean(GwAdmFeignClient.class);
		}
		return gwAdmFeignClient;
	}
	
	private static ObjectMapper objectMapper;
	public static ObjectMapper getInstanceObjectMapper() {
		if(objectMapper == null) {
			log.trace("== call new ObjectMapper()");
			objectMapper = (ObjectMapper) new ObjectMapper().registerModule(new JavaTimeModule());
		}
		return objectMapper;
	}
	
	public static String getComvarCache(RedisOperator<Object> redisOperator, String key) {
		MetaComnVarServiceDto metaComnVarServiceDto = (MetaComnVarServiceDto) redisOperator.getValue(Constant.ESB_COMN_VAR_KEY + ":" + key);
		if(metaComnVarServiceDto == null) {
			log.error("[ICISTR] Rediscache metaComnVarServiceDto is null");
			return null;
		}
		
		log.debug("getComvarCache()", metaComnVarServiceDto.getValue());
		return metaComnVarServiceDto.getValue();
	}
	
	public static String getEsbChipListChidCache(RedisOperator<Object> redisOperator, String key) {
		List<MetaChannelIpListServiceDto> metaChannelIpListServiceDtoList = (ArrayList<MetaChannelIpListServiceDto>) redisOperator.getValue(Constant.ESB_CH_IP_LIST_CHID_KEY + ":" + key);
		
		log.debug("metaChannelIpListServiceDtoList:{}", metaChannelIpListServiceDtoList);
		if(metaChannelIpListServiceDtoList == null) {
			log.error("[ICISTR] Rediscache metaChannelIpListServiceDtoList is null");
			return null;
		}
		String ipAddrs = "";
		for(MetaChannelIpListServiceDto metaChannelIpListServiceDto:metaChannelIpListServiceDtoList) {
			ipAddrs += metaChannelIpListServiceDto.getIpAddr() + ",";
		}
		
		log.debug("getEsbChipListChidCache={}", ipAddrs);
		return ipAddrs;
	}
	
	public static MetaServiceInfoServiceDto getServiceInfoCache(RedisOperator<Object> redisOperator, String key) {
		MetaServiceInfoServiceDto metaServiceInfoServiceDto = (MetaServiceInfoServiceDto) redisOperator.getValue(Constant.ESB_SVC_INFO_KEY + ":" + key);
		log.debug("metaServiceInfoServiceDto:{}", metaServiceInfoServiceDto);
		if(metaServiceInfoServiceDto == null) {
			log.error("[ICISTR] Rediscache metaServiceInfoServiceDto is null");
			return null;
		}
		return metaServiceInfoServiceDto;
	}
	
	public static String getComvarWhenRedisDisable(String comvarKey) {
		QueryMetaComnVarInDs queryMetaComnVarInDs = new QueryMetaComnVarInDs();
		queryMetaComnVarInDs.setMetaComnVarServicePayload(MetaComnVarServiceDomain.builder().key(comvarKey).build());
		GwAdmFeignClient gwAdmFeignClient = getGwAdmFeignClient();
		String gwadmResponse = gwAdmFeignClient.redisSearch(queryMetaComnVarInDs);
		ObjectMapper mapper = getInstanceObjectMapper();
		MetaComnVarServiceDto metaComnVarServicePayload = TransformUtil.jsonToObject(mapper, gwadmResponse, Constant.RESPONSE_PATH+"/metaComnVarServicepayload", new TypeReference<MetaComnVarServiceDto>() {});
		String resultVal = metaComnVarServicePayload .getValue();
		log.debug("getComvarWhenRedisDisable:{}={}",comvarKey, resultVal);
		return resultVal;
	}
	
	public static String getEsbChipListChidWhenRedisDisable(String chId) {
		QueryMetaChIpListInDs queryMetaChIpListInDs = new QueryMetaChIpListInDs();
		queryMetaChIpListInDs.setMetaChannelIpListPayload(MetaChannelIpListServiceDomain.builder().chId(chId).build());
		GwAdmFeignClient gwAdmFeignClient = getGwAdmFeignClient(); 
		String gwadmResponse = gwAdmFeignClient.redisSearch(queryMetaChIpListInDs);
		ObjectMapper mapper = getInstanceObjectMapper();
		List<MetaChannelIpListServiceDto> metaChannelIpListPayload = TransformUtil.jsonToObject(mapper, gwadmResponse, Constant.RESPONSE_PATH+"/metaChannelIpListPayload", new TypeReference<List<MetaChannelIpListServiceDto>>() {});
		
		String ipAddrs = "";
		for(MetaChannelIpListServiceDto metaChannelIpListServiceDto:metaChannelIpListPayload) {
			ipAddrs += metaChannelIpListServiceDto.getIpAddr();
		}
		log.debug("getEsbChipListChidWhenRedisDisable:{}={}", chId,ipAddrs);
		return ipAddrs;
	}
	
}
