package com.kt.icis.sa.icisapigw.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ResultMessage {

	private String successYn;		// 성공/실패 여부 
	private String statusCode;		// httpStatus Code
	private String code;			// 업무별 코드 
	private String obj;				// 전달할 data Pojo 객체 
	private String message;
	private String devMessage;		// 디버그용 에러 메시지 
}
