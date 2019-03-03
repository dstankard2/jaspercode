package net.sf.jaspercode.patterns.tomcat;

public class DataSourceInfo {

	private String name = null;
	
	private String driverClass = null;
	
	private String url = null;
	
	private String username = null;
	
	private String password = null;

	public DataSourceInfo() {
	}

	public DataSourceInfo(String name, String driverClass, String url, String username, String password) {
		super();
		this.name = name;
		this.driverClass = driverClass;
		this.url = url;
		this.username = username;
		this.password = password;
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

	public void setDriverClass(String driverClass) {
		this.driverClass = driverClass;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
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
	
}
