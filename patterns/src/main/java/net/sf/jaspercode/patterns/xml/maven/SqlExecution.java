package net.sf.jaspercode.patterns.xml.maven;

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
@XmlRootElement(name="sql")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name="sql",propOrder={ })
public class SqlExecution extends Component {

	@XmlAttribute(required = true)
	private String id = "";
	
	@XmlAttribute(required = true)
	private String resource = "";

	private String jdbcUrl = null;
	@ConfigProperty(description = "URL of database to connect to, or \"ENV:<env_var>\"", name = "maven.sql.jdbcUrl", required = true)
	public void setJdbcUrl(String url) {
		this.jdbcUrl = url;
	}
	public String getJdbcUrl() {
		return jdbcUrl;
	}
	
	private String username = null;
	@ConfigProperty(description = "Username to use to connect to database, or \"ENV:<env_var>\"", name = "maven.sql.username", required = true)
	public void setUsername(String username) {
		this.username = username;
	}
	public String getUsername() {
		return username;
	}
	
	private String password = null;
	@ConfigProperty(description = "Passwordto use to connect to database, or \"ENV:<env_var>\"", name = "maven.sql.password", required = true)
	public void setPassword(String password) {
		this.password = password;
	}
	public String getPassword() {
		return password;
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getResource() {
		return resource;
	}

	public void setResource(String resource) {
		this.resource = resource;
	}

}
