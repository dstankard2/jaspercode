package net.sf.jaspercode.patterns.js.template.parsing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.jaspercode.api.CodeExecutionContext;
import net.sf.jaspercode.api.ProcessorContext;
import net.sf.jaspercode.api.exception.JasperException;
import net.sf.jaspercode.langsupport.javascript.JavascriptCode;
import net.sf.jaspercode.langsupport.javascript.modules.ModuleFunction;
import net.sf.jaspercode.patterns.js.parsing.JavascriptParser;
import net.sf.jaspercode.patterns.js.parsing.JavascriptParsingResult;

public class DirectiveUtils {

	public static final String PAGE_VAR = "_page";

	public static final String EVENT_DISPATCHER_FN_VAR = "_dis";

	public static final String LOCAL_MODEL_VAR = "_model";
	
	public static final String DOCUMENT_REF = "_d";
	
	public static final String TEMPLATE_ROOT_ELEMENT_REF = "_r";

	@SuppressWarnings("unchecked")
	public static List<AttributeDirective> getAttributeDirectives(ProcessorContext ctx) throws JasperException {
		List<AttributeDirective> ret = new ArrayList<AttributeDirective>();
		String objName = "net.sf.javascribe.view.templating.AttributeDirectives";
		
		List<AttributeDirectiveBase> systemDirectives = (List<AttributeDirectiveBase>)ctx.getObject(objName);
		if (systemDirectives==null) {
			systemDirectives = new ArrayList<AttributeDirectiveBase>();
			ctx.setObject(objName, systemDirectives);
			Set<Class<AttributeDirective>> cls = ctx.getApplicationContext().getPlugins(AttributeDirective.class);
			for(Class<?> cl : cls) {
				AttributeDirectiveBase dir;
				try {
					dir = (AttributeDirectiveBase)cl.newInstance();
					systemDirectives.add(dir);
				} catch(Exception e) {
					throw new JasperException("Couldn't find attribute directives",e);
				}
			}
			Collections.sort(systemDirectives);
		}
		for(AttributeDirective d : systemDirectives) {
			ret.add(d);
		}
		return ret;
	}

	@SuppressWarnings("unchecked")
	public static Map<String,ElementDirective> getElementDirectives(ProcessorContext ctx) throws JasperException {
		Map<String,ElementDirective> ret = new HashMap<String,ElementDirective>();
		String objName = "net.sf.javascribe.view.templating.ElementDirectives";
		
		ret = (Map<String,ElementDirective>)ctx.getObject(objName);
		if (ret==null) {
			ret = new HashMap<String,ElementDirective>();
			ctx.setObject(objName, ret);
			Set<Class<ElementDirective>> cls = ctx.getApplicationContext().getPlugins(ElementDirective.class);
			for(Class<?> cl : cls) {
				ElementDirective dir;
				try {
					dir = (ElementDirective)cl.newInstance();
					ret.put(dir.getElementName(), dir);
				} catch(Exception e) {
					throw new JasperException("Couldn't load attribute directives",e);
				}
			}
		}
		
		return ret;
	}

	public static String parsePartialExpression(String s,CodeExecutionContext execCtx) throws JasperException {
		StringBuilder build = new StringBuilder();
		s = s.trim();
		int i = s.indexOf("{{");
		int previousEnd = 0;
		build.append('\'');

		while(i>=0) {
			String append = s.substring(previousEnd, i);
			int end = s.indexOf("}}", i+2);
			build.append(append.replace("'", "\\'"));
			String add = s.substring(i+2, end).trim();
			if (add.length()==0) {
				throw new JasperException("Found empty expression in {{ }} in template");
			}
			JavascriptParser eval = new JavascriptParser(add,execCtx);
			populateImpliedVariables(eval);
			JavascriptParsingResult result = eval.evalExpression();

			String ref = result.getCode();
			build.append("'+((function(){try{return "+ref+"!=undefined?"+ref+":'';}catch(_e){return '';}})())+'");
			previousEnd = end + 2;
			i = s.indexOf("{{", previousEnd);
		}
		if (previousEnd < s.length()) {
			build.append(s.substring(previousEnd).replace("'","\\'"));
		}
		build.append('\'');
		
		return build.toString();
	}

	public static String newVarName(String baseName,String type,CodeExecutionContext execCtx) {
		for(int i=0;i<100;i++) {
			if (execCtx.getVariableType(baseName+i)==null) {
				execCtx.addVariable(baseName+i, type);
				return baseName+i;
			}
		}
		return null;
	}

	public static void populateImpliedVariables(JavascriptParser eval) {
		eval.addImpliedVariable(DirectiveUtils.LOCAL_MODEL_VAR)
				.addImpliedVariable(DirectiveUtils.PAGE_VAR)
				.addImpliedVariable(DirectiveUtils.PAGE_VAR+".model");
	}
	
	protected static String replace(String str,String replace,String replacement) {
		int i = str.indexOf(replace);
		while(i>=0) {
			str = str.substring(0, i) + replacement + str.substring(i+replace.length());
			i = str.indexOf(replace);
		}
		return str;
	}

	public static String unescapeXml(String str) {
		str = replace(str,"&quot;","\"");
		str = replace(str,"&lt;","<");
		str = replace(str,"&gt;",">");
		str = replace(str,"&amp;","&");
		str = replace(str,"&apos;","'");
		return str;
	}

	public static String getPageName(DirectiveContext ctx) {
		CodeExecutionContext execCtx = ctx.getExecCtx();
		
		if (execCtx.getVariableType(PAGE_VAR)!=null) {
			return execCtx.getVariableType(PAGE_VAR);
		}
		
		return null;
	}

	public static String getEventForModelRef(String modelRef) {
		String ret = modelRef+"Changed";
		int i = modelRef.indexOf('.');
		if (i>0) {
			ret = modelRef.substring(0, i)+"Changed";
		}
		
		return ret;
	}

	public static ModuleFunction getInvokeRem() {
		ModuleFunction fn = new ModuleFunction();
		fn.setName("invokeRem");
		fn.addParam("elt", "DOMElement");
		fn.setCode(new JavascriptCode(INVOKE_REM_CODE));
		return fn;
	}
	
	public static ModuleFunction getRem() {
		ModuleFunction fn = new ModuleFunction();
		fn.setName("rem");
		fn.addParam("parent", "DOMElement");
		fn.addParam("toRemove", "DOMElement");
		fn.setCode(new JavascriptCode(REM_CODE));
		return fn;
	}
	
	public static ModuleFunction getIns() {
		ModuleFunction fn = new ModuleFunction();
		fn.setName("ins");
		fn.addParam("parent", "DOMElement");
		fn.addParam("elt", "DOMElement");
		fn.addParam("prev", "DOMElement");
		fn.setCode(new JavascriptCode(INS_CODE));
		return fn;
	}
	
	private static final String INVOKE_REM_CODE = "if (elt.$$remove && elt.$$remove.length) {\n"
			+ "for(var i=0;i<elt.$$remove.length;i++) {\n"
			+ "elt.$$remove[i]();\n"
			+ "}/* for */\n"
			+ "delete elt.$$remove\n"
			+ "} /* if */\n";
	
	private static final String REM_CODE = "if (!toRemove) {\n"
			+ "for(var i=0;i<parent.childNodes.length;i++) {\n"
			+ "var node = parent.childNodes[i];\n"
			+ "_rem(node);\n"
			+ "} // for\n"
			+ "_invokeRem(parent);\n"
			+ "} // If\n"
			+ "else {\n"
			+ "for(var _i=0;_i<parent.childNodes.length;_i++) {\n"
			+ "if (parent.childNodes[_i]._elt==toRemove) {\n"
			+ "var node = parent.childNodes[_i];\n"
			+ "_rem(node);\n"
			+ "parent.removeChild(node);_i++;\n"
			+ "} // If\n"
			+ "} // for\n"
			+ "} // Else\n";

	private static final String INS_CODE = "for(var i=0;i<parent.childNodes.length;i++) {\n"
			+ "var done = true;\n"
			+ "var n = parent.childNodes[i];\n"
			+ "for(var i2=0;i2<prev.length;i2++) {\n"
			+ "if (n._elt==prev[i2]) {\n"
			+ "done = false;\n"
			+ "break;\n"
			+ "} // if\n"
			+ "} // for\n"
			+ "if (done) {\n"
			+ "parent.insertBefore(elt,n);\n"
			+ "return;\n"
			+ "}\n"
			+ "}// for\n"
			+ "parent.appendChild(elt);\n";
	
	// _invokeRem code
	/*
if (!window._rem) {
	// Invoke remove functions on the element.  $$remove is an array of functions 
	window._invokeRem = function(elt) {
	if ((elt.$$remove) && (elt.$$remove.length)) {
	for(var i=0;i<elt.$$remove.length;i++) {
	elt.$$remove[i]();
	}
	delete elt.$$remove;
	}
	};
	// If only parent is passed, remove all its children recursively and call $remove on the parent
	// If toRemove is defined, remove toRemove and remove all its children
	window._rem = function(parent,toRemove){
	if (!toRemove) {
	for(var i=0;i<parent.childNodes.length;i++) {
	var node = parent.childNodes[i];
	window._rem(node);
	};
	window._invokeRem(parent);
	} else {
	for(var _i=0;_i<parent.childNodes.length;_i++) {
	if (parent.childNodes[_i]._elt==toRemove) {
	var node = parent.childNodes[_i];
	window._rem(node);
	parent.removeChild(node);_i--;
	}
	}
	}
	};
	// Insert an element in its due place under parent, depending on prev
	window._ins = function(parent,elt,prev) {
	for(var i=0;i<parent.childNodes.length;i++) {
	var done = true;
	var n = parent.childNodes[i];
	for(var i2=0;i2<prev.length;i2++) {
	if (n._elt==prev[i2]) {
	done = false;
	break;
	}
	}
	if (done) {
	parent.insertBefore(elt,n);
	return;
	}
	}
	parent.appendChild(elt);
	};
	}
	 */

	/*
	public static final String REM_CODE = 
	"if (!window._rem) {\n"+
	"// Invoke remove functions on the element.  $$remove is an array of functions\n"+
	"window._invokeRem = function(elt) {\n"+
	"if ((elt.$$remove) && (elt.$$remove.length)) {\n"+
	"for(var i=0;i<elt.$$remove.length;i++) {\n"+
	"elt.$$remove[i]();\n"+
	"}\n"+
	"delete elt.$$remove;\n"+
	"}\n"+
	"};\n"+
	"// If only parent is passed, remove all its children recursively and call $remove on the parent\n"+
	"// If parent is defined, remove toRemove and remove all its children\n"+
	"window._rem = function(parent,toRemove){\n"+
	"if (!toRemove) {\n"+
	"for(var i=0;i<parent.childNodes.length;i++) {\n"+
	"var node = parent.childNodes[i];\n"+
	"window._rem(node);\n"+
	"};\n"+
	"window._invokeRem(parent);\n"+
	"} else {\n"+
	"for(var _i=0;_i<parent.childNodes.length;_i++) {\n"+
	"if (parent.childNodes[_i]._elt==toRemove) {\n"+
	"var node = parent.childNodes[_i];\n"+
	"window._rem(node);\n"+
	"parent.removeChild(node);_i--;\n"+
	"}\n"+
	"}\n"+
	"}\n"+
	"};\n"+
	"// Insert an element in its due place under parent, depending on prev \n"+
	"window._ins = function(parent,elt,prev) {\n"+
	"for(var i=0;i<parent.childNodes.length;i++) {\n"+
	"var done = true;\n"+
	"var n = parent.childNodes[i];\n"+
	"for(var i2=0;i2<prev.length;i2++) {\n"+
	"if (n._elt==prev[i2]) {\n"+
	"done = false;\n"+
	"break;\n"+
	"}\n"+
	"}\n"+
	"if (done) {\n"+
	"parent.insertBefore(elt,n);\n"+
	"return;\n"+
	"}\n"+
	"}\n"+
	"parent.appendChild(elt);\n"+
	"};\n"+
	"}\n";
	*/

}

