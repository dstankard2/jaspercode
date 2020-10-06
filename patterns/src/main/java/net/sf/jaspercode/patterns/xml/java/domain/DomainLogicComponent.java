package net.sf.jaspercode.patterns.xml.java.domain;

import net.sf.jaspercode.api.annotation.ConfigProperty;
import net.sf.jaspercode.langsupport.java.JavaComponent;

public abstract class DomainLogicComponent extends JavaComponent {

	private String pkg = "";
	private String serviceName = "";
	private String dependencies = "";
	private String serviceGroupName = "";
	private String implClass = "";

	public String getPkg() {
		return pkg;
	}

	@ConfigProperty(required = true, name = "java.domain.pkg",
			description = "Sub-package that the business logic class is located in, under the Java root package.", 
			example = "domain")
	public void setPkg(String pkg) {
		this.pkg = pkg;
	}

	public String getServiceName() {
		return serviceName;
	}

	@ConfigProperty(required = true, name = "java.domain.serviceClass",
			description = "Name of the Java class that the service will reside in.", 
			example = "UserDomainService")
	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public String getDependencies() {
		return dependencies;
	}

	@ConfigProperty(required = false, name = "java.domain.depedencyRefs",
			description = "Comma-delimited list of system attributes that this component uses to resolve the rule to be generated.", 
			example = "daoFactory,service,anotherService")
	public void setDependencies(String dependencies) {
		this.dependencies = dependencies;
	}

	public String getServiceGroupName() {
		return serviceGroupName;
	}

	@ConfigProperty(required = false, name = "java.domain.serviceGroup",
			description = "Upper-camel name of service grouping that this object will belong to.", 
			example = "DomainServices")
	public void setServiceGroupName(String serviceGroupName) {
		this.serviceGroupName = serviceGroupName;
	}

	public String getImplClass() {
		return implClass;
	}

	@ConfigProperty(required = false, name = "java.domain.implClass",
			description = "Fully qualified name of handwritten class that will subclass the generated business logic class.  Required for handwritten rules.", 
			example = "com.xyz.service.MyServiceImpl")
	public void setImplClass(String implClass) {
		this.implClass = implClass;
	}

}

