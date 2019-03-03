package net.sf.jaspercode.patterns.xml.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import net.sf.jaspercode.api.annotation.Plugin;
import net.sf.jaspercode.api.annotation.RequiredXml;
import net.sf.jaspercode.api.annotation.XmlConfig;
import net.sf.jaspercode.api.config.Component;
import net.sf.jaspercode.patterns.PatternPriority;

@Plugin
@XmlConfig
@XmlRootElement(name="searchIndex")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name="searchIndex",propOrder={ })
public class SearchIndex extends Component {

	public SearchIndex() {
	}

	public int getPriority() {
		return PatternPriority.SEARCH_INDEX;
	}

	@RequiredXml
	@XmlAttribute
	private String name = "";
	
	@RequiredXml
	@XmlAttribute
	private String params = "";
	
	@RequiredXml
	@XmlAttribute
	private String entity = "";
	
	@RequiredXml
	@XmlAttribute
	private String queryString = "";
	
	@XmlAttribute
	@RequiredXml
	private String jpaDaoFactoryRef = "";
	
	@XmlAttribute
	@RequiredXml
	private Boolean multiple;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getParams() {
		return params;
	}

	public void setParams(String params) {
		this.params = params;
	}

	public String getEntity() {
		return entity;
	}

	public void setEntity(String entity) {
		this.entity = entity;
	}

	public String getQueryString() {
		return queryString;
	}

	public void setQueryString(String queryString) {
		this.queryString = queryString;
	}

	public String getJpaDaoFactoryRef() {
		return jpaDaoFactoryRef;
	}

	public void setJpaDaoFactoryRef(String jpaDaoFactoryRef) {
		this.jpaDaoFactoryRef = jpaDaoFactoryRef;
	}

	public Boolean getMultiple() {
		return multiple;
	}

	public void setMultiple(Boolean multiple) {
		this.multiple = multiple;
	}

	@Override
	public String getComponentName() {
		return "SearchIndex["+getEntity()+"."+name+"]";
	}

}

