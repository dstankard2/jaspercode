package net.sf.jaspercode.api.config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import net.sf.jaspercode.api.annotation.Plugin;
import net.sf.jaspercode.api.annotation.XmlConfig;

@Plugin
@XmlConfig
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name="buildComponent",propOrder={ })
@XmlRootElement(name="buildComponent")
public abstract class BuildComponent extends Component {

	@XmlAttribute
	private String buildCommand;

	@XmlAttribute
	private String deployCommand;

	public String getBuildCommand() {
		return buildCommand;
	}
	public void setBuildCommand(String buildCommand) {
		this.buildCommand = buildCommand;
	}
	public String getDeployCommand() {
		return deployCommand;
	}
	public void setDeployCommand(String deployCommand) {
		this.deployCommand = deployCommand;
	}

}
