package com.kt.icis.sa.icisapigw.common.security;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import com.kt.icis.sa.icisapigw.common.redis.RedisOperator;
import com.kt.icis.sa.icisapigw.common.security.lib.KISA_SHA256;
import com.kt.icis.sa.icisapigw.common.security.lib.XORChecksum;
import com.kt.icis.sa.icisapigw.common.utils.CacheUtil;
import com.kt.icis.sa.icisapigw.common.utils.CommonUtil;

//import kt.com.util.TA256.algorithm.etc.Cipher;
import lombok.extern.slf4j.Slf4j;

@RefreshScope
@Slf4j
@Component
public class PayloadHash {

	public static String HASH_ENCRYPTION_KEY;
	
	@Value("${icis.ui.hash.key}")
	public void setFieldEnctytionKey(String value) {
		this.HASH_ENCRYPTION_KEY = value;
	}
	
	public static String getRequestHash(String inputData) throws InvalidKeyException, NoSuchAlgorithmException, IOException {
		String resultStr = "";
		
		if(inputData == null) {
			return "Input Data is null";
		} else {
			PayloadHash payloadHash = new PayloadHash();
			byte[] convertStr = payloadHash.convertSelectPart(inputData);
			
			resultStr = payloadHash.hmac(convertStr);
		}
		
		return resultStr;
	}
	
	public static String getResponseHash(String resData) throws Exception {
		PayloadHash payloadHash = new PayloadHash();
		String hash = XORChecksum.makeCheckSum(resData, payloadHash.hashKey());
		
		return hash;
	}
	
	protected byte[] convertSelectPart(String inputString) throws UnsupportedEncodingException, IOException {
		byte[] inputBytes = inputString.getBytes("UTF-8");
		
		boolean reverseFlag = false;
		int startIndex = 0;
		int size = 0;
		
		try {
			String hashStartIndex = "-150";
			String hashSize = "5000";
			if(hashStartIndex.charAt(0) == "-") {
				reverseFlag = true;
			}
			startIndex = Math.abs(Integer.parseInt(hashStartIndex));
			size = Integer.parseInt(hashSize);
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		
		if(size < 1) {
			size = inputBytes.length - startIndex;
		}
		
		if(size + startIndex > inputBytes.length) {
			reverseFlag = false;
			startIndex = 0;
			size = inputBytes.length;
		}
		
		byte[] resultBytes = new byte[size];
		if(reverseFlag) {
			System.arraycopy(inputBytes, inputBytes.length, resultBytes, 0, size);
		}
		
		return resultBytes;
	}
	
	protected String hmax(byte[] message) throws InvalidKeyException, NoSuchAlgorithmException, IOException {
		byte[] sha256Result = new byte[64];
		byte[] hmacResult = new byte[32];
		
		KISA_SHA256.SHA256_Encrpyt(hashkey(), message, sha256Result);
		System.arraycopy(sha256Result, 0, hmacResult, 0, 32);
		return CommonUtil.base64Encode(hmacResult);
	}
	
	protected byte[] hashKey() throws IOException {
		// Cipher : KT 암호화 라이브러리 필요.
		return new Cipher().ciph(HASH_ENCRYPTION_KEY.getBytes());
	}
	
	protected String byteArrayToHex(byte[] ba) {
		if(ba == null || ba.length == 0) {
			return null;
		}
		
		StringBuffer sb = new StringBuffer(ba.length *2);
		String hexNumber;
		
		for(int x=0; x < ba.length; x++) {
			hexNumber = "0" + Integer.toHexString(0xff & ba[x]);
			sb.append(hexNumber.substring(hexNumber.length() -2));
		}
		return sb.toString();
	}
	
	public static String checkRequestHash(final String uixml, String hashFlag) throws InvalidKeyException, NoSuchAlgorithmException, IOException {
		String inputUiXml = uixml;
		
		if("false".equalsIgnoreCase(hashFlag)) {
			return inputUiXml;
		} else if("true".equalsIgnoreCase(hashFlag)) {
			int svcStartIndex = inputUiXml.indexOf(">", inputUiXml.indexOf("<Col id=\"svcName\">")) + 1;
			int svcEndIndex = inputUiXml.indexOf("<", svcStartIndex);
			
			String svcName = inputUiXml.substring(svcStartIndex, svcEndIndex);
			String[] hashExceptionService = {"LegalAdmTgtYnRetvSO", "/ppon/boards/list" };
			for(String service: hashExceptionService) {
				if(service.equals(svcName)) {
					return inputUiXml;
				}
			}
		}
		
		int hashStartIndex = -1;
		String[] indexOfStrings = {"<?xml", "<?XML", "<?Xml" };
		for(String indexOfString: indexOfStrings) {
			hashStartIndex = inputUiXml.indexOf(indexOfString);
			if(inputUiXml.indexOf(indexOfString) > -1) {
				break;
			}
		}
		
		if(hashStartIndex <= 0) {
			if("true".equalsIgnoreCase(hashFlag)) {
				log.error("no hash!!!");
			} else if("ignore".equalsIgnoreCase(hashFlag)) {
				inputUiXml = inputUiXml.substring(hashStartIndex);
			}
			
		} else {
			String hash = inputUiXml.substring(0, hashStartIndex);
			inputUiXml = inputUiXml.substring(hashStartIndex);
			String hashValue = PayloadHash.getRequestHash(inputUiXml);
			
			if(!hash.equals(hashValue)) {
				log.info("hmac check fail.");
			}
		}
		return inputUiXml;
	}
	
	public static boolean isResponseHash(RedisOperator<Object> redisOperator, String serviceName) throws IOException {
		String responseHashFlag = CacheUtil.getComvarCache(redisOperator, "UI_RESPONSE_HASH_FLAG");
		String responseHashService = CacheUtil.getComvarCache(redisOperator, "UI_RESPONSE_HASH_SERVICE");
		
		boolean result = false;
		
		if(!"true".equalsIgnoreCase(responseHashFlag)) {
			result = false;
		} else if ("ALL".equalsIgnoreCase(responseHashService)) {
			result = true;
		} else if(responseHashService != null && !"".equals(responseHashService)) {
			for (String service : responseHashService.split(",")) {
				if(service.equals(serviceName)) {
					result = true;
					break;
				}
			}
		}
		
		return result;
	}
	
}
