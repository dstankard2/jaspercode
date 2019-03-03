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
@XmlRootElement(name="pageModel")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name="pageModel",propOrder={ })
public class PageModel extends Component {

	public int getPriority() {
		return PatternPriority.PAGE_MODEL;
	}

	@XmlAttribute(required = true)
	private String pageName = "";

	@XmlAttribute(required = true)
	private String attributes = "";

	public String getPageName() {
		return pageName;
	}

	public void setPageName(String pageName) {
		this.pageName = pageName;
	}

	public String getAttributes() {
		return attributes;
	}

	public void setAttributes(String attributes) {
		this.attributes = attributes;
	}

	@Override
	public String getComponentName() {
		return "PageModel["+getPageName()+"]";
	}

}

