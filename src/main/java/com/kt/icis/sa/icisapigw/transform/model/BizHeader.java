package com.kt.icis.sa.icisapigw.transform.model;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description= "비즈헤더")
public class BizHeader {
	@Schema(description= "Order Id", nullable = true)
	private String orderId;
	@Schema(description= "CB서비스 이름", nullable = true)
	private String cbSvcName;
	@Schema(description= "CB오퍼레이션 이름", nullable = true)
	private String cbFnName;
}
