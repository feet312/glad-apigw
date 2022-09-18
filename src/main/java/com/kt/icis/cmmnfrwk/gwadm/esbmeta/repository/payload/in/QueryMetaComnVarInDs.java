package com.kt.icis.cmmnfrwk.gwadm.esbmeta.repository.payload.in;

import com.kt.icis.cmmnfrwk.gwadm.esbmeta.repository.domain.MetaComnVarServiceDomain;

import lombok.Data;
import lombok.Getter;

@Data
@Getter
public class QueryMetaComnVarInDs {
	private MetaComnVarServiceDomain metaComnVarServicePayload;
}
