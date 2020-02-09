package net.sf.jaspercode.patterns.xml.js.template;

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
@XmlRootElement(name="templateSet")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name="templateSet",propOrder={  })
public class TemplateSet extends Component {

	@XmlAttribute
	private String ref = "";

	@XmlAttribute
	private String path = "";

	@XmlAttribute
	private Integer order = 0;

	public String getRef() {
		return ref;
	}

	public void setRef(String ref) {
		this.ref = ref;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public Integer getOrder() {
		return order;
	}

	public void setOrder(Integer order) {
		this.order = order;
	}

	public int getPriority() {
		return PatternPriority.HTML_TEMPLATE;
	}

	@Override
	public String getComponentName() {
		return "TemplateSet["+ref+"]";
	}

}

