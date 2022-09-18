package com.kt.icis.sa.icisapigw.common.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToknAvailHstVrfRes {
	private String trtReslt;
	private String resltMsg;
	private String resltCd;
}
