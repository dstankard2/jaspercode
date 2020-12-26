package net.sf.jaspercode.patterns.js;

import java.util.ArrayList;
import java.util.List;

import net.sf.jaspercode.api.ComponentProcessor;
import net.sf.jaspercode.api.JasperUtils;
import net.sf.jaspercode.api.ProcessorContext;
import net.sf.jaspercode.api.annotation.Plugin;
import net.sf.jaspercode.api.annotation.Processor;
import net.sf.jaspercode.api.config.Component;
import net.sf.jaspercode.api.exception.JasperException;
import net.sf.jaspercode.api.types.DataObjectType;
import net.sf.jaspercode.api.types.ServiceOperation;
import net.sf.jaspercode.api.types.VariableType;
import net.sf.jaspercode.langsupport.java.types.JavaVariableType;
import net.sf.jaspercode.langsupport.java.types.impl.JavaEnumType;
import net.sf.jaspercode.langsupport.javascript.JavascriptCode;
import net.sf.jaspercode.langsupport.javascript.JavascriptUtils;
import net.sf.jaspercode.langsupport.javascript.modules.HandwrittenModuleSource;
import net.sf.jaspercode.langsupport.javascript.modules.ModuleSourceFile;
import net.sf.jaspercode.langsupport.javascript.types.JavascriptDataObjectType;
import net.sf.jaspercode.langsupport.javascript.types.JavascriptEnumType;
import net.sf.jaspercode.langsupport.javascript.types.JavascriptServiceType;
import net.sf.jaspercode.langsupport.javascript.types.JavascriptType;
import net.sf.jaspercode.langsupport.javascript.types.PromiseType;
import net.sf.jaspercode.patterns.http.HttpMethod;
import net.sf.jaspercode.patterns.http.WebServiceContext;
import net.sf.jaspercode.patterns.http.WebServiceDefinition;
import net.sf.jaspercode.patterns.http.WebServiceOperation;
import net.sf.jaspercode.patterns.http.WebUtils;
import net.sf.jaspercode.patterns.xml.js.WebService;
import net.sf.jaspercode.patterns.xml.js.WsClients;

@Plugin
@Processor(componentClass = WsClients.class)
public class WsClientsProcessor implements ComponentProcessor {
	WsClients comp = null;
	ProcessorContext ctx = null;

	@Override
	public void init(Component component, ProcessorContext ctx) {
		this.comp = (WsClients) component;
		this.ctx = ctx;
	}

	@Override
	public void process() throws JasperException {
		ctx.setLanguageSupport("Javascript");
		ModuleSourceFile file = JavascriptUtils.getModuleSource(ctx);
		String ctxName = comp.getBuildContextName();
		String urlPrefix = comp.getUrlPrefix();
		WebServiceContext webCtx = WebUtils.getWebServiceDefinition(ctxName, ctx, false);
		
		List<WebService> services = comp.getWebService();

		if (webCtx == null) {
			throw new JasperException("Found no web services for build context '" + ctxName + "'");
		}

		for (WebService service : services) {
			String name = service.getName();
			String ref = service.getRef();
			String module = service.getModule();

			JavascriptServiceType type = new JavascriptServiceType(name);

			ctx.addSystemAttribute(ref, name);
			ctx.addVariableType(type);

			if (module.trim().equals("")) {
				throw new JasperException("Web Service client module did not specify a module to be a client for");
			}
			if (name.trim().equals("")) {
				throw new JasperException("Web Service client module did not specify a name");
			}
			if (ref.trim().isEmpty()) {
				throw new JasperException("Web Service client module did not specify a ref");
			}

			WebServiceDefinition def = webCtx.getWebServiceDefinition(module);
			if (def == null) {
				throw new JasperException("Couldn't find web service module '" + module + "'");
			}
			HandwrittenModuleSource src = new HandwrittenModuleSource(module);
			file.addModule(src);
			List<WebServiceOperation> ops = def.getOperations();
			for (WebServiceOperation op : ops) {
				appendOperation(op, src, type, urlPrefix, def.getUri());
			}
			src.setName(name);
			src.getCodeBuild().append("return {\n");
			boolean first = true;
			for (WebServiceOperation op : ops) {
				if (first)
					first = false;
				else
					src.getCodeBuild().append(",\n");
				String n = getOperationName(op);
				src.getCodeBuild().append(n + ": _" + n);
			}
			src.getCodeBuild().append("\n};\n");
		}

	}

	// TODO: Figure out something else here
	protected String getOperationName(WebServiceOperation op) {
		return op.getOperationName();
	}

	// The operation will be valid - there's no need to check
	protected void appendOperation(WebServiceOperation op, HandwrittenModuleSource src, JavascriptServiceType type,
			String urlPrefix, String rootUri) throws JasperException {
		StringBuilder fnDec = new StringBuilder();
		StringBuilder fnBody = new StringBuilder();
		String name = getOperationName(op);
		String path = rootUri + op.getPath();
		HttpMethod method = op.getMethod();
		List<String> pathParams = op.getPathVariables();
		boolean firstArg = true;
		String ajaxProvider = comp.getAjaxProvider();
		ServiceOperation serviceOp = new ServiceOperation(name);
		//boolean sendRequestBody = false;

		if (urlPrefix.trim().length() > 0) {
			path = urlPrefix + path;
		}

		processReturnType(serviceOp, op);
		// Ensure types for path variables, request parameters
		for(String p : op.getPathVariables()) {
			String typeName = ctx.getSystemAttribute(p);
			ctx.setLanguageSupport("Java8");
			List<JavascriptType> additions = ensureType(typeName);
			ctx.setLanguageSupport("Javascript");
			for(JavascriptType t : additions) {
				ctx.addVariableType(t);
			}
		}
		for(String p : op.getRequestParameters()) {
			String typeName = ctx.getSystemAttribute(p);
			ctx.setLanguageSupport("Java8");
			List<JavascriptType> additions = ensureType(typeName);
			ctx.setLanguageSupport("Javascript");
			for(JavascriptType t : additions) {
				ctx.addVariableType(t);
			}
		}
		
		type.addOperation(serviceOp);

		fnDec.append("var _" + name).append(" = ").append("(");
		for (String s : pathParams) {
			if (firstArg)
				firstArg = false;
			else
				fnDec.append(',');
			fnDec.append(s);
			String t = ctx.getSystemAttribute(s);
			serviceOp.addParam(s, t);
		}

		path = path.replaceAll("\\{", "'+");
		path = path.replaceAll("\\}", "+'");

		fnBody.append(
				"var _options = {\nmethod: '" + method.value() + "',\npath: '" + path + "',\nsendCookies: true\n};\n");

		if ((method == HttpMethod.POST) || (method == HttpMethod.PUT)) {
			if (op.getRequestBody() != null) {
				//sendRequestBody = true;
				VariableType bodyType = JasperUtils.getTypeForSystemAttribute(VariableType.class, op.getRequestBody(), ctx);
				if (bodyType.getName().equals("string")) {
					fnBody.append("_options.requestBody = _requestBody;\n");
				} else {
					fnBody.append("_options.requestBody = JSON.stringify(_requestBody);\n");
				}
				if (firstArg)
					firstArg = false;
				else
					fnDec.append(',');
				fnDec.append("_requestBody");
				String n = op.getRequestBody();
				String t = ctx.getSystemAttribute(n);
				serviceOp.addParam(n, t);
			}
		}

		for (String param : op.getRequestParameters()) {
			String t = ctx.getSystemAttribute(param);
			serviceOp.addParam(param, t);
			if (firstArg) {
				firstArg = false;
				fnBody.append("_options.path += '?';\n");
			} else {
				fnDec.append(',');
				fnBody.append("_options.path += '&';\n");
			}
			fnDec.append(param);
			fnBody.append("if (" + param + ") _options.path += '" + param + "='+" + param + "\n");
			fnBody.append("else _options.path += '" + param + "=';\n");
		}

		if ((comp.getPreprocessing()!=null) && (comp.getPreprocessing().trim().length() > 0)) {
			fnBody.append(comp.getPreprocessing()).append("(_options);\n");
		} else {
			ctx.getLog().info("No request preprocessing defined for web service");
		}

		// TODO: This should probably be fetch
		final String DefaultAjaxProvider = "XMLHttpRequest";
		// Ajax Provider
		if (ajaxProvider==null) {
			ajaxProvider = DefaultAjaxProvider;
		} else if (ajaxProvider.trim().length() == 0) {
			ajaxProvider = DefaultAjaxProvider;
		}
		fnBody.append("var _promise;\n");
		AjaxClientProvider provider = JavascriptPatternUtils.getAjaxClientProvider(ajaxProvider, ctx);
		if (provider == null) {
			throw new JasperException("Couldn't instantiate Ajax provider '" + ajaxProvider + "'");
		}
		JavascriptCode ajaxCode = provider.getAjaxCode("_options", "_promise");
		fnBody.append(ajaxCode.getCodeText());
		fnBody.append("return _promise;\n");

		fnDec.append(") =>");
		src.getCodeBuild().append(fnDec);
		src.getCodeBuild().append("{\n");

		// Append function body
		src.getCodeBuild().append(fnBody);

		src.getCodeBuild().append("}\n");
	}

	private void processReturnType(ServiceOperation op, WebServiceOperation webOp) throws JasperException {
		String body = webOp.getResponseBody();

		if ((body == null) || (body.trim().length() == 0)) {
			op.returnType("Promise");
		} else {
			ctx.setLanguageSupport("Java8");
			// DataObjectType responseBodyType = JasperUtils.getType(DataObjectType.class,
			// body, ctx);

			// Ensure that the types exist in Javascript for response body and its
			// properties
			List<JavascriptType> additions = ensureType(body);

			ctx.setLanguageSupport("Javascript");
			for (JavascriptType a : additions) {
				ctx.addVariableType(a);
			}
			JavascriptType javascriptType = JasperUtils.getType(JavascriptType.class, body, ctx);
			ctx.addVariableType(PromiseType.getPromise(javascriptType, body + "Promise"));
			op.returnType(body + "Promise");
		}
	}

	private boolean checkJavascriptTypeExists(String name) throws JasperException {
		boolean ret = false;
		ctx.setLanguageSupport("Javascript");

		if (name.startsWith("list/")) {
			String elementTypeName = name.substring(5);
			ret = (ctx.getVariableType(elementTypeName) != null);
		} else {
			ret = (ctx.getVariableType(name) != null);
		}
		ctx.setLanguageSupport("Java8");
		return ret;
	}

	private List<JavascriptType> ensureType(String typeName) throws JasperException {
		List<JavascriptType> ret = new ArrayList<>();
		// String name = type.getName();
		boolean create = false;
		JavaVariableType type = JasperUtils.getType(JavaVariableType.class, typeName, ctx);

		if (!checkJavascriptTypeExists(typeName)) {
			create = true;
		}
		if (type instanceof DataObjectType) {
			DataObjectType objType = (DataObjectType) type;

			if (create) {
				JavascriptDataObjectType newType = new JavascriptDataObjectType(typeName, false, ctx);
				for (String a : objType.getAttributeNames()) {
					String t = objType.getAttributeType(a);
					newType.addAttribute(a, t);
					// JavaVariableType attType = JasperUtils.getType(JavaVariableType.class, t,
					// ctx);
					ret.addAll(ensureType(t));
				}
				ret.add(newType);
			}

			for (String a : objType.getAttributeNames()) {
				String name = objType.getAttributeType(a);
				// JavaVariableType t = JasperUtils.getType(JavaVariableType.class, name, ctx);
				ensureType(name);
			}
		} else if ((typeName.startsWith("list/")) && (create)) {
			String eltTypeName = typeName.substring(5);
			// JavaVariableType eltType = JasperUtils.getType(JavaVariableType.class,
			// eltTypeName, ctx);
			ret.addAll(ensureType(eltTypeName));
		} else if ((type instanceof JavaEnumType) && (create)) {
			JavascriptEnumType enumType = new JavascriptEnumType(typeName, false, ctx);
			ret.add(enumType);
		} else if (create) {
			throw new JasperException("Was unable to create a Javascript type for type '" + typeName + "'");
		}
		return ret;
	}

}
