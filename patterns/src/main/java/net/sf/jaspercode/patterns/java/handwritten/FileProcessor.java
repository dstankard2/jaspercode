package net.sf.jaspercode.patterns.java.handwritten;

import java.io.IOException;
import java.util.List;

import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.JavaType;
import org.jboss.forge.roaster.model.source.AnnotationSource;
import org.jboss.forge.roaster.model.source.FieldSource;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.MethodSource;
import org.jboss.forge.roaster.model.source.ParameterSource;

import net.sf.jaspercode.api.CodeExecutionContext;
import net.sf.jaspercode.api.JasperUtils;
import net.sf.jaspercode.api.ProcessorContext;
import net.sf.jaspercode.api.exception.JasperException;
import net.sf.jaspercode.api.resources.ApplicationFile;
import net.sf.jaspercode.api.types.ServiceOperation;
import net.sf.jaspercode.langsupport.java.JavaCode;
import net.sf.jaspercode.langsupport.java.JavaClassSourceFile;
import net.sf.jaspercode.langsupport.java.JavaUtils;
import net.sf.jaspercode.langsupport.java.types.JavaVariableType;
import net.sf.jaspercode.langsupport.java.types.impl.JavaDataObjectType;
import net.sf.jaspercode.langsupport.java.types.impl.LocatedServiceType;
import net.sf.jaspercode.patterns.PatternPriority;
import net.sf.jaspercode.patterns.java.http.JavaWebUtils;
import net.sf.jaspercode.patterns.web.JavaWebappRuntimePlatform;

public class FileProcessor {
	
	private ApplicationFile file = null;
	private ProcessorContext ctx = null;
	private JavaClassSource src = null;
	private String stringPriority = null;
	private int priority = 0;
	private AnnotationSource<JavaClassSource> classAnnotation = null;
	private ClassType classType = null;

	public int getPriority() {
		return priority;
	}
	
	public FileProcessor(ApplicationFile file,ProcessorContext ctx) throws IOException,JasperException {
		this.file = file;
		this.ctx = ctx;

		priority = 0;
		findPriority();
		if (classAnnotation!=null) {
			if (stringPriority!=null) {
				priority = Integer.parseInt(stringPriority);
			}
		} else {
			priority = -1;
		}
	}
	
	private void findPriority() throws IOException {
		AnnotationSource<JavaClassSource> an = null;

		JavaType<?> type = Roaster.parse(file.getInputStream());
		if (!(type instanceof JavaClassSource)) {
			return;
		}
		src = (JavaClassSource)type;
		an = src.getAnnotation("net.sf.jaspercode.patterns.java.handwritten.BusinessService");
		if (an!=null) {
			stringPriority = an.getLiteralValue("priority");
			classAnnotation = an;
			if (stringPriority==null) {
				priority = PatternPriority.BUSINESS_SERVICE;
			}
			classType = ClassType.BUSINESS_SERVICE;
			return;
		}
		an = src.getAnnotation("net.sf.jaspercode.patterns.java.handwritten.DataObject");
		if (an!=null) {
			stringPriority = an.getLiteralValue("priority");
			classAnnotation = an;
			if (stringPriority==null) {
				priority = PatternPriority.DATA_OBJECT;
			}
			classType = ClassType.DATA_OBJECT;
			return;
		}
		an = src.getAnnotation("net.sf.jaspercode.patterns.java.handwritten.WebServlet");
		if (an!=null) {
			stringPriority = ""+PatternPriority.SERVLET_ENDPOINT;
			classAnnotation = an;
			classType = ClassType.SERVLET;
			return;
		}
		an = src.getAnnotation("net.sf.jaspercode.patterns.java.handwritten.WebServletFilter");
		if (an!=null) {
			stringPriority = ""+PatternPriority.SERVLET_FILTER;
			classAnnotation = an;
			classType = ClassType.SERVLET_FILTER;
			return;
		}
		an = src.getAnnotation("net.sf.jaspercode.patterns.java.handwritten.Websocket");
		if (an!=null) {
			stringPriority = ""+PatternPriority.SERVLET_ENDPOINT;
			classAnnotation = an;
			classType = ClassType.WEBSOCKET;
			return;
		}
		an = src.getAnnotation("net.sf.jaspercode.patterns.java.handwritten.WebContextListener");
		if (an!=null) {
			stringPriority = ""+PatternPriority.SERVLET_ENDPOINT;
			classAnnotation = an;
			classType = ClassType.WEB_CONTEXT_LISTENER;
			return;
		}
	}
	
	public void process() throws JasperException {
		if (classType==ClassType.BUSINESS_SERVICE) {
			processBusinessService();
		} else if (classType==ClassType.DATA_OBJECT) {
			processDataObject();
		} else if (classType==ClassType.SERVLET_FILTER) {
			processServletFilter();
		} else if (classType==ClassType.SERVLET) {
			processServlet();
		} else if (classType==ClassType.WEBSOCKET) {
			processWebsocket();
		} else if (classType==ClassType.WEB_CONTEXT_LISTENER) {
			processWebContextListener();
		}
		System.out.println("Process handwritten class with annotation " + classAnnotation.getName());
	}

	private void processWebContextListener() throws JasperException {
		List<String> interfaces = src.getInterfaces();
		boolean found = false;
		for(String in : interfaces) {
			if (in.equals("javax.servlet.ServletContextListener")) {
				found = true;
				break;
			}
		}
		if (!found) throw new JasperException("A class with @WebContextListener must implement interface 'javax.servlet.ServletContextEvent'");
		
		JavaWebappRuntimePlatform platform = JavaWebUtils.getWebPlatform(ctx);
		platform.addServletContextListener(src.getCanonicalName());
	}
	
	private void processWebsocket() throws JasperException {
		String className = src.getName();
		String pkg = src.getPackage();
		AnnotationSource<JavaClassSource> an = src.getAnnotation("net.sf.jaspercode.patterns.java.handwritten.Websocket");
		String path = an.getStringValue("path");
		JavaWebappRuntimePlatform platform = JavaWebUtils.getWebPlatform(ctx);

		String superType = src.getSuperType();
		if ((superType==null) || (!"javax.websocket.Endpoint".equals(superType))) {
			ctx.getLog().warn("Couldn't easily determine that '"+(pkg+'.'+className)+"' extends javax.websocket.Endpoint");
			ctx.getLog().warn("A websocket endpoint must extend javax.websocket.Endpoint in order to be processed properly");
			//throw new JasperException("A websocket endpoint must extend javax.websocket.Endpoint");
		}
		platform.addWebsocketEndpoint(path, pkg+'.'+className);
	}
	
	private void processServlet() throws JasperException {
		String className = src.getName();
		String pkg = src.getPackage();
		AnnotationSource<JavaClassSource> an = src.getAnnotation("net.sf.jaspercode.patterns.java.handwritten.WebServlet");
		String name = an.getStringValue("name");
		String[] uris = an.getStringArrayValue("uri");
		String filterChain = an.getStringValue("filterChain");
		JavaWebappRuntimePlatform platform = JavaWebUtils.getWebPlatform(ctx);

		platform.addServlet(name, pkg+'.'+className);
		for(String uri : uris) {
			platform.addServletMapping(uri, name);
			if (filterChain!=null) {
				JavaWebUtils.applyServletFilterChain(uri, filterChain, ctx);
			}
		}
	}

	private void processServletFilter() throws JasperException {
		String className = src.getName();
		String pkg = src.getPackage();
		AnnotationSource<JavaClassSource> an = src.getAnnotation("net.sf.jaspercode.patterns.java.handwritten.WebServletFilter");
		String name = an.getStringValue("name");
		JavaWebappRuntimePlatform platform = JavaWebUtils.getWebPlatform(ctx);

		platform.addFilter(name, pkg+'.'+className);
	}
	
	private void processDataObject() throws JasperException {
		String className = src.getName();
		String pkg = src.getPackage();
		JavaDataObjectType type = null;
		
		type = new JavaDataObjectType(className,pkg+'.'+className,ctx.getBuildContext());
		ctx.addVariableType(type);
		for(FieldSource<JavaClassSource> field : src.getFields()) {
			if (field.getAnnotation("net.sf.jaspercode.patterns.java.handwritten.DataObjectAttribute")!=null) {
				String name = field.getName();
				String classname = field.getType().getQualifiedName();
				String typeName = JavaUtils.getTypeName(classname);
				type.addProperty(name, typeName);
				ctx.addSystemAttribute(name, typeName);
			}
		}
		String lowerCamel = JasperUtils.getLowerCamelName(className);
		ctx.addSystemAttribute(lowerCamel, className);
		ctx.addSystemAttribute(lowerCamel+"List", "list"+type);
		String plural = JasperUtils.getMultiple(lowerCamel);
		ctx.addSystemAttribute(plural, "list/"+className);
	}

	private void processBusinessService() throws JasperException {
		String className = src.getName();
		String pkg = src.getPackage();
		String ref = classAnnotation.getStringValue("ref");
		String group = classAnnotation.getStringValue("group");
		String locatorName = group+"Locator";
		String subPkg = ctx.getProperty("handwritten.servicelocator.package");

		if (ref.trim().length()==0) {
			ref = JasperUtils.getLowerCamelName(className);
		}
		ctx.addSystemAttribute(ref, className);
		
		String locatorMethodName = "get"+Character.toUpperCase(ref.charAt(0))+ref.substring(1);
		
		if (subPkg==null) {
			throw new JasperException("Handwritten business service requires configuration property 'handwritten.servicelocator.package' to determine package of service locator");
		}
		String locatorPackage = JavaUtils.getRootPackage(ctx)+'.'+subPkg;
		JavaClassSourceFile locatorSource = JavaUtils.getClassSourceFile(locatorPackage+'.'+locatorName, ctx);

		LocatedServiceType serviceType = new LocatedServiceType(pkg,className,ctx.getBuildContext(),locatorPackage+'.'+locatorName,locatorName);
		ctx.addVariableType(serviceType);
		locatorSource.getSrc().addField().setStatic(true).setName(ref).setType(pkg+'.'+className).setLiteralInitializer("_get"+className+"()").setPrivate();
		locatorSource.getSrc().addMethod().setName(locatorMethodName).setReturnType(className).setBody("return "+ref+";").setPublic();

		MethodSource<JavaClassSource> inst = locatorSource.getSrc().addMethod().setName("_get"+className).setStatic(true).setPrivate().setReturnType(pkg+'.'+className);
		JavaCode locatorCode = new JavaCode();
		CodeExecutionContext locatorExecCtx = new CodeExecutionContext(ctx);
		locatorCode.append(serviceType.declare("_ret", locatorExecCtx));
		locatorCode.appendCodeText("_ret = new "+className+"();\n");

		locatorSource.getSrc().addImport(pkg+'.'+className);
		List<MethodSource<JavaClassSource>> methods = src.getMethods();
		for(MethodSource<JavaClassSource> method : methods) {
			AnnotationSource<JavaClassSource> an = method.getAnnotation("net.sf.jaspercode.patterns.java.handwritten.Dependency");
			if (an!=null) {
				String depRef = an.getStringValue("ref");
				String methodName = method.getName();
				if (depRef==null) {
					if (methodName.startsWith("set")) {
						depRef = Character.toLowerCase(methodName.charAt(3))+methodName.substring(4);
					}
				}
				String depTypeName = ctx.getSystemAttribute(depRef);
				if (depTypeName==null) {
					throw new JasperException("Dependency ref '"+depRef+"' was not found in system attributes");
				}
				JavaVariableType depType = JasperUtils.getType(JavaVariableType.class, depTypeName, ctx);
				JavaUtils.append(locatorCode, JavaUtils.serviceInstance(depRef, depType, locatorExecCtx, ctx));
				locatorCode.appendCodeText("_ret."+methodName+"("+depRef+");\n");
				this.ctx.dependOnVariableType(depType);
				this.ctx.dependOnSystemAttribute(depRef);
			} else {
				an = method.getAnnotation("net.sf.jaspercode.patterns.java.handwritten.BusinessRule");
				if (an!=null) {
					ServiceOperation op = new ServiceOperation(method.getName());
					serviceType.addOperation(op);
					String returnClassName = method.getReturnType().getQualifiedName();
					String returnTypeName = JavaUtils.getTypeName(returnClassName);
					op.returnType(returnTypeName);
					List<ParameterSource<JavaClassSource>> params = method.getParameters();
					for(ParameterSource<JavaClassSource> param : params) {
						String paramTypeName = JavaUtils.getTypeName(param.getType().getQualifiedName());
						String paramName = param.getName();
						op.addParam(paramName, paramTypeName);
					}
				}
			}
		}
		locatorCode.appendCodeText("return _ret;\n");
		inst.setBody(locatorCode.getCodeText());
		for(String im : locatorCode.getImports()) {
			locatorSource.getSrc().addImport(im);
		}
	}

	/*
	private void processBusinessService(JavaClassSource src, String ref) {
		String className = src.getName();
		List<String> dependencies = null;

		List<FieldSource<JavaClassSource>> members = src.getFields();
		for(FieldSource<JavaClassSource> member : members) {
			AnnotationSource<JavaClassSource> an = member.getAnnotation("net.sf.jaspercode.patterns.java.handwritten.Dependency");
			if (an==null) continue;
		}
	}
	*/

}

