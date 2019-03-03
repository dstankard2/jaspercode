package net.sf.jaspercode.patterns.xml.java.service;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import net.sf.jaspercode.api.annotation.ConfigProperty;
import net.sf.jaspercode.api.annotation.Plugin;
import net.sf.jaspercode.api.annotation.XmlConfig;
import net.sf.jaspercode.langsupport.java.JavaComponent;
import net.sf.jaspercode.patterns.PatternPriority;

@XmlConfig
@Plugin
@XmlRootElement(name="service")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name="service",propOrder={ })
public class Service extends JavaComponent {

	private String pkg = null;

	@XmlElementRef
	private List<Operation> serviceOperation = new ArrayList<Operation>();

	@XmlAttribute
	private String params = null;
	
	@XmlAttribute
	private String module = null;

	@XmlAttribute
	private String name = null;

	public List<Operation> getServiceOperation() {
		return serviceOperation;
	}

	public void setServiceOperation(List<Operation> serviceOperation) {
		this.serviceOperation = serviceOperation;
	}

	public String getParams() {
		return params;
	}

	public void setParams(String params) {
		this.params = params;
	}

	public String getModule() {
		return module;
	}

	public void setModule(String module) {
		this.module = module;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public int getPriority() {
		return PatternPriority.BUSINESS_SERVICE;
	}
	
	@Override
	public String getComponentName() {
		StringBuilder b = new StringBuilder();
		b.append(getModule()+'.'+getName()+'(');
		b.append(this.getParams()+')');
		return b.toString();
	}

	public String getPkg() {
		return pkg;
	}

	@ConfigProperty(required = true, name = "java.service.pkg",
			description = "Sub-package that the data object class will be created in, under the Java root package.", 
			example = "dto")
	public void setPkg(String pkg) {
		this.pkg = pkg;
	}

}
