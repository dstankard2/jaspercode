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
@XmlRootElement(name="classification")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name="classification",propOrder={ })
public class Classification extends JavaComponent {

	@XmlTransient
	private String pkg = null;
	
	@XmlAttribute
	private String name = "";
	
	@XmlAttribute
	private String attributes = "";

	@XmlAttribute(name = "extends")
	private String extend = "";

	@Override
	public String getComponentName() {
		return "Classification["+name+"]";
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAttributes() {
		return attributes;
	}

	public void setAttributes(String attributes) {
		this.attributes = attributes;
	}
	
	public String getExtend() {
		return extend;
	}

	public void setExtend(String extend) {
		this.extend = extend;
	}

	public int getPriority() {
		return PatternPriority.CLASSIFICATION;
	}

	public String getPkg() {
		return pkg;
	}

	@ConfigProperty(required = true, name = "java.classification.pkg",
			description = "Sub-package that the classification Java interface will be created in, under the Java root package.", 
			example = "dto")
	public void setPkg(String pkg) {
		this.pkg = pkg;
	}

}
