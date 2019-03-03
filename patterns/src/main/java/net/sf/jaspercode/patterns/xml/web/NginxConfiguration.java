package net.sf.jaspercode.patterns.xml.web;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import net.sf.jaspercode.api.annotation.ConfigProperty;
import net.sf.jaspercode.api.annotation.Plugin;
import net.sf.jaspercode.api.annotation.XmlConfig;
import net.sf.jaspercode.api.config.Component;


@XmlConfig
@Plugin
@XmlRootElement(name="nginxConfig")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name="nginxConfig",propOrder={ })
public class NginxConfiguration extends Component {

	@XmlAttribute
	private String configFile = null;
	
	private String nginxExecutable = null;
	
	@ConfigProperty(required = true, name = "nginx.executable",
			description = "Location of nginx executable file", example = "/etc/nginx")
	public void setNginxExecutable(String nginxExecutable) {
		this.nginxExecutable = nginxExecutable;
	}
	
	public String getNginxExecutable() {
		return nginxExecutable;
	}

	public String getConfigFile() {
		return configFile;
	}

	public void setConfigFile(String configFile) {
		this.configFile = configFile;
	}
	
	@Override
	public String getComponentName() {
		return "NGINX Configuration[File: "+getConfigFile()+"]";
	}
	
}

