package com.kt.icis.sa.icisapigw.transform.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ServiceRequest {

	public CommonHeader commonHeader;
	public BizHeader bizHeader;
	public String saSecurity;
}
