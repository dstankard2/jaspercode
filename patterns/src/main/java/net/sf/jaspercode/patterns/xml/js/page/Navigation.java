package net.sf.jaspercode.patterns.xml.js.page;

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
@XmlRootElement(name="navigation")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name="navigation",propOrder={ })
public class Navigation extends Component {

	@XmlAttribute
	private String containerId = null;
	
	@XmlAttribute
	private String ref = null;
	
	@XmlAttribute
	private String serviceName = null;

}

