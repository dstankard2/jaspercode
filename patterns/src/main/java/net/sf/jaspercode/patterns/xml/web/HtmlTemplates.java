package net.sf.jaspercode.patterns.xml.web;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import net.sf.jaspercode.api.annotation.Plugin;
import net.sf.jaspercode.api.annotation.XmlConfig;

@XmlConfig
@Plugin
@XmlRootElement(name="mysqlSchema")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name="mysqlSchema",propOrder={ })
public class HtmlTemplates {

	@XmlAttribute
	private String folder = null;
	
	@XmlAttribute
	private String serviceName = null;
	
	@XmlAttribute
	private String ref = null;

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

	public String getRef() {
		return ref;
	}

	public void setRef(String ref) {
		this.ref = ref;
	}
	
}
