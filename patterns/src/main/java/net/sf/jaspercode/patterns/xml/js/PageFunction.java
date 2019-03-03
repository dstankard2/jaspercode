package net.sf.jaspercode.patterns.xml.js;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import net.sf.jaspercode.api.annotation.Plugin;
import net.sf.jaspercode.api.annotation.XmlConfig;
import net.sf.jaspercode.api.config.Component;

@XmlConfig
@Plugin
@XmlRootElement(name="fn")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name="fn",propOrder={ })
public class PageFunction extends Component {

	@XmlAttribute(required = true)
	private String name = null;

	@XmlAttribute
	private String returnType = null;
	
	@XmlAttribute
	private String returnVal = null;

	@XmlAttribute
	private String params = null;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getReturnType() {
		return returnType;
	}

	public void setReturnType(String returnType) {
		this.returnType = returnType;
	}

	public String getReturnVal() {
		return returnVal;
	}

	public void setReturnVal(String returnVal) {
		this.returnVal = returnVal;
	}

	public String getParams() {
		return params;
	}

	public void setParams(String params) {
		this.params = params;
	}
	
}
