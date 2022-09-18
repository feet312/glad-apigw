package com.kt.icis.sa.icisapigw.common.exception;

import java.nio.charset.StandardCharsets;

import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.kt.icis.sa.icisapigw.common.utils.TransformUtil;
import com.kt.icis.sa.icisapigw.transform.model.BizHeader;
import com.kt.icis.sa.icisapigw.transform.model.CommonHeader;
import com.kt.icis.sa.icisapigw.transform.model.ServiceResponse;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class GwExceptionHandler implements ErrorWebExceptionHandler {
	
	@Bean
	public ErrorWebExceptionHandler myWExceptionHandler() {
		return new GwExceptionHandler();
	}
	
	private String getDefaultError(String pErrorCode) {
		CommonHeader commonHeader = CommonHeader.builder().appName("NBSS_ICIS").svcName("/api-gw/common/error").fnCd("service").build();
		BizHeader bizHeader = BizHeader.builder().cbFnName("service").cbSvcName("/api-gw/common/error").build();
		
		String errTitle = "";
		String errMessage = "";
		String errorCode = "";
		
		if(pErrorCode.equals("ICSS1002")) {
			errTitle = "일시적으로 서비스를 이용할 수 없습니다.";
			errMessage = "일시적으로 서비스를 이용할 수 없습니다. 잠시 후 다시 시도하여 주시기 바랍니다.";
		} else if(pErrorCode.equals("ICSS1003")) {
			errTitle = "차단된 CH ID 입니다";
			errMessage = "서비스 호출에 실패하였습니다. IS 만족센터로 문의하세요.";
		} else if(pErrorCode.equals("ICSS1004")) {
			errTitle = "인증에러";
			errMessage = "사용자인증에러 : 사용자 인증토큰이 만료 되었습니다. 다시 로그인 하십시요.";
		} else if(pErrorCode.equals("ICSS1005")) {
			errTitle = "Checksum 에러";
			errMessage = "Chechsum에러 : Checksum 값이 올바르지 않습니다.";
		} else if(pErrorCode.equals("ICSS1006")) {
			errTitle = "서비스 사용 및 기간 에러";
			errMessage = "차단된 서비스이거나 사용 기간이 맞지 않습니다.";
		} else if(pErrorCode.equals("ICSS1007")) {
			errTitle = "요청 및 응답에러";
			errMessage = "요청 또는 응답이 올바르지 않습니다.";
		} else {
			errorCode = "ICSS1001";
			errTitle = "요청이 실패하였습니다.";
			errMessage = "요청 처리에 실패하였습니다.";
		}
		
		commonHeader.setResponseType("S");
		commonHeader.setResponseCode(errorCode);
		commonHeader.setResponseTitle(errTitle);
		commonHeader.setResponseBasc(errMessage);
		commonHeader.setResponseDtal(errMessage);
		ServiceResponse serviceResponse = new ServiceResponse();
		serviceResponse.setCommonHeader(commonHeader);
		serviceResponse.setBizHeader(bizHeader);
		
		String strData = "";
		String uiDtoFormat = "";
		
		try {
			uiDtoFormat = TransformUtil.getXml2UiFormatXml(TransformUtil.getXmlResponseBodyFromObj(serviceResponse));
			strData = TransformUtil.transformObjToResponse(uiDtoFormat);
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		return strData;
	}
	
	@Override
	public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
		String errorCode = "ICSS1001";
		
		if(ex.getClass() == IllegalArgumentException.class) {
			errorCode = ex.getMessage();
		} else if(ex.getClass() == GwRuntimeException.class) {
			errorCode = ex.getMessage();
		} else if(ex.getClass() == CallNotPermittedException.class) {
			errorCode = "ICSS1002";
		} else {
			errorCode = "ICSS1001";
		}
		
		byte[] bytes = getDefaultError(errorCode).getBytes(StandardCharsets.UTF_8);
		DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
		exchange.getResponse().getHeaders().add("Content-Type", MediaType.APPLICATION_XML_VALUE);
		exchange.getResponse().setStatusCode(HttpStatus.OK);
		
		return exchange.getResponse().writeWith(Mono.just(buffer));
	}

}
