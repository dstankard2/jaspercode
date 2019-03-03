package net.sf.jaspercode.patterns.xml.java.service;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import net.sf.jaspercode.api.CodeExecutionContext;
import net.sf.jaspercode.api.ProcessorContext;
import net.sf.jaspercode.api.annotation.Plugin;
import net.sf.jaspercode.api.annotation.XmlConfig;
import net.sf.jaspercode.patterns.java.service.CallValidationRuleRenderer;
import net.sf.jaspercode.patterns.java.service.OperationRenderer;

@Plugin
@XmlConfig
@XmlRootElement(name="callValidationRule")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name="callValidationRule",propOrder={ "operation" })
public class CallValidationRuleOperation extends Operation implements NestingOperation,ResultOperation {

	@Override
	public OperationRenderer getRenderer(ProcessorContext ctx) {
		return new CallValidationRuleRenderer(ctx, this); 
	}

	@XmlAttribute
	private String params = "";
	
	@XmlAttribute
	private String rule = "";
	
	@XmlElementRef
	private List<Operation> operation = new ArrayList<Operation>();

	@Override
	public List<Operation> getOperation() {
		return operation;
	}

	public String getRule() {
		return rule;
	}

	public void setRule(String rule) {
		this.rule = rule;
	}

	public String getParams() {
		return params;
	}

	public void setParams(String params) {
		this.params = params;
	}

	@Override
	public String getResultType(ProcessorContext ctx,CodeExecutionContext execCtx) {
		return "string";
	}

	@Override
	public String getResultName(ProcessorContext ctx,CodeExecutionContext execCtx) {
		return "returnValue.validationError";
	}
	
}

