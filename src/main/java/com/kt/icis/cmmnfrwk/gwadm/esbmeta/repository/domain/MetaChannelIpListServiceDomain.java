package com.kt.icis.cmmnfrwk.gwadm.esbmeta.repository.domain;

import java.time.LocalDateTime;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

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
public class MetaChannelIpListServiceDomain {

	@Schema(description="채널ID", nullable = false, example="1")
	private String chId;
	@Schema(description="접근IP주소", nullable = false, example="1")
	private String ipAddr;
	@Schema(description="생성일자", nullable = false, example="1")
	@JsonSerialize(using=LocalDateTimeSerializer.class)
	@JsonDeserialize(using=LocalDateTimeDeserializer.class)
	private LocalDateTime cretDt;
	@Schema(description="생성자ID", nullable = false, example="1")
	private String cretId;
	@Schema(description="변경일자", nullable = false, example="1")
	@JsonSerialize(using=LocalDateTimeSerializer.class)
	@JsonDeserialize(using=LocalDateTimeDeserializer.class)
	private LocalDateTime chgDt;
	@Schema(description="변경자ID", nullable = false, example="1")
	private String chgId;
}
