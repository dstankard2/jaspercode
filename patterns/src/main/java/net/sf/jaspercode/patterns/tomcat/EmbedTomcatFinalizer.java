package net.sf.jaspercode.patterns.tomcat;

import net.sf.jaspercode.api.RuntimePlatform;
import net.sf.jaspercode.api.annotation.ConfigProperty;
import net.sf.jaspercode.api.annotation.Plugin;
import net.sf.jaspercode.api.config.Component;
import net.sf.jaspercode.langsupport.java.JavaUtils;

@Plugin
public class EmbedTomcatFinalizer extends Component {
	RuntimePlatform platform = null;
	String jarName = "";
	String contextRoot = "";
	int port = 0;
	String pkg = null;

	public String getPkg() {
		return pkg;
	}
	
	@ConfigProperty(required = true, name = JavaUtils.CONFIG_PROPERTY_JAVA_ROOT_PACKAGE,
			description = "Root Java package of the code distribution.", example = "com.mycomponent.myapp")
	public void setPkg(String pkg) {
		this.pkg = pkg;
	}
	
	public EmbedTomcatFinalizer(String jarName,String contextRoot,int port) {
		this.jarName = jarName;
		this.contextRoot = contextRoot;
		this.port = port;
	}
	
	public String getComponentName() {
		return "EmbedTomcatJar["+jarName+"]";
	}
	
	public String getPatternName() {
		return "EmbedTomcatFinalizer";
	}

	@Override
	public int getPriority() {
		return 100000;
	}

	public RuntimePlatform getPlatform() {
		return platform;
	}

	public void setPlatform(RuntimePlatform platform) {
		this.platform = platform;
	}

	public String getJarName() {
		return jarName;
	}

	public void setJarName(String jarName) {
		this.jarName = jarName;
	}

	public String getContextRoot() {
		return contextRoot;
	}

	public void setContextRoot(String contextRoot) {
		this.contextRoot = contextRoot;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}
	

}
