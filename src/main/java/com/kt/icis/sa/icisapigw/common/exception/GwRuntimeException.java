package com.kt.icis.sa.icisapigw.common.exception;

public class GwRuntimeException extends DefaultNestedRuntimeException {
	
	private static final long serialVersionUID = -9096806026148593245L;
	
	public GwRuntimeException(String code) {
		super(code);
	}
	
	public GwRuntimeException(String code, String reason) {
		super(code, reason);
	}
	
	public GwRuntimeException(String code, String reason, Object data) {
		super(code, reason, data);
	}
	
	public GwRuntimeException(String code, String reason, Throwable cause) {
		super(code, reason, cause);
	}
	
	public GwRuntimeException(String code, String reason, Object data, Throwable cause) {
		super(code, reason, data, cause);
	}

}
