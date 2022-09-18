package com.kt.icis.sa.icisapigw.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToknAvailHstVrfReq {
	private String athnToknId;
	private String userIpadr;
	private String userId;

}
