package net.sf.jaspercode.patterns.xml.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import net.sf.jaspercode.api.annotation.ConfigProperty;
import net.sf.jaspercode.api.annotation.Plugin;
import net.sf.jaspercode.api.annotation.XmlConfig;
import net.sf.jaspercode.langsupport.java.JavaComponent;
import net.sf.jaspercode.patterns.PatternPriority;

@Plugin
@XmlConfig
@XmlRootElement(name="jpaDaoFactory")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name="jpaDaoFactory",propOrder={ })
public class JpaDaoFactory extends JavaComponent {

	public JpaDaoFactory() {
	}

	public int getPriority() {
		return PatternPriority.JPA_DAO_FACTORY;
	}

	@XmlAttribute
	private String ref = "";
	
	@XmlAttribute
	private String entityManagerLocator = "";
	
	@XmlAttribute
	private String name = "";
	
	@XmlAttribute
	private String selectByIndex = "";
	
	@XmlTransient
	private String pkg = "";
	
	public String getRef() {
		return ref;
	}

	public void setRef(String ref) {
		this.ref = ref;
	}

	public String getEntityManagerLocator() {
		return entityManagerLocator;
	}

	public void setEntityManagerLocator(String entityManagerLocator) {
		this.entityManagerLocator = entityManagerLocator;
	}

	public String getSelectByIndex() {
		return selectByIndex;
	}

	public void setSelectByIndex(String selectByIndex) {
		this.selectByIndex = selectByIndex;
	}

	@ConfigProperty(required = true, name = "java.model.package", example = "model",
			description = "Sub-package that the data object class will be created in, under the Java root package.")
	public void setPkg(String pkg) {
		this.pkg = pkg;
	}
	
	public String getPkg() {
		return pkg;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getComponentName() {
		return "JpaDaoFactory['"+getName()+"']";
	}

}
