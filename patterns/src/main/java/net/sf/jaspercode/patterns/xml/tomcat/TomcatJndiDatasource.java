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
import net.sf.jaspercode.patterns.PatternPriority;

@Plugin
@XmlConfig
@XmlRootElement(name="tomcatJndiDatasource")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name="tomcatJndiDatasource",propOrder={ })
public class TomcatJndiDatasource extends Component {

	public TomcatJndiDatasource() {
	}
	
	public int getPriority() {
		return PatternPriority.EMBED_TOMCAT_JAR;
	}

	@XmlAttribute
	private String name = "";
	
	@XmlAttribute
	private String username = "";
	
	@XmlAttribute
	private String password = "";
	
	@XmlAttribute
	private String url = "";
	
	@XmlTransient
	private String driverClass = "";
	
	public String getComponentName() {
		return "TomcatJndiDatasource:"+name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDriverClass() {
		return driverClass;
	}

	@ConfigProperty(description = "JDBC Driver Class", example = "com.mysql.jdbc.Driver", name = "tomcat.jndi.datasource.driverClass", required = true)
	public void setDriverClass(String driverClass) {
		this.driverClass = driverClass;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	
}

