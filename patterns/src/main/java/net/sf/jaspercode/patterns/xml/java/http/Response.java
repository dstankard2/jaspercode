package net.sf.jaspercode.patterns.xml.java.http;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import net.sf.jaspercode.api.annotation.Plugin;
import net.sf.jaspercode.api.annotation.XmlConfig;

@Plugin
@XmlConfig
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name="response",propOrder={  })
public class Response {

	@XmlAttribute
	private String condition = "";
	
	@XmlAttribute
	private Integer httpStatus = null;

	@XmlAttribute
	private String responseBody = "";

	public String getCondition() {
		return condition;
	}

	public void setCondition(String condition) {
		this.condition = condition;
	}

	public Integer getHttpStatus() {
		return httpStatus;
	}

	public void setHttpStatus(Integer httpStatus) {
		this.httpStatus = httpStatus;
	}

	public String getResponseBody() {
		return responseBody;
	}

	public void setResponseBody(String responseBody) {
		this.responseBody = responseBody;
	}

}

