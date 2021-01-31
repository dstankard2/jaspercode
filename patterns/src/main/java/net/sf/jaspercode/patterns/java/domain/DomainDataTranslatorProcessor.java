package net.sf.jaspercode.patterns.java.domain;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.MethodSource;

import net.sf.jaspercode.api.AttribEntry;
import net.sf.jaspercode.api.CodeExecutionContext;
import net.sf.jaspercode.api.ComponentProcessor;
import net.sf.jaspercode.api.JasperUtils;
import net.sf.jaspercode.api.ProcessorContext;
import net.sf.jaspercode.api.annotation.Plugin;
import net.sf.jaspercode.api.annotation.Processor;
import net.sf.jaspercode.api.config.Component;
import net.sf.jaspercode.api.exception.JasperException;
import net.sf.jaspercode.api.types.ServiceOperation;
import net.sf.jaspercode.langsupport.java.JavaClassSourceFile;
import net.sf.jaspercode.langsupport.java.JavaCode;
import net.sf.jaspercode.langsupport.java.JavaUtils;
import net.sf.jaspercode.langsupport.java.types.JavaVariableType;
import net.sf.jaspercode.langsupport.java.types.impl.JavaDataObjectType;
import net.sf.jaspercode.langsupport.java.types.impl.JavaServiceType;
import net.sf.jaspercode.patterns.xml.java.domain.DomainDataTranslator;

@Plugin
@Processor(componentClass = DomainDataTranslator.class)
public class DomainDataTranslatorProcessor implements ComponentProcessor {
	DomainDataTranslator comp = null;
	ProcessorContext ctx = null;

	@Override
	public void init(Component component, ProcessorContext ctx) {
		this.comp = (DomainDataTranslator)component;
		this.ctx = ctx;
	}

	@Override
	public void process() throws JasperException {
		ctx.setLanguageSupport("Java8");
		JavaClassSourceFile src = DomainRuleUtils.getServiceSourceFile(comp, ctx);
		JavaServiceType serviceType = DomainRuleUtils.getServiceType(comp, ctx);
		CodeExecutionContext execCtx = new CodeExecutionContext(ctx);

		String attrib = comp.getReturnAttribute();
		String params = comp.getParams();
		String ruleName = "get"+JasperUtils.getUpperCamelName(attrib);
		DomainDataRuleSet currentRules = DomainRuleUtils.getDomainDataRules(ctx);
		if (ruleName.equals(attrib)) {
			throw new JasperException("Couldn't determine rule name from attribute name '"+attrib+"'");
		}
		String attribTypeName = ctx.getSystemAttribute(attrib);
		if (attribTypeName==null) {
			throw new JasperException("Couldn't determine type for system attribute '"+attrib+"'");
		}
		JavaDataObjectType attribType = JasperUtils.getType(JavaDataObjectType.class, attribTypeName, ctx);

		MethodSource<JavaClassSource> methodSrc = src.getSrc().addMethod();
		ServiceOperation op = new ServiceOperation(ruleName);
		op.returnType(attribTypeName);
		methodSrc.setName(ruleName).setPublic();
		methodSrc.setReturnType(attribType.getImport());
		JavaCode code = new JavaCode();
		
		List<AttribEntry> ruleParams = JasperUtils.readParametersAsList(params, ctx);
		if (ruleParams.size()==0) {
			ctx.getLog().warn("Found no parameters for rule - ensure that the 'params' XML attribute is specified");
		}
		for(AttribEntry entry : ruleParams) {
			String key = entry.getName();
			JavaVariableType type = (JavaVariableType)entry.getType();
			op.addParam(key, type.getName());
			src.addImport(type);
			methodSrc.addParameter(type.getClassName(), key);
			execCtx.addVariable(key, type.getName());
		}
		code.append(attribType.declare(attrib, execCtx));
		code.append(attribType.instantiate(attrib));
		
		Map<String,JavaServiceType> serviceRefs = DomainRuleUtils.getDependencyRefs(comp, ctx);
		
		for(String a : attribType.getAttributeNames()) {
			// Attempt to find attribute "a" in code execution context
			String getCode = findAttribute(attrib, attribType, a, execCtx, serviceRefs, currentRules);
			if (getCode==null) {
				throw new JasperException("Couldn't find attribute '"+a+"' to build data object");
			}
			attribType.getCodeToSetAttribute(attrib, a, getCode, execCtx);
			code.appendCodeText(getCode);
		}

		code.appendCodeText("return "+attrib+";");
		methodSrc.setBody(code.getCodeText());
		src.addImports(code);
		serviceType.addOperation(op);
	}
	
	protected String findAttribute(String dataObjectRef, JavaDataObjectType dataObjectType, String attrib, 
			CodeExecutionContext execCtx, Map<String,JavaServiceType> serviceRefs,
			DomainDataRuleSet currentRules) throws JasperException {
		String ret = null;
		
		// Check code execution context
		if (execCtx.getVariableType(attrib)!=null) {
			ret = dataObjectType.getCodeToSetAttribute(dataObjectRef, attrib, attrib, execCtx);
			ret += ';';
			return ret;
		}
		// Check data objects in the code execution context for the variable.
		ret = checkDataObjects(dataObjectRef, dataObjectType, attrib, execCtx, serviceRefs);
		if (ret==null) {
			ret = checkCurrentRules(dataObjectRef, dataObjectType, attrib, execCtx, currentRules);
			if (ret==null) {
				ret = checkDependencies(dataObjectRef, dataObjectType, attrib, execCtx, serviceRefs);
			}
		}

		return ret;
	}

	public String checkCurrentRules(String dataObjectRef,JavaDataObjectType dataObjectType,String attrib, CodeExecutionContext execCtx, DomainDataRuleSet currentRules) throws JasperException {
		String ret = null;
		List<DomainDataRule> rules = currentRules.findRulesForAttribute(attrib);

		for(DomainDataRule rule : rules) {
			if (!rule.getAttribute().equals(attrib)) continue;
			for(ServiceOperation op : rule.getOperations()) {
				try {
					JavaCode code = JavaUtils.callJavaOperation(null, rule.getServiceRef(), op, execCtx, null);
					return dataObjectType.getCodeToSetAttribute(dataObjectRef, attrib, code.getCodeText(), execCtx);
					//return code.getCodeText();
				} catch(JasperException e) {
					// Couldn't invoke the rule in the current execCtx
				}
			}
		}

		return ret;
	}

	private String checkDependencies(String dataObjectRef,JavaDataObjectType dataObjectType,String attrib, CodeExecutionContext execCtx, Map<String,JavaServiceType> serviceRefs) throws JasperException {
		String getName = "get"+JasperUtils.getUpperCamelName(attrib);

		for(Entry<String,JavaServiceType> entry : serviceRefs.entrySet()) {
			JavaServiceType serviceType = entry.getValue();
			String ref = entry.getKey();
			List<ServiceOperation> ops = serviceType.getOperations(getName);
			for(ServiceOperation op : ops) {
				try {
					JavaCode code = JavaUtils.callJavaOperation(null, ref, op, execCtx, null, false);
					return dataObjectType.getCodeToSetAttribute(dataObjectRef, attrib, code.getCodeText(), execCtx)+';';
					//return code.getCodeText();
				} catch(JasperException e) {
					// Couldn't invoke the rule in the current execCtx
				}
			}
		}

		return null;
	}

	private String checkDataObjects(String dataObjectRef,JavaDataObjectType dataObjectType,String attrib, CodeExecutionContext execCtx, Map<String,JavaServiceType> serviceRefs) throws JasperException {
		String ret = null;
		for(String var : execCtx.getVariableNames()) {
			JavaVariableType t = execCtx.getType(JavaVariableType.class, execCtx.getVariableType(var));
			if (t instanceof JavaDataObjectType) {
				JavaDataObjectType dobj = (JavaDataObjectType)t;
				if (dobj.getAttributeType(attrib)!=null) {
					StringBuilder build = new StringBuilder();
					build.append("if ("+var+"!=null) {\n");
					String value = dobj.getCodeToRetrieveAttribute(var, attrib, null, execCtx);
					build.append(dataObjectType.getCodeToSetAttribute(dataObjectRef, attrib, value, execCtx));
					build.append(";\n}\n");
					return build.toString();
				}
			}
		}
		
		return ret;
	}

}

