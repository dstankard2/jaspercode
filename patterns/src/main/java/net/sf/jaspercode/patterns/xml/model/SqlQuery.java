package net.sf.jaspercode.patterns.xml.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import net.sf.jaspercode.api.annotation.ConfigProperty;
import net.sf.jaspercode.api.annotation.XmlConfig;
import net.sf.jaspercode.langsupport.java.JavaComponent;
import net.sf.jaspercode.patterns.PatternPriority;

//@Plugin
@XmlConfig
@XmlRootElement(name="nativeQuery")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name="nativeQuery",propOrder={ "query" })
public class SqlQuery extends JavaComponent {

	@XmlTransient
	private String pkg = null;
	
	@XmlAttribute
	private String service = null;

	@XmlAttribute
	private String resultType = "";

	@XmlAttribute
	private String ruleName = "";

	@XmlElement(name = "query")
	private String query = "";
	
	@XmlAttribute
	private String emLocator = "";

	public SqlQuery() {
	}
	
	public String getEmLocator() {
		return emLocator;
	}

	public void setEmLocator(String emLocator) {
		this.emLocator = emLocator;
	}

	@ConfigProperty(required = true, name = "java.model.package", example = "model",
			description = "Sub-package that the data object class will be created in, under the Java root package.")
	public void setPkg(String pkg) {
		this.pkg = pkg;
	}
	
	public String getPkg() {
		return pkg;
	}

	@Override
	public int getPriority() {
		return PatternPriority.JPA_DAO_FACTORY + PatternPriority.DATA_OBJECT / 2;
	}

	public String getComponentName() {
		return "SqlQuery["+getRuleName()+"]";
	}

	public String getRuleName() {
		return ruleName;
	}

	public void setRuleName(String ruleName) {
		this.ruleName = ruleName;
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public String getService() {
		return service;
	}

	public void setService(String service) {
		this.service = service;
	}

	public String getResultType() {
		return resultType;
	}

	public void setResultType(String resultType) {
		this.resultType = resultType;
	}

}

