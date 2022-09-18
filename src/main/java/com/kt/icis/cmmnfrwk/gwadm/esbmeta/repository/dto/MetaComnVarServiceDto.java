package com.kt.icis.cmmnfrwk.gwadm.esbmeta.repository.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class MetaComnVarServiceDto implements Serializable {
	private static final long serialVersionUID = 113L;
	private String key;
	private String value;
	private String keyDesc;
	private LocalDateTime cretDt;
	private String cretId;
	private LocalDateTime chgDt;
	private String chgId;
}
