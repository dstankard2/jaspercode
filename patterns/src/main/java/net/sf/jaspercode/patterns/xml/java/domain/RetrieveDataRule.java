
package net.sf.jaspercode.patterns.xml.java.domain;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import net.sf.jaspercode.api.annotation.Plugin;
import net.sf.jaspercode.api.annotation.RequiredXml;
import net.sf.jaspercode.api.annotation.XmlConfig;
import net.sf.jaspercode.patterns.PatternPriority;

@XmlConfig
@Plugin
@XmlRootElement(name="retrieveDataRule")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name="retrieveDataRule",propOrder={  })
public class RetrieveDataRule extends DomainLogicComponent {

	@Override
	public int getPriority() {
		return PatternPriority.DOMAIN_RULE;
	}

	@RequiredXml
	@XmlAttribute
	private String returnAttribute = "";
	
	@RequiredXml
	@XmlAttribute
	private String name = "";

	@RequiredXml
	@XmlAttribute
	private String params = "";

	public String getReturnAttribute() {
		return returnAttribute;
	}

	public void setReturnAttribute(String returnAttribute) {
		this.returnAttribute = returnAttribute;
	}

	public String getParams() {
		return params;
	}

	public void setParams(String params) {
		this.params = params;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getComponentName() {
		return "RetrieveDataRule["+this.getServiceName()+"."+name+"]";
	}

}

