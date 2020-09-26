package net.sf.jaspercode.patterns.java.http;

import java.util.ArrayList;
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
import net.sf.jaspercode.langsupport.java.types.impl.JavaDataObjectType;
import net.sf.jaspercode.langsupport.java.types.impl.JavaEnumType;
import net.sf.jaspercode.langsupport.java.types.impl.JavaServiceType;
import net.sf.jaspercode.patterns.http.HttpMethod;
import net.sf.jaspercode.patterns.http.WebServiceContext;
import net.sf.jaspercode.patterns.http.WebServiceDefinition;
import net.sf.jaspercode.patterns.http.WebServiceOperation;
import net.sf.jaspercode.patterns.http.WebUtils;
import net.sf.jaspercode.patterns.xml.java.http.Endpoint;
import net.sf.jaspercode.patterns.xml.java.http.Response;

@Plugin
@Processor(componentClass = Endpoint.class)
public class EndpointProcessor implements ComponentProcessor {

	private static final String JACKSON_DEPENDENCY = "jackson-databind";
	
	private Endpoint comp = null;
	private ProcessorContext ctx = null;
	
	@Override
	public void init(Component component, ProcessorContext ctx) {
		this.comp = (Endpoint)component;
		this.ctx = ctx;
	}

	public void process() throws JasperException {
		ctx.setLanguageSupport("Java8");

		// Add dependencies for this pattern
		ctx.getBuildContext().addDependency(JACKSON_DEPENDENCY);

		HttpMethod method = comp.getMethod();
		if (method==null) {
			ctx.getLog().warn("No HTTP Method specified, defaulting to GET");
			method = HttpMethod.GET;
		}

		String path = comp.getPath();
		if (path.trim().length()==0) {
			throw new JasperException("Java HTTP Endpoint doesn't have a path specified");
		}

		WebServiceContext webCtx = WebUtils.getWebServiceDefinition(ctx);
		WebServiceDefinition srvDef = webCtx.getWebServiceDefinition(comp.getModule());
		
		if (srvDef==null) {
			throw new JasperException("Couldn't find web service module '"+comp.getModule()+"'");
		}

		String operationResult = comp.getOperationResult();
		String requestBody = null;
		WebServiceOperation op = new WebServiceOperation();
		srvDef.getOperations().add(op);

		HttpMethod httpMethod = comp.getMethod();
		if ((httpMethod==HttpMethod.POST) || (httpMethod==HttpMethod.PUT)) {
			if (comp.getRequestBody().trim().length()==0) {
				ctx.getLog().error("Java HTTP Endpoint is POST or PUT but does not have a request body specified - continuing");
			} else {
				requestBody = comp.getRequestBody();
				op.setRequestBodyFormat("json");
				op.setRequestBody(requestBody);
			}
			
		} else if (comp.getRequestBody().trim().length()>0) {
			ctx.getLog().error("Servlet Endpoint is not POST or PUT but specifies a request body - ignoring");
		}

		if (comp.getRequestParameters().trim().length()>0) {
			String params[] = comp.getRequestParameters().split(",");
			for(String s : params) {
				if (ctx.getSystemAttribute(s)==null) {
					throw new JasperException("Couldn't recognize parameter '"+s+"' as system attribute");
				}
				op.getRequestParameters().add(s);
			}
		}

		String moduleName = comp.getModule();
		if (moduleName.trim().length()==0) {
			throw new JasperException("Java HTTP Endpoint doesn't have a module specified.");
		}
		
		if (comp.getOperation().trim().length()==0) {
			throw new JasperException("Java HTTP Endpoint doesn't have a service operation specified");
		}

		JavaClassSourceFile src = null;
		String pkg = JavaUtils.getJavaPackage(comp, ctx);
		String className = moduleName + "Servlet";
		src = JavaUtils.getClassSourceFile(pkg+'.'+className, ctx, false);

		if (src==null) {
			throw new JasperException("Couldn't Find servlet web service '"+pkg+'.'+className+"' - You must declare a module component");
		}

		MethodSource<JavaClassSource> serviceMethod = src.getSrc().getMethod("service","javax.servlet.ServletRequest","javax.servlet.ServletResponse");
		if (serviceMethod==null) {
			throw new JasperException("Couldn't find service method in servlet web service '"+pkg+'.'+className+"'");
		}

		StringBuilder code = new StringBuilder(serviceMethod.getBody());
		CodeExecutionContext parentExecCtx = JavaUtils.getCodeExecutionContext(className, "service", ctx, false);
		
		if (parentExecCtx==null) {
			ctx.getLog().error("Couldn't find code execution context for web service module '"+className+"'");
		}
		
		CodeExecutionContext execCtx = new CodeExecutionContext(parentExecCtx);
		
		code.append("_params = retrieveParameters(\""+path+"\",_path);\n");
		String methodString = httpMethod.name();
		code.append("if ((_params != null) && (_method.equals(\""+methodString+"\"))) {\n");
		
		// Set op method and path
		op.setMethod(httpMethod);
		op.setPath(path);

		// Read path parameters
		List<String> pathParams = readPathParams(path);
		for(String param : pathParams) {
			String t = ctx.getSystemAttribute(param);
			if (t==null) {
				throw new JasperException("Path parameter '"+param+"' was not recognized as a system attribute");
			}
			if (execCtx.getTypeForVariable(param)!=null) {
				throw new JasperException("Path parameter '"+param+"' was already a variable in the web service's code execution context");
			}
			appendPathParamCode(src,param,code,execCtx);
		}
		
		// Handle parsing of request body
		if (requestBody!=null) {
			if ((httpMethod==HttpMethod.POST) || (httpMethod==HttpMethod.PUT)) {
				op.setRequestBodyFormat("json");
				JavaVariableType bodyType = JasperUtils.getTypeForSystemAttribute(JavaVariableType.class, requestBody, ctx);
				op.setRequestBody(requestBody);
				// For strings, just get the request body
				if (execCtx.getVariableType(requestBody)!=null) {
					throw new JasperException("Cannot handle request body reference '"+requestBody+"' - That variable already exists in the code execution context");
				}
				ctx.dependOnSystemAttribute(requestBody);
				ctx.dependOnVariableType(bodyType);
				if (bodyType==ctx.getVariableType("string")) {
					code.append("String "+requestBody+" = \"\";\n");
					execCtx.addVariable(requestBody, "string");
					src.getSrc().addImport("java.io.BufferedReader");
					code.append("BufferedReader _r = _httpRequest.getReader();\n");
					code.append("String _l = null;\n");
					code.append("while((_l = _r.readLine()) != null) {\n");
					code.append(requestBody+" += _l.trim();\n}\n");
				} else if(bodyType==ctx.getVariableType("integer")) {
					code.append("Integer "+requestBody+" = null;\n");
					execCtx.addVariable(requestBody, "integer");
					src.getSrc().addImport("java.io.BufferedReader");
					code.append("BufferedReader _r = _httpRequest.getReader();\n");
					code.append("String _s = \"\";\n");
					code.append("String _l = null;\n");
					code.append("while((_l = _r.readLine()) != null) {\n");
					code.append("_s += _l;\n}\n");
					code.append("_s = _s.trim();\n");
					code.append("try {"+requestBody+" = Integer.parseInt(_s);\n} catch(Exception e) { }\n");
				} else if(bodyType instanceof JavaEnumType) {
					String enumClassName = bodyType.getClassName();
					//JavaEnumType en = (JavaEnumType)bodyType;
					src.getSrc().addImport(bodyType.getImport());

					code.append(enumClassName+" "+requestBody+" = null;\n");
					execCtx.addVariable(requestBody, "integer");
					src.getSrc().addImport("java.io.BufferedReader");
					code.append("BufferedReader _r = _httpRequest.getReader();\n");
					code.append("String _s = \"\";\n");
					code.append("String _l = null;\n");
					code.append("while((_l = _r.readLine()) != null) {\n");
					code.append("_s += _l;\n}\n");
					code.append("if (_s.startsWith(\"\\\"\")) {_s = _s.substring(1,_s.indexOf(\"\\\"\",1));}");
					code.append("try {"+requestBody+" = "+enumClassName+".valueOf(_s.trim());} catch(IllegalArgumentException e) { }\n");
					//code.append("try {"+requestBody+" = "+en.getClassName()+".parseInt(_s);\n} catch(Exception e) { }\n");
				} else if (bodyType instanceof JavaDataObjectType) {
					// We have dependency on Jackson databind API
					JavaDataObjectType objType = (JavaDataObjectType)bodyType;
					src.getSrc().addImport("com.fasterxml.jackson.databind.ObjectMapper");
					src.addImport(objType);
					code.append("ObjectMapper _objectMapper = new ObjectMapper();\n");
					code.append(objType.declare(requestBody, execCtx).getCodeText());
					code.append(requestBody+" = _objectMapper.readValue(_httpRequest.getReader(),"+objType.getClassName()+".class);\n");
					execCtx.addVariable(requestBody, objType.getName());
				} else {
					throw new JasperException("Cannot specify request body with type '"+bodyType.getName()+"' - only data objects, integers, strings and enumerations are supported");
				}
			}
		}
		
		// Handle request parameters
		for(String param : op.getRequestParameters()) {
			if (execCtx.getVariableType(param)!=null) {
				throw new JasperException("Cannot process request parameter - A variable named '"+param+"' is already in the code execution context");
			}
			JavaVariableType type = JasperUtils.getTypeForSystemAttribute(JavaVariableType.class, param, ctx);
			execCtx.addVariable(param, type.getName());
			JavaCode c = type.declare(param, execCtx);
			code.append(c.getCodeText());
			String typeName = type.getName();
			if (typeName.equals("string")) {
				code.append(param+" = _httpRequest.getParameter(\""+param+"\");\n");
				code.append("if (("+param+"!=null) && ("+param+".trim().length()==0)) "+param+" = null;\n");
			}
			else if (typeName.equals("integer")) {
				String n = "_"+param+"String";
				code.append("String "+n+" = _httpRequest.getParameter(\""+param+"\");\n");
				code.append("if ("+n+"!=null) {\n");
				code.append("try {"+param+" = Integer.parseInt("+n+");} catch(Exception e) { }\n");
				code.append("}");
			}
			else if (typeName.equals("longint")) {
				String n = "_"+param+"String";
				code.append("String "+n+" = _httpRequest.getParameter(\""+param+"\");\n");
				code.append("if ("+n+"!=null) {\n");
				code.append("try {"+param+" = Long.parseLong("+n+");} catch(Exception e) { }\n");
				code.append("}");
			} else if (type instanceof JavaEnumType) {
				//JavaEnumType enumType = (JavaEnumType)type;
				src.addImport(type);
				ctx.getLog().info("Path parameter '"+param+"' is an enumeration - evaluating with Enum value");
				String enumClassName = type.getClassName();
				String n = "_"+param+"String";
				code.append("String "+n+" = _httpRequest.getParameter(\""+param+"\");\n");
				code.append("if ("+n+"!=null) {\n");
				// TODO: Log exception
				code.append("try {"+param+" = "+enumClassName+".valueOf("+n+");} catch(IllegalArgumentException e) { }\n");
				code.append("}");
			}
		}
		
		// Invoke service operation
		JavaCode invokeCode = new JavaCode();
		String ruleName = comp.getOperation();
		List<ServiceOperation> ops = JasperUtils.findRuleFromRef(ruleName, ctx);
		if (ops.size()==0) {
			throw new JasperException("Found no rule with ref '"+ruleName+"'");
		}
		if (ops.size()>1) {
			throw new JasperException("Web service endpoint expects to find one rule for ref '"+ruleName+"'");
		}
		String[] ruleParts = ruleName.split("\\.");
		ServiceOperation serviceOp = ops.get(0);
		String resultTypeName = serviceOp.getReturnType();
		JavaVariableType resultType = JasperUtils.getType(JavaVariableType.class, resultTypeName, ctx);
		boolean invalidResponseBodyType =  false;
		// Runtime code will not work if a data object type has no attributes
		if (resultType instanceof JavaDataObjectType) {
			JavaDataObjectType dobj = (JavaDataObjectType)resultType;
			if (dobj.getAttributeNames().size()==0) {
				invalidResponseBodyType = true;
			}
		}
		JavaUtils.append(invokeCode, resultType.declare(operationResult, execCtx));
		execCtx.addVariable(operationResult, resultTypeName);
		JavaServiceType serviceType = JasperUtils.getTypeForSystemAttribute(JavaServiceType.class, ruleParts[0], ctx);
		JavaUtils.append(invokeCode, serviceType.declare(ruleParts[0], execCtx));
		ctx.dependOnVariableType(serviceType);
		execCtx.addVariable(ruleParts[0], serviceType.getName());
		JavaUtils.append(invokeCode,serviceType.instantiate(ruleParts[0]));
		JavaUtils.append(invokeCode, JavaUtils.callJavaOperation(operationResult, ruleParts[0], serviceOp, execCtx, null));
		op.setOperationName(ruleParts[1]);
		code.append(invokeCode.getCodeText());
		src.addImports(invokeCode);

		// Handle responses
		List<Response> responses = comp.getResponse();
		if (responses.size()>0) {
			if (responses.size()>1) {
				throw new JasperException("Java HTTP Endpoint only supports one response at this time");
			}
			Response r = responses.get(0);
			if (r.getCondition().trim().length()>0) {
				throw new JasperException("Java HTTP Endpoint response does not support condition");
			}
			Integer status = r.getHttpStatus();
			if (status==null) status = 200;
			String responseBodyRef = r.getResponseBody();
			code.append("_httpResponse.setStatus("+status+");\n");
			if (responseBodyRef.trim().length()>0) {
				if (invalidResponseBodyType) {
					throw new JasperException("A web service result cannot return a response body when the service result has no attributes");
				}
				//ctx.getLog().warn("Found service result '"+resultTypeName+"' with no attributes - this cannot be the responsesending no response body from web service");

				op.setResponseBodyFormat("json");
				op.setResponseBody(resultTypeName);
				String bodyValue = JasperUtils.evaluateReference(responseBodyRef, execCtx);
				code.append("_httpResponse.setContentType(\"application/json\");\n");
				code.append("String _serialized = new ObjectMapper().writeValueAsString("+bodyValue+");\n");
				code.append("_httpResponse.setContentLength(_serialized.length());\n");
				src.getSrc().addImport("java.io.Writer");
				src.getSrc().addImport("com.fasterxml.jackson.databind.ObjectMapper");
				code.append("Writer _writer = _httpResponse.getWriter();\n");
				code.append("_writer.write(_serialized);\n");
				code.append("_writer.flush();\n");
			} else {
				ctx.getLog().warn("HTTP Endpoint result has no responseBody specified");
			}
			code.append("_httpResponse.flushBuffer();\n");
		}
		
		code.append("}\n");
		serviceMethod.setBody(code.toString());
	}
	
	private void appendPathParamCode(JavaClassSourceFile src,String param,StringBuilder code,CodeExecutionContext execCtx) throws JasperException {
		JavaVariableType type = JasperUtils.getTypeForSystemAttribute(JavaVariableType.class, param, ctx);
		String typeName = type.getName();
		JavaCode c = type.declare(param, execCtx);
		execCtx.addVariable(param, typeName);

		code.append(c.getCodeText());
		if (typeName.equals("string")) {
			code.append(param+" = _params.get(\""+param+"\");\n");
			code.append("if (("+param+"!=null) && ("+param+".trim().length()==0)) "+param+" = null;\n");
		}
		else if (typeName.equals("integer")) {
			String name = "_"+param+"String";
			code.append("String "+name+" = _params.get(\""+param+"\");");
			code.append("if ("+name+"!=null) {\n");
			code.append("try {"+param+" = Integer.parseInt("+name+");} catch(Exception e) { }\n");
			code.append("}");
		}
		else if (typeName.equals("longint")) {
			String name = "_"+param+"String";
			code.append("String "+name+" = _params.get(\""+param+"\");");
			code.append("if ("+name+"!=null) {\n");
			code.append("try {"+param+" = Long.parseLong("+name+");} catch(Exception e) { }\n");
			code.append("}");
		}
		else if (type instanceof JavaEnumType) {
			ctx.getLog().info("Path parameter '"+param+"' is an enumeration - evaluating with Enum value");
			String className = type.getClassName();
			String name = "_"+param+"String";
			code.append("String "+name+" = _params.get(\""+param+"\");");
			code.append("if ("+name+"!=null) {\n");
			// TODO: Log exception
			code.append("try {"+param+" = "+className+".valueOf("+name+");} catch(IllegalArgumentException e) { }\n");
			code.append("}");
		}
		else {
			// No other parameter types supported
			throw new JasperException("Found a web service with path parameter '"+param+"' - only integers, strings and enums are supported");
		}
	}

	protected List<String> readPathParams(String path) {
		List<String> ret = new ArrayList<>();
		int index = path.indexOf("{");
		int end = 0;
		String eval = null;

		while(index>=0) {
			end = path.indexOf('}', index+1);
			eval = path.substring(index+1, end);
			index = path.indexOf("{", end);
			ret.add(eval);
		}

		return ret;
	}

}

