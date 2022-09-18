package com.kt.icis.sa.icisapigw.common.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.kt.icis.cmmnfrwk.gwadm.esbmeta.repository.payload.in.QueryMetaChIpListInDs;
import com.kt.icis.cmmnfrwk.gwadm.esbmeta.repository.payload.in.QueryMetaComnVarInDs;

@FeignClient(name="gwadm", url="${api.gwadm.url}")
public interface GwAdmFeignClient {
	@GetMapping(value="/system/healthz", produces="text/plain")
	public String getSystemHealth();
	
	@PostMapping(path="/gw-adm/alternative/getcomnvar", consumes="application/json")
	public String redisSearch(@RequestBody QueryMetaComnVarInDs serviceInfo);
	
	@PostMapping(path="/gw-adm/alternative/getchanneliplist", consumes="application/json")
	public String redisSearch(@RequestBody QueryMetaChIpListInDs channelIpList);	
}
