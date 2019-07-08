package net.sf.jaspercode.patterns.xml.js.template;

import java.util.ArrayList;
import java.util.List;

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
@XmlRootElement(name="templateDirectory")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name="templateDirectory",propOrder={ "folder" })
public class TemplateSets extends Component {

	@XmlAttribute
	private String serviceName = "";

	@XmlElement
	private List<TemplateFolder> folder = new ArrayList<>();
	
	public int getPriority() {
		return PatternPriority.HTML_TEMPLATE;
	}

	public List<TemplateFolder> getFolder() {
		return folder;
	}

	public void setFolder(List<TemplateFolder> folder) {
		this.folder = folder;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	@Override
	public String getComponentName() {
		return "TemplateSets["+serviceName+"]";
	}

}

