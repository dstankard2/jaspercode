package net.sf.jaspercode.patterns.java.http;

import java.util.List;

import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.MethodSource;

import net.sf.jaspercode.api.CodeExecutionContext;
import net.sf.jaspercode.api.ComponentProcessor;
import net.sf.jaspercode.api.JasperUtils;
import net.sf.jaspercode.api.ProcessorContext;
import net.sf.jaspercode.api.annotation.Plugin;
import net.sf.jaspercode.api.annotation.Processor;
import net.sf.jaspercode.api.config.Component;
import net.sf.jaspercode.api.exception.JasperException;
import net.sf.jaspercode.api.types.ServiceOperation;
import net.sf.jaspercode.langsupport.java.JavaCode;
import net.sf.jaspercode.langsupport.java.JavaClassSourceFile;
import net.sf.jaspercode.langsupport.java.JavaUtils;
import net.sf.jaspercode.langsupport.java.types.JavaVariableType;
import net.sf.jaspercode.langsupport.java.types.impl.JavaServiceType;
import net.sf.jaspercode.patterns.http.WebServiceContext;
import net.sf.jaspercode.patterns.http.WebServiceDefinition;
import net.sf.jaspercode.patterns.http.WebUtils;
import net.sf.jaspercode.patterns.web.JavaWebappRuntimePlatform;
import net.sf.jaspercode.patterns.xml.java.http.Preprocessing;
import net.sf.jaspercode.patterns.xml.java.http.WebServiceModule;

@Plugin
@Processor(componentClass = WebServiceModule.class)
public class WebServiceModuleProcessor implements ComponentProcessor {

	private ProcessorContext ctx = null;
	private WebServiceModule comp = null;
	
	@Override
	public void init(Component component, ProcessorContext ctx) {
		this.ctx = ctx;
		this.comp = (WebServiceModule)component;
	}

	@Override
	public void process() throws JasperException {
		ctx.setLanguageSupport("Java8");
		JavaClassSourceFile src = new JavaClassSourceFile(ctx);
		JavaClassSource cl = src.getSrc();
		String name = comp.getName();
		String servletName = name+"Servlet";
		String pkg = JavaUtils.getJavaPackage(comp, ctx);
		StringBuilder bodyCode = new StringBuilder();
		CodeExecutionContext execCtx = JavaUtils.getCodeExecutionContext(servletName, "service", ctx, true);

		cl.setPackage(pkg);
		cl.setName(servletName);
		cl.setSuperType("javax.servlet.http.HttpServlet");
		MethodSource<JavaClassSource> service = cl.addMethod().setName("service").setPublic();
		service.addThrows("java.io.IOException");
		service.addThrows("javax.servlet.ServletException");
		service.addParameter("javax.servlet.ServletRequest", "_request");
		service.addParameter("javax.servlet.ServletResponse", "_response");
		cl.addImport("javax.servlet.http.HttpServletRequest");
		cl.addImport("javax.servlet.http.HttpServletResponse");
		
		bodyCode.append("HttpServletRequest _httpRequest = (HttpServletRequest)_request;\n");
		bodyCode.append("HttpServletResponse _httpResponse = (HttpServletResponse)_response;\n");
		bodyCode.append("String _path = _httpRequest.getPathInfo();\n");
		bodyCode.append("String _template = null;\n");
		bodyCode.append("Map<String,String> _params = null;\n");
		bodyCode.append("String _method = _httpRequest.getMethod();\n");

		if ((comp.getRequestRef()!=null) && (comp.getRequestRef().trim().length()>0)) {
			String ref = comp.getRequestRef();
			ctx.addSystemAttribute(ref, "HttpServletRequest");
			bodyCode.append("HttpServletRequest "+ref+" = _httpRequest;\n");
			execCtx.addVariable(ref, "HttpServletRequest");
		}
		if ((comp.getResponseRef()!=null) && (comp.getResponseRef().trim().length()>0)) {
			String ref = comp.getResponseRef();
			ctx.addSystemAttribute(ref, "HttpServletResponse");
			bodyCode.append("HttpServletResponse "+ref+" = _httpResponse;\n");
			execCtx.addVariable(ref, "HttpServletResponse");
		}
		
		JavaWebappRuntimePlatform platform = JavaWebUtils.getWebPlatform(ctx);
		String uri = comp.getUri();
		if (uri.trim().length()==0) {
			throw new JasperException("Web Service module must have a uri");
		}
		platform.addServlet(servletName, pkg+'.'+servletName);
		platform.addServletMapping(uri+"/*", servletName);
		
		String filterGroup = comp.getFilterGroup();
		if (filterGroup.trim().length()>0) {
			JavaWebUtils.applyServletFilterChain(uri+"/*", filterGroup, ctx);
		}
		
		List<Preprocessing> procs = comp.getPreprocessing();
		JavaCode preprocessingCode = new JavaCode();
		for(Preprocessing proc : procs) {
			String ref = proc.getRef();
			if (ref.trim().length()==0) {
				continue;
			}
			if (execCtx.getVariableType(ref)!=null) {
				throw new JasperException("Variable ref '"+ref+"' in module '"+name+"' already exists in the current code execution context");
			}
			JavaVariableType refType = JasperUtils.getTypeForSystemAttribute(JavaVariableType.class, ref, ctx);
			JavaUtils.append(preprocessingCode, refType.declare(ref, execCtx));
			if (proc.getSource().trim().length()>0) {
				String v = proc.getSource();
				String evaluatedValue = JasperUtils.evaluateReference(v, execCtx);
				JavaUtils.append(preprocessingCode,JavaUtils.set(ref, evaluatedValue, execCtx));
				String type = ctx.getSystemAttribute(ref);
				execCtx.addVariable(ref, type);
			} else if (proc.getRule().trim().length()>0) {
				String v = proc.getRule();
				List<ServiceOperation> ops = JasperUtils.findRuleFromRef(v, ctx);
				if (ops.size()==0) {
					throw new JasperException("Cannot find preprocessing rule reference '"+v+"'");
				} else if (ops.size()>1) {
					throw new JasperException("Preprocessing found multiple rules called '"+v+"'");
				}
				ServiceOperation op = ops.get(0);
				if (op.getReturnType()==null) {
					throw new JasperException("Rule '"+v+"' does not return a value");
				}
				String serviceRef = v.substring(0, v.indexOf('.'));
				if (execCtx.getTypeForVariable(serviceRef)==null) {
					JavaServiceType serviceType = JasperUtils.getTypeForSystemAttribute(JavaServiceType.class, serviceRef, ctx);
					execCtx.addVariable(serviceRef, serviceType.getName());
					JavaUtils.append(preprocessingCode, serviceType.declare(serviceRef, execCtx));
					JavaUtils.append(preprocessingCode, serviceType.instantiate(serviceRef));
				}
				JavaUtils.append(preprocessingCode,JavaUtils.callJavaOperation(ref, serviceRef, op, execCtx, null));
				execCtx.addVariable(ref,op.getReturnType());
			}
		}
		
		bodyCode.append(preprocessingCode.getCodeText());
		for(String im : preprocessingCode.getImports()) {
			src.getSrc().addImport(im);
		}

		service.setBody(bodyCode.toString());
		ctx.addSourceFile(src);
		
		WebServiceContext webCtx = WebUtils.getWebServiceDefinition(ctx);
		WebServiceDefinition def = new WebServiceDefinition(webCtx);
		def.setUri(uri);
		def.setName(name);
		webCtx.getWebServices().add(def);

		src.getSrc().addImport("java.util.Map");
		src.getSrc().addImport("java.util.HashMap");
		MethodSource<JavaClassSource> retrieveParameters = src.getSrc().addMethod().setName("retrieveParameters").setPrivate().setBody(RETRIEVE_PARAMETER_CODE).setReturnType("Map<String,String>");
		retrieveParameters.addParameter("String", "template");
		retrieveParameters.addParameter("String", "path");
	}

	private static final String RETRIEVE_PARAMETER_CODE = 
			"		Map<String,String> ret = new HashMap<String,String>();\n"+
					"		int templateStart = 0,pathStart = 0; // Place in template and path where we are scanning from\n"+
					"		boolean done = false;\n"+
					"\n"+
					"		while(!done) {\n"+
					"			int paramIndex = template.indexOf(\"{\",templateStart);\n"+
					"			if (paramIndex==templateStart) {\n"+
					"				int paramEnd = template.indexOf('}',paramIndex);\n"+
					"				if (paramEnd < 0) throw new RuntimeException(\"Found invalid web service template string '\"+template+\"'\");\n"+
					"				String paramName = template.substring(paramIndex+1, paramEnd);\n"+
					"				String paramValue = null;\n"+
					"				if (paramEnd == template.length()-1) {\n"+
					"					// This is the end of the template... The parameter string is the rest of the path\n"+
					"					paramValue = path.substring(pathStart);\n"+
					"					done = true;\n"+
					"				} else {\n"+
					"					char end = template.charAt(paramEnd+1);\n"+
					"					int endI = path.indexOf(end, pathStart);\n"+
					"					if (endI<0) return null;\n"+
					"					paramValue = path.substring(pathStart, endI);\n"+
					"					templateStart = paramEnd+1;\n"+
					"					pathStart = endI;\n"+
					"				}\n"+
					"				ret.put(paramName, paramValue);\n"+
					"			} else {\n"+
					"				// Compare next segment of template to next segment of path\n"+
					"				String templateCompare = null;\n"+
					"				if (paramIndex<0) {\n"+
					"					// Comparing the end of the path to the end of the template\n"+
					"					templateCompare = template.substring(templateStart);\n"+
					"					done = true;\n"+
					"				} else {\n"+
					"					templateCompare = template.substring(templateStart,paramIndex);\n"+
					"				}\n"+
					"				if (path.indexOf(templateCompare, pathStart)==pathStart) {\n"+
					"					// match.  If paramIndex < 0, at end of string so return\n"+
					"					if (!done) {\n"+
					"						templateStart = paramIndex;\n"+
					"						pathStart += templateCompare.length();\n"+
					"					}\n"+
					"                   else { // Check ends of path and template\n"+
					"                       if (!template.substring(templateStart).equals(path.substring(pathStart))) {\n"+
					"                           return null;\n"+
					"                       }\n"+
					"                   }\n"+
					"				} else {\n"+
					"					// fail.\n"+
					"					return null;\n"+
					"				}\n"+
					"			}\n"+
					"		}\n"+
					"\n"+
					"		return ret;";

}
