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
@Schema(description= "공통헤더")
public class CommonHeader {
	@Schema(description= "어플리케이션 이름", nullable = false)
	private String appName;
	@Schema(description= " 서비스 이름", nullable = false)
	private String svcName;
	@Schema(description= "오퍼레이션 이름", nullable = false)
	private String fnName;
	@Schema(description= "기능코드", nullable = true)
	private String fnCd;
	@Schema(description= "거래고유번호", nullable = false)
	private String globalNo;
	@Schema(description= "채널구분", nullable = false)
	private String chnlType;
	@Schema(description= "환경구분", nullable = true, example = "사용하지 않음")
	private String envrFlag;
	@Schema(description= "송수신Flag", nullable = true, example = "T/R")
	private String trFlag;
	@Schema(description= "송수신일자", nullable = false, example = "20220810(2022년 8월 10일)")
	private String trDate;
	@Schema(description= "송수신시간", nullable = false, example = "225323005(22시 53분 23.005초)")
	private String trTime;
	@Schema(description= "클라이언트IP", nullable = false)
	private String clntIp;
	@Schema(description= "응답유형", nullable = false, example = "S/E/I/D")
	private String responseType;
	@Schema(description= "응답코드", nullable = false, example = "COME5501,공백")
	private String responseCode;
	@Schema(description= "응답구분코드", nullable = true)
	private String responseLogCd;
	@Schema(description= "응답타이틀", nullable = false)
	private String responseTitle;
	@Schema(description= "응답기본내역", nullable = false)
	private String responseBasc;
	@Schema(description= "응답상세내역", nullable = false)
	private String responseDtal;
	@Schema(description= "응답시스템", nullable = true, example = "1")
	private String responseSystem;
	@Schema(description= "사용자아이디", nullable = false, example = "통합ID에서 사용")
	private String userId;
	@Schema(description= "실사용자아이디", nullable = true, example = "채널ID에서 사용")
	private String realUserId;
	@Schema(description= "필러", nullable = true, example = "EAI에서 요청시 DTO의 풀명칭")
	private String filler;
	@Schema(description= "사용자언어코드", nullable = true, example = "사용하지 않음")
	private String langCode;
	@Schema(description= "조직ID", nullable = false, example = "사용자가 속한 최하부 조직ID")
	private String orgId;
	@Schema(description= "Source ID", nullable = false, example = "최초 이벤트 발생 Program ID")
	private String srcId;
	@Schema(description= "Current Host ID", nullable = true, example = "현재 처리 hostname")
	private String curHostId;
	@Schema(description= "Logical Date & time", nullable = false, example = "20220810225323(2022년 8월 10일 22시 53분 23초)")
	private String lgDateTime;
	@Schema(description= "Token ID", nullable = true, example="ESB->권한/보안을 통해 얻은 토ID")
	private String tokenId;
	@Schema(description= "Company Code", nullable = true, example="판매회사코드")
	private String cmpnCd;
	@Schema(description= "Locking 유형", nullable = true, example="\"1\": ban전용, \"2\": ncn전용, \"3\": ban+ncn")
	private String lockType;
	@Schema(description= "Locking ID", nullable = true)
	private String lockId;
	@Schema(description= "Locking Timestamp", nullable = true)
	private String lockTimeSt;
	@Schema(description= "비즈니스키", nullable = true, example="E2E 모니털이 비즈니스 키 전달 용도")
	private String businessKey;
	@Schema(description= "임의키", nullable = true, example="Reserved 필드")
	private String arbitraryKey;
	@Schema(description= "재처리연동구분", nullable = true, example="1")
	private String resendFlag;
	@Schema(description= "phase", nullable = true, example="N/R/C")
	private String phase;

	@Schema(description= "B-MON 로그 포인트", nullable = false, example = "EB/IC/... BMON 담당자로부터 전달받은 로그포인트 ")
	private String logPoint;
	

}
