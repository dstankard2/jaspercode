package net.sf.jaspercode.patterns.http;

public enum HttpMethod {

	GET("GET"),
	POST("POST"),
	PUT("PUT"),
	DELETE("DELETE");
	
	private String method;
	
	private HttpMethod(String method) {
		this.method = method;
	}

	public String value() {
		return method;
	}

}

