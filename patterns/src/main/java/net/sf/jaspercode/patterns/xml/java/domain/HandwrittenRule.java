
package net.sf.jaspercode.patterns.xml.java.domain;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import net.sf.jaspercode.api.annotation.Plugin;
import net.sf.jaspercode.api.annotation.XmlConfig;
import net.sf.jaspercode.patterns.PatternPriority;

@XmlConfig
@Plugin
@XmlRootElement(name="handwrittenRule")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name="handwrittenRule",propOrder={  })
public class HandwrittenRule extends DomainLogicComponent {

	@Override
	public int getPriority() {
		return PatternPriority.HANDWRITTEN_DOMAIN_RULE;
	}

	@XmlAttribute
	private String returnType = "";
	
	@XmlAttribute
	private String rule = "";
	
	@XmlAttribute
	private String params = "";

	public String getReturnType() {
		return returnType;
	}

	public void setReturnType(String returnType) {
		this.returnType = returnType;
	}

	public String getRule() {
		return rule;
	}

	public void setRule(String rule) {
		this.rule = rule;
	}

	public String getParams() {
		return params;
	}

	public void setParams(String params) {
		this.params = params;
	}
	
}

