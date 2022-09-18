package com.kt.icis.sa.icisapigw.common.security.lib;

import javax.xml.bind.DatatypeConverter;

/**
 * <pre>
 * 체크섬 알고리즘 테스트 케이스 
 * </pre>
 * 
 * @author sehwan
 *
 */
public class XORChecksum {

	public static final String ENCODING = "UTF-8";
	
	public static final int CHECKSUM_BYTE = 8;
	
	public static String makeCheckSum(String msg, byte[] key) throws Exception {
		int keyLength = key.length;
		
		byte[] byteChecksum = new byte[CHECKSUM_BYTE];
		
		for(int i=0; i < CHECKSUM_BYTE; i++) {
			byteChecksum[i] = -0x7f;
		}
		
		byte[] bytes = msg.getBytes(ENCODING);
		int bytesLength = bytes.length;
		
		int nIdxKey = 0;
		int nIdxChecksum = 0;
		
		for(int i=0; i < bytesLength; i++) {
			byteChecksum[nIdxChecksum++] ^= bytes[i]^key[nIdxKey++];
			
			if(nIdxKey == keyLength) {
				nIdxKey = 0;
			}
			
			if(nIdxChecksum == CHECKSUM_BYTE) {
				nIdxChecksum = 0;
			}
		}
		String strBase64 = DatatypeConverter.printBase64Binary(byteChecksum);
		
		return strBase64;
	}
}
