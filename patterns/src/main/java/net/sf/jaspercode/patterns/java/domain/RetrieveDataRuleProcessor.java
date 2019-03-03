package net.sf.jaspercode.patterns.java.domain;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.MethodSource;

import net.sf.jaspercode.api.AttribEntry;
import net.sf.jaspercode.api.CodeExecutionContext;
import net.sf.jaspercode.api.ComponentProcessor;
import net.sf.jaspercode.api.JasperException;
import net.sf.jaspercode.api.JasperUtils;
import net.sf.jaspercode.api.ProcessorContext;
import net.sf.jaspercode.api.annotation.Plugin;
import net.sf.jaspercode.api.annotation.Processor;
import net.sf.jaspercode.api.config.Component;
import net.sf.jaspercode.api.types.ServiceOperation;
import net.sf.jaspercode.langsupport.java.JavaClassSourceFile;
import net.sf.jaspercode.langsupport.java.JavaCode;
import net.sf.jaspercode.langsupport.java.JavaUtils;
import net.sf.jaspercode.langsupport.java.types.JavaVariableType;
import net.sf.jaspercode.langsupport.java.types.impl.JavaServiceType;
import net.sf.jaspercode.patterns.xml.java.domain.RetrieveDataRule;

@Plugin
@Processor(componentClass = RetrieveDataRule.class)
public class RetrieveDataRuleProcessor implements ComponentProcessor {
	RetrieveDataRule comp = null;
	ProcessorContext ctx = null;
	
	@Override
	public void init(Component component, ProcessorContext ctx) {
		this.comp = (RetrieveDataRule)component;
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
		String ruleName = comp.getName();

		if (ruleName.equals(attrib)) {
			throw new JasperException("Couldn't determine rule name from attribute name '"+attrib+"'");
		}
		String attribTypeName = ctx.getSystemAttribute(attrib);
		if (attribTypeName==null) {
			throw new JasperException("Couldn't determine type for system attribute '"+attrib+"'");
		}
		JavaVariableType attribType = JasperUtils.getType(JavaVariableType.class, attribTypeName, ctx);

		ctx.originateSourceFile(src);
		MethodSource<JavaClassSource> methodSrc = src.getJavaClassSource().addMethod();
		ServiceOperation op = new ServiceOperation(ruleName);
		op.returnType(attribTypeName);
		ctx.originateSourceFile(src);
		methodSrc.setName(ruleName);
		methodSrc.setReturnType(attribType.getImport());
		methodSrc.setPublic();
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
			if (entry.isOriginator()) {
				ctx.originateSystemAttribute(key);
			}
		}
		code.append(attribType.declare(attrib, execCtx));

		Map<String,JavaServiceType> serviceRefs = DomainRuleUtils.getDependencyRefs(comp, ctx);
		DomainDataRuleSet currentRules = DomainRuleUtils.getDomainDataRules(ctx);
		// Add this to the service refs to use rules that have already been defined
		serviceRefs.put("this", serviceType);
		
		JavaCode bodyCode = internalFindAttribute(0,attrib,execCtx,serviceRefs,currentRules);
		if (bodyCode==null) {
			throw new JasperException("Unable to resolve rule - unable to find attribute");
		}

		code.append(bodyCode);
		code.appendCodeText("return "+attrib+";");
		methodSrc.setBody(code.getCodeText());
		JavaUtils.addImports(src, code);
		serviceType.addOperation(op);
	}

	protected JavaCode internalFindAttribute(int level,String attrib,CodeExecutionContext execCtx,Map<String,JavaServiceType> serviceRefs, DomainDataRuleSet currentRules) throws JasperException {
		JavaCode ret = null;

		level++;
		if (level>=4) {
			return null;
		}

		// Is the attribute in the current execCtx?
		if (execCtx.getTypeForVariable(attrib)!=null) {
			return new JavaCode();
		}
		ret = findInCurrentRules(level,attrib,execCtx,serviceRefs,currentRules);
		if (ret==null) {
			ret = findInDependencies(level,attrib,execCtx,serviceRefs,currentRules);
		}
		
		if (ret==null) {
			ctx.getLog().warn("Unable to locate attribute '"+attrib+"' in current code execution context");
		}
		return ret;
	}

	protected JavaCode findInDependencies(int level,String attrib,CodeExecutionContext execCtx,Map<String,JavaServiceType> serviceRefs, DomainDataRuleSet currentRules) throws JasperException {
		JavaCode ret = null;
		String getName = "get"+JasperUtils.getUpperCamelName(attrib);
		String findName = "find"+JasperUtils.getUpperCamelName(attrib);
		
		for(Entry<String,JavaServiceType> entry : serviceRefs.entrySet()) {
			String ref = entry.getKey();
			JavaServiceType serviceType = entry.getValue();
			
			List<ServiceOperation> getOperations = serviceType.getOperations(getName);
			for(ServiceOperation op : getOperations) {
				ret = invokeOperation(level, attrib, execCtx, serviceRefs, currentRules, ref, op);
				if (ret!=null) {
					break;
				}
			}
			if (ret==null) {
				List<ServiceOperation> findOperations = serviceType.getOperations(findName);
				for(ServiceOperation op : findOperations) {
					ret = invokeOperation(level, attrib, execCtx, serviceRefs, currentRules, ref, op);
					if (ret!=null) {
						break;
					}
				}
			}
			if (ret!=null) {
				break;
			}
		}

		return ret;
	}
	
	protected JavaCode findInCurrentRules(int level,String attrib,CodeExecutionContext execCtx,Map<String,JavaServiceType> serviceRefs, DomainDataRuleSet currentRules) throws JasperException {
		JavaCode ret = null;
		//Map<String,String> ruleParams = null;

		List<DomainDataRule> rules = currentRules.findRulesForAttribute(attrib);
		for(DomainDataRule rule : rules) {
			if (execCtx.getVariableType(rule.getServiceRef())!=null) {
				// For each rule, find the parameters as attributes.  If they are all found, attempt to invoke the rule.
				for(ServiceOperation op : rule.getOperations()) {
					ret = invokeOperation(level, attrib, execCtx, serviceRefs, currentRules, rule.getServiceRef(), op);
					if (ret!=null) {
						break;
					}
				}
				if (ret!=null) {
					break;
				}
			}
		}

		return ret;
	}

	private JavaCode invokeOperation(int level, String attrib, CodeExecutionContext execCtx,
			Map<String, JavaServiceType> serviceRefs, DomainDataRuleSet currentRules, String ref,
			ServiceOperation op) throws JasperException {
		JavaCode ret;
		ret = new JavaCode();
		for(String param : op.getParamNames()) {
			if (execCtx.getTypeForVariable(param)==null) {
				String paramTypeName = op.getParamType(param);
				JavaVariableType paramType = execCtx.getType(JavaVariableType.class, paramTypeName);
				ret.append(paramType.declare(param, execCtx));
				JavaCode append = internalFindAttribute(level, param, execCtx, serviceRefs, currentRules);
				if (append!=null) {
					ret.append(append);
					execCtx.addVariable(param, paramTypeName);
				} else {
					ret = null;
					break;
				}
			}
		}
		if (ret!=null) {
			JavaCode invoke = JavaUtils.callJavaOperation(attrib, ref, op, execCtx, null);
			if (invoke!=null) {
				ret.append(invoke);
			} else {
				ret = null;
			}
		}
		return ret;
	}

}

