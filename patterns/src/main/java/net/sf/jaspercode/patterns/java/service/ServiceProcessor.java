package net.sf.jaspercode.patterns.java.service;

import java.util.List;

import org.jboss.forge.roaster.model.source.JavaClassSource;

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
import net.sf.jaspercode.langsupport.java.JavaCode;
import net.sf.jaspercode.langsupport.java.JavaClassSourceFile;
import net.sf.jaspercode.langsupport.java.JavaUtils;
import net.sf.jaspercode.langsupport.java.types.impl.JavaDataObjectType;
import net.sf.jaspercode.langsupport.java.types.impl.JavaServiceType;
import net.sf.jaspercode.patterns.xml.java.service.NestingOperation;
import net.sf.jaspercode.patterns.xml.java.service.Operation;
import net.sf.jaspercode.patterns.xml.java.service.ResultOperation;
import net.sf.jaspercode.patterns.xml.java.service.Service;

@Plugin
@Processor(componentClass = Service.class)
public class ServiceProcessor implements ComponentProcessor {

	Service comp = null;
	ProcessorContext ctx = null;
	
	@Override
	public void init(Component component, ProcessorContext ctx) {
		this.comp = (Service)component;
		this.ctx = ctx;
	}

	@Override
	public void process() throws JasperException {
		ctx.setLanguageSupport("Java8");
		String pkg = null;
		String className = comp.getModule()+"Service";
		pkg = JavaUtils.getJavaPackage(comp, ctx);
		
		JavaClassSourceFile src = JavaUtils.getClassSourceFile(pkg+'.'+className, ctx);
		JavaServiceType type = null;
		
		if (ctx.getVariableType(className)!=null) {
			type = JasperUtils.getType(JavaServiceType.class, className, ctx);
		} else {
			type = new JavaServiceType(className,pkg+'.'+className,ctx.getBuildContext());
			ctx.addVariableType(type);
			String lowerCamel = JasperUtils.getLowerCamelName(type.getName());
			ctx.addSystemAttribute(lowerCamel, type.getName());
		}

		ctx.originateSourceFile(src);
		ctx.originateVariableType(className);
		
		//handleResult(pkg);
		handleServiceAndResult(type,src, pkg);
	}

	private void handleServiceAndResult(JavaServiceType type, JavaClassSourceFile src, String pkg) throws JasperException {
		ServiceOperation op = new ServiceOperation(comp.getName());
		String paramString = comp.getParams();
		String resultName = null;
		
		type.addOperation(op);
		resultName = comp.getName();
		resultName = Character.toUpperCase(comp.getName().charAt(0))+comp.getName().substring(1);
		resultName = resultName + "ServiceResult";
		//JavaDataObjectType resultType = JasperUtils.getType(JavaDataObjectType.class, resultName, ctx);
		List<AttribEntry> params = JasperUtils.readParametersAsList(paramString, ctx);
		CodeExecutionContext execCtx = new CodeExecutionContext(ctx);
		op.returnType(resultName);
		for(AttribEntry param : params) {
			String n = param.getName();
			String t = param.getType().getName();
			op.addParam(n, t);
			execCtx.addVariable(n, t);
		}

		JavaDataObjectType resultType = handleResult(pkg, execCtx);
		
		JavaCode code = resultType.declare("returnValue", execCtx);
		JavaUtils.append(code, resultType.instantiate("returnValue"));
		execCtx.addVariable("returnValue", resultName);
		
		JavaCode bodyCode = new JavaCode();
		processOperations(comp.getServiceOperation(),bodyCode, execCtx);
		JavaUtils.append(code, bodyCode);

		code.appendCodeText("return returnValue;\n");
		JavaUtils.addServiceOperation(op, code, src.getJavaClassSource(), ctx);
	}
	
	private void processOperations(List<Operation> ops,JavaCode currentCode,CodeExecutionContext execCtx) throws JasperException {
		for(Operation op : ops) {
			if (op instanceof NestingOperation) {
				NestingOperation n = (NestingOperation)op;
				NestingOperationRenderer renderer = (NestingOperationRenderer)op.getRenderer(ctx);
				JavaUtils.append(currentCode, renderer.getCode(execCtx));
				List<Operation> nestedOps = n.getOperation();
				processOperations(nestedOps,currentCode,execCtx);
				JavaUtils.append(currentCode, renderer.endingCode(execCtx));
			} else {
				OperationRenderer renderer = op.getRenderer(ctx);
				JavaUtils.append(currentCode, renderer.getCode(execCtx));
			}
		}
	}

	private JavaDataObjectType handleResult(String servicePkg, CodeExecutionContext execCtx) throws JasperException {
		JavaDataObjectType objType = null;
		JavaClassSourceFile src = new JavaClassSourceFile(ctx);
		String resultName = null;

		resultName = comp.getName();
		resultName = Character.toUpperCase(comp.getName().charAt(0))+comp.getName().substring(1);
		resultName = resultName + "ServiceResult";
		if (ctx.getVariableType(resultName)!=null) {
			throw new JasperException("Couldn't create service result class as a type named '"+resultName+"' already exists - please override the default");
		}
		ctx.getLog().info("Creating service result type '"+resultName+"'");
		objType = new JavaDataObjectType(resultName,servicePkg+'.'+resultName,ctx.getBuildContext());
		ctx.addVariableType(objType);

		JavaClassSource cl = src.getJavaClassSource();
		cl.setPackage(servicePkg);
		cl.setName(resultName);
		ctx.addSourceFile(src);

		List<Operation> ops = comp.getServiceOperation();
		readOperationsForResult(objType, src, ops, execCtx);

		return objType;
	}

	private void readOperationsForResult(JavaDataObjectType objType, JavaClassSourceFile src, 
			List<Operation> ops, CodeExecutionContext execCtx) throws JasperException {
		for(Operation op : ops) {
			if (op instanceof ResultOperation) {
				String resultType,resultName;
				ResultOperation res = (ResultOperation)op;
				resultType = res.getResultType(ctx, execCtx);
				if (resultType!=null) {
					resultName = res.getResultName(ctx, execCtx);
					if ((resultName!=null) && (resultName.startsWith("returnValue."))) {
						resultName = resultName.substring(12);
						if (objType.getAttributeType(resultName)==null) {
							if ((ctx.getSystemAttribute(resultName)!=null) && 
									(!ctx.getSystemAttribute(resultName).equals(resultType))) {
								throw new JasperException("Found inconsistent types '"+resultType+"' and '"+ctx.getSystemAttribute(resultName)+"' for attribute '"+resultName+"'");
							}
							ctx.addSystemAttribute(resultName, resultType);
							objType.addProperty(resultName, resultType);
							JavaUtils.addProperty(src, resultName, resultType, ctx);
						}
					}
				}
			}
			if (op instanceof NestingOperation) {
				NestingOperation nes = (NestingOperation)op;
				readOperationsForResult(objType,src,nes.getOperation(), execCtx);
			}
		}

	}

}
