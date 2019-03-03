package net.sf.jaspercode.patterns.xml.java.dataobject;

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
@XmlRootElement(name="ordinalEnum")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name="ordinalEnum",propOrder={ "value" })
public class OrdinalEnum extends JavaComponent {

	@XmlTransient
	private String pkg = null;

	@XmlElement
	private List<String> value = new ArrayList<>();
	
	@XmlAttribute
	private String name = "";

	@XmlAttribute
	private String ref = "";

	public int getPriority() {
		return PatternPriority.JAVA_ENUM;
	}

	public String getPkg() {
		return pkg;
	}

	@ConfigProperty(required = true, name = "java.enum.pkg",
			description = "Sub-package that the enumeration class will be created in, under the Java root package.", 
			example = "enum")
	public void setPkg(String pkg) {
		this.pkg = pkg;
	}

	public String getComponentName() {
		return "Java Enumeration:"+getName();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<String> getValue() {
		return value;
	}

	public void setValue(List<String> value) {
		this.value = value;
	}

	public String getRef() {
		return ref;
	}

	public void setRef(String ref) {
		this.ref = ref;
	}

}
