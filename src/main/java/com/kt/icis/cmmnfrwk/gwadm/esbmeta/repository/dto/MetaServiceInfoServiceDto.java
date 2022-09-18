package com.kt.icis.cmmnfrwk.gwadm.esbmeta.repository.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class MetaServiceInfoServiceDto implements Serializable {
	private static final long serialVersionUID = 112L;
	private String appNm;
	private String svcNm;
	private String funcNm;
	private String useYn;
	private String svcDesc;
	private String authYn;
	private String errCnclUseYn;
	private String errcnclYn;
	private String errProgressUseYn;
	private String errProgressYn;
	private String errSwitchUseYn;
	private String errSwitchYn;
	private String errAsycUseYn;
	private String errAsycYn;
	private String err24by7UserYn;
	private String err24by7Yn;
	private LocalDateTime stDt;
	private LocalDateTime fnsDt;
	private LocalDateTime cretDt;
	private String cretId;
	private LocalDateTime chgDt;
	private String chgId;
}
