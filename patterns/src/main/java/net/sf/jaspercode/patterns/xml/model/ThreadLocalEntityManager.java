package net.sf.jaspercode.patterns.xml.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import net.sf.jaspercode.api.annotation.ConfigProperty;
import net.sf.jaspercode.api.annotation.Plugin;
import net.sf.jaspercode.api.annotation.XmlConfig;
import net.sf.jaspercode.langsupport.java.JavaComponent;
import net.sf.jaspercode.patterns.PatternPriority;

@Plugin
@XmlConfig
@XmlRootElement(name="threadLocalEntityManager")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name="threadLocalEntityManager",propOrder={ })
public class ThreadLocalEntityManager extends JavaComponent {

	private ThreadLocalEntityManager() {
	}

	private String pkg;

	@XmlAttribute
	private String name = "";

	@XmlAttribute
	private String txRef = "";
	
	public int getPriority() {
		return PatternPriority.THREAD_LOCAL_ENTITY_MANAGER;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTxRef() {
		return txRef;
	}

	public void setTxRef(String txRef) {
		this.txRef = txRef;
	}

	@ConfigProperty(required = true, name = "java.model.package", example = "model",
			description = "Sub-package that the data object class will be created in, under the Java root package.")
	public void setPkg(String pkg) {
		this.pkg = pkg;
	}
	
	public String getPkg() {
		return this.pkg;
	}
	
	@Override
	public String getComponentName() {
		return "ThreadLocalEntityManager['"+name+"']";
	}

}
