package net.sf.jaspercode.patterns.xml.java.service;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import net.sf.jaspercode.api.ProcessorContext;
import net.sf.jaspercode.api.annotation.Plugin;
import net.sf.jaspercode.api.annotation.XmlConfig;
import net.sf.jaspercode.patterns.java.service.JpaTxRenderer;
import net.sf.jaspercode.patterns.java.service.OperationRenderer;

@Plugin
@XmlConfig
@XmlRootElement(name="jpaTx")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name="jpaTx",propOrder={ "operation" })
public class JpaTxOperation extends Operation implements NestingOperation {

	@Override
	public OperationRenderer getRenderer(ProcessorContext ctx) { return new JpaTxRenderer(ctx,this); }

	@XmlElementRef
	private List<Operation> operation = new ArrayList<Operation>();
	
	@XmlAttribute
	private String locator = "";
	
	@XmlAttribute
	private Boolean commit = Boolean.FALSE;
	
	@XmlAttribute
	private String ref = "";
	
	public List<Operation> getOperation() {
		return operation;
	}

	public void setOperation(List<Operation> operation) {
		this.operation = operation;
	}

	public String getLocator() {
		return locator;
	}

	public void setLocator(String locator) {
		this.locator = locator;
	}

	public Boolean getCommit() {
		return commit;
	}

	public void setCommit(Boolean commit) {
		this.commit = commit;
	}

	public String getRef() {
		return ref;
	}

	public void setRef(String ref) {
		this.ref = ref;
	}

}

