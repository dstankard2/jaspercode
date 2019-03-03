package net.sf.jaspercode.patterns.xml.js.page;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import net.sf.jaspercode.api.annotation.Plugin;
import net.sf.jaspercode.api.annotation.XmlConfig;
import net.sf.jaspercode.api.config.Component;
import net.sf.jaspercode.patterns.PatternPriority;

@XmlConfig
@Plugin
@XmlRootElement(name="page")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name="page",propOrder={ })
public class Page extends Component {

	public int getPriority() {
		return PatternPriority.PAGE;
	}

	@XmlAttribute(required = true)
	private String name = "";

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public String getComponentName() {
		return "JavascriptPage[name=\""+name+"\"]";
	}
}
