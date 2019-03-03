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

@XmlConfig
@Plugin
@XmlRootElement(name="webServiceModule")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name="webServiceModule",propOrder={ "preprocessing" })
public class WebServiceModule extends JavaComponent {

	@XmlTransient
	private String requestRef = "";
	
	public String getRequestRef() {
		return requestRef;
	}

	@ConfigProperty(required = false, name = "java.http.requestRef",
			description = "Reference to HttpServletRequest object, so it can be used in web service logic", 
			example = "request")
	public void setRequestRef(String requestRef) {
		this.requestRef = requestRef;
	}

	@XmlTransient
	private String responseRef = "";
	
	public String getResponseRef() {
		return responseRef;
	}

	@ConfigProperty(required = false, name = "java.http.responseRef",
			description = "Reference to HttpServletResponse object, so it can be used in web service logic", 
			example = "response")
	public void setResponseRef(String responseRef) {
		this.responseRef = responseRef;
	}

	
	
	@XmlTransient
	private String pkg = null;

	@XmlElement
	private List<Preprocessing> preprocessing = new ArrayList<>();

	@XmlAttribute
	private String name = "";
	
	@XmlAttribute
	private String uri = "";
	
	@XmlAttribute
	private String filterGroup = "";

	public String getComponentName() {
		return "WebServiceModule["+name+"]";
	}

	public int getPriority() {
		return PatternPriority.SERVLET_MODULE;
	}
	
	public List<Preprocessing> getPreprocessing() {
		return preprocessing;
	}

	public void setPreprocessing(List<Preprocessing> preprocessing) {
		this.preprocessing = preprocessing;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public String getFilterGroup() {
		return filterGroup;
	}

	public void setFilterGroup(String filterGroup) {
		this.filterGroup = filterGroup;
	}

	public String getPkg() {
		return pkg;
	}

	@ConfigProperty(required = true, name = "java.webservice.pkg",
			description = "Sub-package that the servlet class will be created in, under the Java root package.", 
			example = "servlet")
	public void setPkg(String pkg) {
		this.pkg = pkg;
	}

}

