package com.kt.icis.cmmnfrwk.gwadm.esbmeta.repository.payload.in;

import com.kt.icis.cmmnfrwk.gwadm.esbmeta.repository.domain.MetaChannelIpListServiceDomain;

import lombok.Data;
import lombok.Getter;

@Data
@Getter
public class QueryMetaChIpListInDs {
	private MetaChannelIpListServiceDomain metaChannelIpListPayload;
}
