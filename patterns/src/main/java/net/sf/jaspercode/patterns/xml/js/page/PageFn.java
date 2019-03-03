package net.sf.jaspercode.patterns.xml.js.page;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import net.sf.jaspercode.api.annotation.Plugin;
import net.sf.jaspercode.api.annotation.XmlConfig;
import net.sf.jaspercode.api.config.Component;
import net.sf.jaspercode.patterns.PatternPriority;

@XmlConfig
@Plugin
@XmlRootElement(name="pageFn")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name="pageFn",propOrder={ })
public class PageFn extends Component {

	public int getPriority() {
		return PatternPriority.PAGE_FN;
	}

	@XmlAttribute
	private Boolean async = false;
	
	@XmlAttribute
	private String pageName = "";
	
	@XmlAttribute
	private String service = "";
	
	@XmlAttribute
	private String name = "";

	@XmlAttribute
	private String event = "";

	@XmlElement(name = "code")
	private String code = "";
	
	public String getEvent() {
		return event;
	}

	public void setEvent(String event) {
		this.event = event;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getPageName() {
		return pageName;
	}

	public void setPageName(String pageName) {
		this.pageName = pageName;
	}

	public String getService() {
		return service;
	}

	public void setService(String service) {
		this.service = service;
	}

	public Boolean getAsync() {
		return async;
	}

	public void setAsync(Boolean async) {
		this.async = async;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public String getComponentName() {
		return "PageFn ["+pageName+"]["+name+"]";
	}

}
