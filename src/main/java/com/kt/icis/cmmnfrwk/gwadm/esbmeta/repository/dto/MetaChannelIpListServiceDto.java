package com.kt.icis.cmmnfrwk.gwadm.esbmeta.repository.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class MetaChannelIpListServiceDto implements Serializable {
	private static final long serialVersionUID = 101L;
	private String chId;
	private String ipAddr;
	private LocalDateTime cretDt;
	private String cretId;
	private LocalDateTime chgDt;
	private String chgId;

}
