package net.sf.jaspercode.patterns.xml.java.dataobject;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
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
@XmlRootElement(name="dataObject")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name="dataObject",propOrder={ })
public class DataObject extends JavaComponent {

	@XmlTransient
	private String pkg = null;

	@XmlAttribute(name = "extends")
	private String extend = "";

	@XmlAttribute
	private String attributes = "";

	public int getPriority() {
		return PatternPriority.DATA_OBJECT;
	}

	@XmlAttribute
	private String name = "";
	
	public String getPkg() {
		return pkg;
	}

	@ConfigProperty(required = true, name = "java.dataObject.pkg",
			description = "Sub-package that the data object class will be created in, under the Java root package.", 
			example = "dto")
	public void setPkg(String pkg) {
		this.pkg = pkg;
	}

	public String getAttributes() {
		return attributes;
	}

	public void setAttributes(String attributes) {
		this.attributes = attributes;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getExtend() {
		return extend;
	}

	public void setExtend(String extend) {
		this.extend = extend;
	}

	public String getComponentName() {
		return "DataObject:"+getName();
	}

}
