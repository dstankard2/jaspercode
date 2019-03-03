package net.sf.jaspercode.langsupport.java;

import net.sf.jaspercode.api.annotation.ConfigProperty;
import net.sf.jaspercode.api.config.Component;

public abstract class JavaComponent extends Component {

	private String javaRootPackage = null;

	public abstract String getPkg();

	public String getJavaRootPackage() {
		return javaRootPackage;
	}
	
	public String getFullPackage() {
		return javaRootPackage + '.' + getPkg();
	}

	@ConfigProperty(required = true, name = JavaUtils.CONFIG_PROPERTY_JAVA_ROOT_PACKAGE,
			description = "Root Java package of the code distribution.", example = "net.sf.jaspercode")
	public void setJavaRootPackage(String javaRootPackage) {
		this.javaRootPackage = javaRootPackage;
	}

}
