package net.sf.jaspercode.patterns.xml.tomcat;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import net.sf.jaspercode.api.annotation.ConfigProperty;
import net.sf.jaspercode.api.annotation.Plugin;
import net.sf.jaspercode.api.annotation.XmlConfig;
import net.sf.jaspercode.api.config.Component;
import net.sf.jaspercode.langsupport.java.JavaUtils;
import net.sf.jaspercode.patterns.PatternPriority;

@Plugin
@XmlConfig
@XmlRootElement(name="embedTomcatJar")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name="embedTomcatJar",propOrder={ })
public class EmbedTomcatJar extends Component {

	public EmbedTomcatJar() {
	}

	public int getPriority() {
		return PatternPriority.EMBED_TOMCAT_JAR;
	}
	
	@XmlAttribute
	private String jarName = "";
	
	@XmlAttribute
	private String context = "";

	@XmlAttribute
	private Integer port = null;

	@XmlTransient
	private String pkg = null;
	
	@XmlTransient
	private Integer debugPort = null;
	
	public String getPkg() {
		return pkg;
	}
	
	public String getJarName() {
		return jarName;
	}

	public void setJarName(String jarName) {
		this.jarName = jarName;
	}

	public String getContext() {
		return context;
	}

	public void setContext(String context) {
		this.context = context;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}
	
	public String getComponentName() {
		if (jarName.length() >0) {
			return jarName+".jar";
		}
		return super.getComponentName();
	}
	
	@ConfigProperty(required = true, name = JavaUtils.CONFIG_PROPERTY_JAVA_ROOT_PACKAGE,
			description = "Root Java package of the code distribution.", example = "net.sf.jaspercode")
	public void setJavaRootPackage(String pkg) {
		this.pkg = pkg;
	}

	@ConfigProperty(required = false, name = "tomcat.debugPort",
			description = "TCP/IP Port to start Tomcat JDWP debugging on", example = "3333")
	public void setDebugPort(Integer debugPort) {
		this.debugPort = debugPort;
	}
	
	public Integer getDebugPort() {
		return debugPort;
	}

}

