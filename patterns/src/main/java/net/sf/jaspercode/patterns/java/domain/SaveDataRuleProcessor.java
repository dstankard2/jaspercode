package net.sf.jaspercode.patterns.java.domain;

import java.util.ArrayList;
import java.util.HashMap;
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
import net.sf.jaspercode.langsupport.java.types.impl.JavaDataObjectType;
import net.sf.jaspercode.langsupport.java.types.impl.JavaServiceType;
import net.sf.jaspercode.patterns.xml.java.domain.SaveDataRule;

@Plugin
@Processor(componentClass = SaveDataRule.class)
public class SaveDataRuleProcessor implements ComponentProcessor {
	SaveDataRule comp = null;
	ProcessorContext ctx = null;

	@Override
	public void init(Component component, ProcessorContext ctx) {
		this.comp = (SaveDataRule)component;
		this.ctx = ctx;
	}

	@Override
	public void process() throws JasperException {
		ctx.setLanguageSupport("Java8");
		JavaClassSourceFile src = DomainRuleUtils.getServiceSourceFile(comp, ctx);
		JavaServiceType serviceType = DomainRuleUtils.getServiceType(comp, ctx);

		String entityConfig = comp.getEntities();
		String params = comp.getParams();
		String ruleName = comp.getName();
		
		String[] entities = entityConfig.split(",");

		ctx.originateSourceFile(src);
		MethodSource<JavaClassSource> methodSrc = src.getJavaClassSource().addMethod();
		methodSrc.setPublic().setName(ruleName);

		ServiceOperation op = new ServiceOperation(ruleName);
		methodSrc.setName(ruleName);

		JavaCode code = new JavaCode();
		CodeExecutionContext execCtx = new CodeExecutionContext(ctx);

		List<AttribEntry> ruleParams = JasperUtils.readParametersAsList(params, ctx);
		if (ruleParams.size()==0) {
			ctx.getLog().warn("A save data rule must take parameters which are the data to be saved");
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

		Map<String,JavaServiceType> deps = DomainRuleUtils.getDependencyRefs(comp, ctx);
		DomainDataRuleSet rules = DomainRuleUtils.getDomainDataRules(ctx);
		Map<String,JavaDataObjectType> entityTypes = new HashMap<>();

		for(String entityName : entities) {
			String entityTypeName = ctx.getSystemAttribute(entityName);
			JavaDataObjectType entityType = JasperUtils.getType(JavaDataObjectType.class, entityTypeName, ctx);
			code.append(entityType.declare(entityName, execCtx));
			String upperAttrib = JasperUtils.getUpperCamelName(entityName);
			String getName = "get"+upperAttrib;
			String findName = "find"+upperAttrib;
			List<DomainDataRule> domainRules = rules.findRulesForAttribute(entityName);
			entityTypes.put(entityName, entityType);

			boolean entityFound = false;
			for(Entry<String,JavaServiceType> entry : deps.entrySet()) {
				List<ServiceOperation> ops = new ArrayList<>();
				JavaServiceType depServiceType = entry.getValue();
				String depServiceRef = entry.getKey();
				ops.addAll(depServiceType.getOperations(getName));
				ops.addAll(depServiceType.getOperations(findName));
				for(ServiceOperation refOp : ops) {
					try {
						code.append(JavaUtils.callJavaOperation(entityName, depServiceRef, refOp, execCtx, null));
						entityFound = true;
					} catch(JasperException e) {
						// Calling this operation failed
					}
				}
			}
			if (!entityFound) {
				for(DomainDataRule domainRule : domainRules) {
					for(ServiceOperation o : domainRule.getOperations()) {
						try {
							JavaCode newCode = JavaUtils.addServiceToExecutionContext(domainRule.getServiceRef(), execCtx, ctx);
							newCode.append(JavaUtils.callJavaOperation(domainRule.getServiceRef(), domainRule.getServiceRef(), o, execCtx, null));
							code.append(newCode);
							entityFound = true;
						} catch(JasperException e) {
							e.printStackTrace();
						}
					}
				}
			}
			if (!entityFound) {
				throw new JasperException("Found find rule for entiity '"+entityName+"'");
			}
		}
		
		String dataObject = comp.getDataObject();
		if (!op.getParamNames().contains(dataObject)) {
			throw new JasperException("Object to save '"+dataObject+"' is not a parameter to the domain rule");
		}
		String dataObjectTypeName = op.getParamType(dataObject);
		JavaDataObjectType dataObjectType = JasperUtils.getType(JavaDataObjectType.class, dataObjectTypeName, ctx);
		
		for(String attrib : dataObjectType.getAttributeNames()) {
			//boolean found = false;
			String get = dataObjectType.getCodeToRetrieveAttribute(dataObject, attrib, null, execCtx);
			for(Entry<String,JavaDataObjectType> entry : entityTypes.entrySet()) {
				JavaDataObjectType t = entry.getValue();
				String name = entry.getKey();
				if (t.getAttributeType(attrib)!=null) {
					String set = t.getCodeToSetAttribute(name, attrib, get, execCtx);
					code.appendCodeText(set+';');
				}
			}
		}
		
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

