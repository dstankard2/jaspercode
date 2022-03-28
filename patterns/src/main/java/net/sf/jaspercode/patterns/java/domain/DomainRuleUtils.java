package net.sf.jaspercode.patterns.java.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jboss.forge.roaster.model.source.JavaClassSource;

import net.sf.jaspercode.api.CodeExecutionContext;
import net.sf.jaspercode.api.JasperUtils;
import net.sf.jaspercode.api.ProcessorContext;
import net.sf.jaspercode.api.exception.JasperException;
import net.sf.jaspercode.api.types.ServiceOperation;
import net.sf.jaspercode.langsupport.java.JavaClassSourceFile;
import net.sf.jaspercode.langsupport.java.JavaCode;
import net.sf.jaspercode.langsupport.java.JavaUtils;
import net.sf.jaspercode.langsupport.java.types.JavaVariableType;
import net.sf.jaspercode.langsupport.java.types.ServiceLocator;
import net.sf.jaspercode.langsupport.java.types.impl.JavaServiceType;
import net.sf.jaspercode.langsupport.java.types.impl.LocatedServiceType;
import net.sf.jaspercode.langsupport.java.types.impl.ServiceLocatorImpl;
import net.sf.jaspercode.patterns.xml.java.domain.DomainLogicComponent;

public class DomainRuleUtils {

	private static final String DOMAIN_DATA_RULES_OBJ = "DomainDataRulesObj";
	
	private static final String DEFAULT_SERVICE_GROUP_NAME = "DomainServices";

	// For public use - returns all services available and references to them (including located services - references are the code to locate them)
	// This method does not mark any dependency types as modified by the current component
	public static Map<String,JavaServiceType> getDependencyRefs(DomainLogicComponent comp,ProcessorContext ctx) throws JasperException {
		Map<String,JavaServiceType> ret = new HashMap<>();
		
		String deps = comp.getDependencies();
		if ((deps==null) || (deps.trim().equals(""))) {
			ctx.getLog().warn("No dependencies found for Domain Rule component - Specify with configuration property 'java.domain.depedencyRefs'");
			return ret;
		}
		String[] refs = deps.split(",");
		for(String ref : refs) {
			JavaVariableType type = JasperUtils.getTypeForSystemAttribute(JavaVariableType.class, ref, ctx);
			if (type instanceof JavaServiceType) {
				ret.put(ref, (JavaServiceType)type);
			} else if (type instanceof ServiceLocator) {
				ServiceLocator loc = (ServiceLocator)type;
				for(String srv : loc.getAvailableServices()) {
					String r = loc.getService(ref, srv, null);
					JavaServiceType t = JasperUtils.getType(JavaServiceType.class, srv, ctx);
					ret.put(r, t);
				}
			}
		}
		
		return ret;
	}

	// Private helper method for determining dependencies of the domain service class
	private static Map<String,JavaVariableType> getDependencies(DomainLogicComponent comp,ProcessorContext ctx) throws JasperException {
		Map<String,JavaVariableType> ret = new HashMap<>();
		
		String deps = comp.getDependencies();
		if ((deps==null) || (deps.trim().equals(""))) {
			ctx.getLog().warn("No dependencies found for Domain Rule component - specify with configuration property 'java.domain.depedencyRefs'");
			return ret;
		}
		String[] refs = deps.split(",");
		for(String ref : refs) {
			JavaVariableType type = JasperUtils.getTypeForSystemAttribute(JavaVariableType.class, ref, ctx);
			ret.put(ref, type);
		}
		
		return ret;
	}
	
	private static void initializeDomainService(DomainLogicComponent comp,ProcessorContext ctx) throws JasperException {
		JavaClassSourceFile serviceFile = null;
		String pkg = comp.getFullPackage();
		String className = comp.getServiceName();
		String serviceGroup = comp.getServiceGroupName();
		String ref = JasperUtils.getLowerCamelName(className);
		LocatedServiceType serviceType = null;
		String implClass = comp.getImplClass();

		//if ((implClass==null) || (implClass.trim().length()==0)) {
		//	throw new JasperException("Couldn't find implementation class name for domain logic component");
		//}
		if (className.equals(ref)) {
			throw new JasperException("Couldn't determine ref for service '"+className+"'");
		}

		ctx.addSystemAttribute(ref, className);
		serviceFile = new JavaClassSourceFile(ctx);
		serviceFile.getSrc().setPackage(pkg);
		serviceFile.getSrc().setName(className);
		ctx.addSourceFile(serviceFile);
		
		ctx.getLog().debug("Initializing Java domain service "+className);
		
		if ((serviceGroup==null) || (serviceGroup.trim().isEmpty())) {
			ctx.getLog().warn("Found no service group name - defaulting to '"+DEFAULT_SERVICE_GROUP_NAME+"'");
			serviceGroup = DEFAULT_SERVICE_GROUP_NAME;
		}

		// Add located service for this type
		serviceType = new LocatedServiceType(pkg, className,ctx.getBuildContext(),pkg+'.'+serviceGroup+"Locator", serviceGroup+"Locator");
		ctx.addVariableType(serviceType);
		
		ServiceLocatorImpl serviceLocatorType = null;
		JavaClassSourceFile locatorSource = null;
		
		if (ctx.getVariableType(serviceGroup+"Locator")==null) {
			serviceLocatorType = new ServiceLocatorImpl(serviceGroup+"Locator",serviceGroup+"Locator",pkg,ctx.getBuildContext());
			ctx.addVariableType(serviceLocatorType);
			locatorSource = new JavaClassSourceFile(ctx);
			locatorSource.getSrc().setName(serviceGroup+"Locator");
			locatorSource.getSrc().setPackage(pkg);
			ctx.addSourceFile(locatorSource);
		} else {
			serviceLocatorType = JasperUtils.getType(ServiceLocatorImpl.class, serviceGroup+"Locator", ctx);
			locatorSource = JavaUtils.getClassSourceFile(pkg+'.'+serviceGroup+"Locator", ctx);
			ctx.modifyVariableType(serviceLocatorType);
		}

		// Add locator method for this service
		String instanceClass = className;
		if ((implClass!=null) && (implClass.trim().length()>0)) {
			ctx.getLog().info("Creating service '"+className+"' as abstract class and using '"+implClass+"' as implementation");
			instanceClass = implClass;
			serviceFile.getSrc().setAbstract(true);
		}
		CodeExecutionContext execCtx = null;
		execCtx = new CodeExecutionContext(ctx);
		JavaClassSource locatorClass = locatorSource.getSrc();
		serviceLocatorType.getAvailableServices().add(className);
		locatorClass.addField().setName(ref).setType(pkg+'.'+className).setStatic(true).setLiteralInitializer("_get"+className+"();\n").setPrivate().setFinal(true);
		locatorClass.addMethod().setName("get"+className).setReturnType(pkg+'.'+className).setBody("return "+ref+";").setPublic();
		Map<String,JavaVariableType> deps = DomainRuleUtils.getDependencies(comp, ctx);
		JavaCode getCode = new JavaCode();
		getCode.appendCodeText(className+" _ret = new "+instanceClass+"();\n");
		for(Entry<String,JavaVariableType> dep : deps.entrySet()) {
			String depName = dep.getKey();
			JavaVariableType depType = dep.getValue();
			getCode.append(JavaUtils.serviceInstance(depName, depType, execCtx, ctx));
			String setter = "set"+JasperUtils.getUpperCamelName(depName);
			getCode.appendCodeText("_ret."+setter+"("+depName+");\n");
			locatorSource.addImports(getCode);
			serviceType.addDependency(depName);
			serviceFile.getSrc().addField().setName(depName).setType(depType.getImport()).setProtected();
			serviceFile.getSrc().addMethod().setPublic().setName("set"+JasperUtils.getUpperCamelName(depName)).setBody("this."+depName+" = "+depName+";").addParameter(depType.getImport(), depName);//.addParameter(depName, depType.getImport());
		}
		getCode.appendCodeText("return _ret;\n");
		locatorClass.addMethod().setName("_get"+className).setStatic(true).setBody(getCode.getCodeText()).setReturnType(pkg+'.'+className).setPrivate();
	}

	public static JavaClassSourceFile getServiceSourceFile(DomainLogicComponent comp,ProcessorContext ctx) throws JasperException {
		JavaClassSourceFile ret = null;
		String pkg = comp.getFullPackage();
		String className = comp.getServiceName();

		ret = JavaUtils.getClassSourceFile(pkg+'.'+className, ctx, false);
		if (ret==null) {
			initializeDomainService(comp,ctx);
			ret = getServiceSourceFile(comp,ctx);
		}

		return ret;
	}
	
	public static JavaServiceType getServiceType(DomainLogicComponent comp,ProcessorContext ctx) throws JasperException {
		JavaServiceType ret = null;
		String className = comp.getServiceName();
		
		if (ctx.getVariableType(className)==null) {
			initializeDomainService(comp,ctx);
			ret = getServiceType(comp,ctx);
		} else {
			ret = JasperUtils.getType(JavaServiceType.class, className, ctx);
		}
		
		return ret;
	}
	
	public static DomainDataRuleSet getDomainDataRules(ProcessorContext ctx) {
		DomainDataRuleSet rules = null;
		
		rules = (DomainDataRuleSet)ctx.getObject(DOMAIN_DATA_RULES_OBJ);
		if (rules==null) {
			rules = new DomainDataRuleSet();
			ctx.setObject(DOMAIN_DATA_RULES_OBJ, rules);
		}
		return rules;
	}

	// Find rules that will return the attribute specified
	public static List<ServiceOperation> getRulesForAttribute(String attribute,DomainLogicComponent comp,CodeExecutionContext execCtx, ProcessorContext ctx) throws JasperException {
		List<ServiceOperation> ret = new ArrayList<>();
		Map<String,JavaServiceType> refs = DomainRuleUtils.getDependencyRefs(comp, ctx);
		DomainDataRuleSet rules = getDomainDataRules(ctx);

		String getOpName = "get"+JasperUtils.getUpperCamelName(attribute);
		String findOpName = "find"+JasperUtils.getUpperCamelName(attribute);
		for(Entry<String,JavaServiceType> entry : refs.entrySet()) {
			JavaServiceType type = entry.getValue();
			ret.addAll(type.getOperations(getOpName));
			ret.addAll(type.getOperations(findOpName));
		}
		List<DomainDataRule> attribRules = rules.findRulesForAttribute(attribute);
		for(DomainDataRule rule : attribRules) {
			if (rule.getAttribute().equals(attribute)) {
				ret.addAll(rule.getOperations());
			}
		}

		return ret;
	}

}

