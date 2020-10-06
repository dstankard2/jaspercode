package net.sf.jaspercode.patterns.xml.java.service;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import net.sf.jaspercode.api.ProcessorContext;
import net.sf.jaspercode.api.annotation.Plugin;
import net.sf.jaspercode.api.annotation.XmlConfig;
import net.sf.jaspercode.patterns.java.service.OperationRenderer;
import net.sf.jaspercode.patterns.java.service.SetVarRenderer;

@Plugin
@XmlConfig
@XmlRootElement(name="setVar")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name="setVar",propOrder={ })
public class SetVarOperation extends Operation {

	@XmlAttribute
	private String name;
	
	@XmlAttribute
	private String type;
	
	@XmlAttribute
	private String value;
	
	public OperationRenderer getRenderer(ProcessorContext ctx) { 
		return new SetVarRenderer(ctx, this); 
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
}

