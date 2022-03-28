package net.sf.jaspercode.api.config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

import net.sf.jaspercode.api.annotation.Plugin;

//@Plugin
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name="property",propOrder={ })
public class Property {

	@XmlAttribute(required=true)
	private String name = null;
	
	@XmlValue
	private String value = null;

	public Property() { }
	
	public Property(String name,String value) {
		this.name = name;
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
}
