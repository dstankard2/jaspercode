package net.sf.jaspercode.patterns.java.domain;

import java.util.List;

import net.sf.jaspercode.api.types.ServiceOperation;
import net.sf.jaspercode.langsupport.java.types.impl.JavaServiceType;

public class DomainDataRule {

	private String attribute = null;
	private JavaServiceType serviceType = null;
	private String serviceRef = null;
	private List<ServiceOperation> operations = null;

	public String getAttribute() {
		return attribute;
	}

	public void setAttribute(String attribute) {
		this.attribute = attribute;
	}

	public JavaServiceType getServiceType() {
		return serviceType;
	}

	public void setServiceType(JavaServiceType serviceType) {
		this.serviceType = serviceType;
	}

	public String getServiceRef() {
		return serviceRef;
	}

	public void setServiceRef(String ref) {
		this.serviceRef = ref;
	}

	public List<ServiceOperation> getOperations() {
		return operations;
	}

	public void setOperations(List<ServiceOperation> operations) {
		this.operations = operations;
	}

}
