package net.sf.jaspercode.patterns.xml.java.http;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import net.sf.jaspercode.api.annotation.ConfigProperty;
import net.sf.jaspercode.api.annotation.Plugin;
import net.sf.jaspercode.api.annotation.XmlConfig;
import net.sf.jaspercode.langsupport.java.JavaComponent;
import net.sf.jaspercode.patterns.PatternPriority;
import net.sf.jaspercode.patterns.http.HttpMethod;

@XmlConfig
@Plugin
@XmlRootElement(name="endpoint")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name="endpoint",propOrder={ "response" })
public class Endpoint extends JavaComponent {

	@XmlAttribute
	private String module = "";

	@XmlAttribute
	private HttpMethod method;

	@XmlAttribute
	private String path = "";
	
	@XmlAttribute
	private String requestBody = "";
	
	@XmlAttribute
	private String requestParameters = "";

	@XmlAttribute
	private Boolean asynch = false;
	
	@XmlAttribute
	private String operation = "";

	@XmlElement
	private List<Response> response = new ArrayList<>();
	
	@XmlTransient
	private String pkg = null;

	@Override
	public int getPriority() {
		return PatternPriority.SERVLET_ENDPOINT;
	}
	
	@XmlTransient
	private String operationResult = null;

	@ConfigProperty(required = true, name = "java.httpendpoint.operationResult",
			description = "Reference to HttpServletRequest object, so it can be used in web service logic", 
			example = "request")
	public void setOperationResult(String operationResult) {
		this.operationResult = operationResult;
	}
	
	public String getOperationResult() {
		return operationResult;
	}

	@Override
	public String getComponentName() {
		if ((getMethod()!=null) && (getPath().trim().length()>0)) {
			return getMethod().name()+" "+getPath();
		} else if ((getMethod()==null) && (getPath().trim().length()==0)) {
			return "<METHOD> <PATH>";
		} else if (getMethod()==null) {
			return "<METHOD> "+getPath();
		}
		return getMethod().name()+" <PATH>";
	}

	public String getModule() {
		return module;
	}

	public void setModule(String module) {
		this.module = module;
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

	public String getRequestParameters() {
		return requestParameters;
	}

	public void setRequestParameters(String requestParameters) {
		this.requestParameters = requestParameters;
	}

	public Boolean getAsynch() {
		return asynch;
	}

	public void setAsynch(Boolean asynch) {
		this.asynch = asynch;
	}

	public String getOperation() {
		return operation;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}

	public List<Response> getResponse() {
		return response;
	}

	public void setResponse(List<Response> response) {
		this.response = response;
	}

	public String getPkg() {
		return pkg;
	}

	@ConfigProperty(required = true, name = "java.webservice.pkg",
			description = "Sub-package that the servlet class is located in, under the Java root package.", 
			example = "servlet")
	public void setPkg(String pkg) {
		this.pkg = pkg;
	}

}

