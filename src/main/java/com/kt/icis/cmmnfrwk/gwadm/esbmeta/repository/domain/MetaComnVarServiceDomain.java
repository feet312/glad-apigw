package com.kt.icis.cmmnfrwk.gwadm.esbmeta.repository.domain;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.kt.icis.cmmnfrwk.gwadm.esbmeta.repository.dto.MetaComnVarServiceDto;

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
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class MetaComnVarServiceDomain implements Serializable {

	@Schema(description="공통변수KEY", nullable = false, example="1")
	private String key;
	@Schema(description="공통변수값", nullable = false, example="1")
	private String value;
	@Schema(description="공통변수값설명", nullable = false, example="1")
	private String keyDesc;
	@Schema(description="생성일자", nullable = false, example="1")
	private LocalDateTime cretDt;
	@Schema(description="생성자ID", nullable = false, example="1")
	private String cretId;
	@Schema(description="변경일자", nullable = false, example="1")
	private LocalDateTime chgDt;
	@Schema(description="변경자ID", nullable = false, example="1")
	private String chgId;
	
	public static MetaComnVarServiceDomain of(MetaComnVarServiceDto dto) {
		return MetaComnVarServiceDomain.builder()
				.key(dto.getKey())
				.value(dto.getValue())
				.keyDesc(dto.getKeyDesc())
				.cretDt(dto.getCretDt())
				.cretId(dto.getCretId())
				.chgDt(dto.getChgDt())
				.chgId(dto.getChgId())
				.build();
	}
	
	public static List<MetaComnVarServiceDomain> ofList(List<MetaComnVarServiceDto> MetaComnVarServiceList) {
		List<MetaComnVarServiceDomain> payloadList = new ArrayList<>();
		for(MetaComnVarServiceDto dto: MetaComnVarServiceList ) {
			payloadList.add(MetaComnVarServiceDomain.of(dto));
		}
		return payloadList;
	}
}
