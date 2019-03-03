package net.sf.jaspercode.patterns.xml.java.http;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import net.sf.jaspercode.api.annotation.Plugin;
import net.sf.jaspercode.api.annotation.XmlConfig;

@XmlConfig
@Plugin
@XmlRootElement(name="preprocessing")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name="preprocessing",propOrder={  })
public class Preprocessing {

	@XmlAttribute
	private String ref = "";
	
	@XmlAttribute
	private String rule = "";
	
	@XmlAttribute
	private String source = "";

	public String getRef() {
		return ref;
	}

	public void setRef(String ref) {
		this.ref = ref;
	}

	public String getRule() {
		return rule;
	}

	public void setRule(String rule) {
		this.rule = rule;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

}
