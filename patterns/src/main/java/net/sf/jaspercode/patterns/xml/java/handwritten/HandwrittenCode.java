package net.sf.jaspercode.patterns.xml.java.handwritten;

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
@XmlRootElement(name="userCode")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name="userCode",propOrder={ })
public class HandwrittenCode extends Component {

	@XmlAttribute(required=true)
	private String path = "";
	
	@XmlAttribute
	private int priority = 0;

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public String getComponentName() {
		return "HandwrittenCode at '"+path+"'";
	}

}
