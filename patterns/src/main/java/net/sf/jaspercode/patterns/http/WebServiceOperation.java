package net.sf.jaspercode.patterns.http;

import java.util.ArrayList;
import java.util.List;

public class WebServiceOperation {

	private String operationName = null;
	private HttpMethod method = null;
	private String path = null;
	private String requestBody = null;
	private String requestBodyFormat = null;
	private String responseBody = null;
	private String responseBodyFormat = null;
	private List<String> requestParameters = new ArrayList<>();

	public WebServiceOperation() {
	}

	public String getOperationName() {
		return operationName;
	}

	public void setOperationName(String operationName) {
		this.operationName = operationName;
	}

	public HttpMethod getMethod() {
		return method;
	}

	public void setMethod(HttpMethod method) {
		this.method = method;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getRequestBody() {
		return requestBody;
	}

	public void setRequestBody(String requestBody) {
		this.requestBody = requestBody;
	}

	public String getRequestBodyFormat() {
		return requestBodyFormat;
	}

	public void setRequestBodyFormat(String requestBodyFormat) {
		this.requestBodyFormat = requestBodyFormat;
	}

	public String getResponseBody() {
		return responseBody;
	}

	public void setResponseBody(String responseBody) {
		this.responseBody = responseBody;
	}

	public String getResponseBodyFormat() {
		return responseBodyFormat;
	}

	public void setResponseBodyFormat(String responseBodyFormat) {
		this.responseBodyFormat = responseBodyFormat;
	}

	public List<String> getRequestParameters() {
		return requestParameters;
	}

	public void setRequestParameters(List<String> requestParameters) {
		this.requestParameters = requestParameters;
	}
	
	// The path string has already been validated
	public List<String> getPathVariables() {
		List<String> ret = new ArrayList<>();
		String val = this.path;
		
		int i = val.indexOf('{');
		while(i>=0) {
			int end = val.indexOf('}',i+1);
			ret.add(val.substring(i+1, end));
			val = val.substring(0, i) + val.substring(end+1);
			i = val.indexOf('{');
		}
		return ret;
	}
	
}
