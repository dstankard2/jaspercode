package net.sf.jaspercode.patterns.java.domain;

import java.util.List;

import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.MethodSource;

import net.sf.jaspercode.api.AttribEntry;
import net.sf.jaspercode.api.ComponentProcessor;
import net.sf.jaspercode.api.JasperUtils;
import net.sf.jaspercode.api.ProcessorContext;
import net.sf.jaspercode.api.annotation.Plugin;
import net.sf.jaspercode.api.annotation.Processor;
import net.sf.jaspercode.api.config.Component;
import net.sf.jaspercode.api.exception.JasperException;
import net.sf.jaspercode.api.types.ServiceOperation;
import net.sf.jaspercode.langsupport.java.JavaClassSourceFile;
import net.sf.jaspercode.langsupport.java.types.JavaVariableType;
import net.sf.jaspercode.langsupport.java.types.impl.JavaServiceType;
import net.sf.jaspercode.patterns.xml.java.domain.HandwrittenRule;

@Plugin
@Processor(componentClass = HandwrittenRule.class)
public class HandwrittenRuleProcessor implements ComponentProcessor {
	HandwrittenRule comp = null;
	ProcessorContext ctx = null;
	
	@Override
	public void init(Component component, ProcessorContext ctx) {
		this.comp = (HandwrittenRule)component;
		this.ctx = ctx;
	}
	
	@Override
	public void process() throws JasperException {
		ctx.setLanguageSupport("Java8");
		JavaClassSourceFile src = DomainRuleUtils.getServiceSourceFile(comp, ctx);
		JavaServiceType serviceType = DomainRuleUtils.getServiceType(comp, ctx);

		String returnTypeName = comp.getReturnType();
		String params = comp.getParams();
		String ruleName = comp.getRule();

		if (ruleName.trim().length()==0) {
			throw new JasperException("No rule specified for handwritten rule");
		}

		if (!src.getSrc().isAbstract()) {
			throw new JasperException("Handwritten rule requires the domain service to be an sbtract class - you must specify configuration 'java.domain.implClass'");
		}
		
		JavaVariableType returnType = null;
		if (returnTypeName.trim().length()>0) {
			returnType = JasperUtils.getType(JavaVariableType.class, returnTypeName, ctx);
		}
		
		ServiceOperation op = new ServiceOperation(ruleName);
		MethodSource<JavaClassSource> methodSrc = src.getSrc().addMethod();

		methodSrc.setPublic().setAbstract(true).setName(ruleName);
		if (returnType!=null) {
			src.addImport(returnType);
			methodSrc.setReturnType(returnType.getClassName());
			op.returnType(returnTypeName);
		}
		
		List<AttribEntry> ruleParams = JasperUtils.readParametersAsList(params, ctx);
		for(AttribEntry param : ruleParams) {
			JavaVariableType paramType = (JavaVariableType)param.getType();
			String paramName = param.getName();
			src.addImport(paramType);
			methodSrc.addParameter(paramType.getClassName(), paramName);
			op.addParam(paramName, paramType.getName());
		}
		serviceType.addOperation(op);
		ctx.originateVariableType(serviceType);
	}
	
}

