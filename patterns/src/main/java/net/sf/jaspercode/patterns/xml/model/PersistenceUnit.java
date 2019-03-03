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
@XmlRootElement(name="persistenceUnit")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name="persistenceUnit",propOrder={ })
public class PersistenceUnit extends JavaComponent {

	@XmlAttribute
	private String txRef = "";
	
	@XmlAttribute
	private String name = "";
	
	@XmlAttribute
	private String tableSetId = "";

	@XmlTransient
	private String pkg = null;

	public int getPriority() {
		return PatternPriority.PERSISTENCE_UNIT;
	}
	
	public String getTxRef() {
		return txRef;
	}

	public void setTxRef(String txRef) {
		this.txRef = txRef;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPkg() {
		return pkg;
	}

	@ConfigProperty(required = true, name = "java.entity.package", example = "entity",
			description = "Sub-package that the data object class will be created in, under the Java root package.")
	public void setPkg(String pkg) {
		this.pkg = pkg;
	}

	public String getTableSetId() {
		return tableSetId;
	}

	public void setTableSetId(String tableSetId) {
		this.tableSetId = tableSetId;
	}
	
	public String getComponentName() {
		return "PersistenceUnit:"+getName();
	}

}

