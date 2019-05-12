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
@XmlType(name="templateDirectory",propOrder={ "subfolder" })
public class TemplateDirectory extends Component {

	@XmlAttribute(required = true)
	private String folder = "";

	@XmlAttribute
	private String serviceName = "";

	@XmlElement
	private List<Subfolder> subfolder = new ArrayList<>();
	
	public int getPriority() {
		return PatternPriority.HTML_TEMPLATE;
	}

	public String getFolder() {
		return folder;
	}

	public void setFolder(String folder) {
		this.folder = folder;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public List<Subfolder> getSubfolder() {
		return subfolder;
	}

	public void setSubfolder(List<Subfolder> subfolder) {
		this.subfolder = subfolder;
	}
	
	@Override
	public String getComponentName() {
		return "TemplateDirectory["+serviceName+"]";
	}

}

