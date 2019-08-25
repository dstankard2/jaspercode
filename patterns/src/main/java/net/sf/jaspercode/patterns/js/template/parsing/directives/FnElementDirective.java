package net.sf.jaspercode.patterns.js.template.parsing.directives;

import java.util.List;

import net.sf.jaspercode.api.AttribEntry;
import net.sf.jaspercode.api.CodeExecutionContext;
import net.sf.jaspercode.api.JasperUtils;
import net.sf.jaspercode.api.annotation.Plugin;
import net.sf.jaspercode.api.exception.JasperException;
import net.sf.jaspercode.patterns.js.parsing.JavascriptParser;
import net.sf.jaspercode.patterns.js.parsing.JavascriptParsingResult;
import net.sf.jaspercode.patterns.js.template.parsing.DirectiveContext;
import net.sf.jaspercode.patterns.js.template.parsing.DirectiveUtils;
import net.sf.jaspercode.patterns.js.template.parsing.ElementDirective;

@Plugin
public class FnElementDirective implements ElementDirective {

	@Override
	public String getElementName() {
		return "js-fn";
	}

	@Override
	public void generateCode(DirectiveContext ctx) throws JasperException {
		//String html = DirectiveUtils.unescapeXml(ctx.getInnerHtml()).trim();
		String html = ctx.getInnerHtml().trim();
		String name = ctx.getDomAttributes().get("name");
		String params = ctx.getDomAttributes().get("params");
		String event = ctx.getDomAttributes().get("event");
		StringBuilder code = ctx.getCode();
		CodeExecutionContext execCtx = ctx.getExecCtx();
		CodeExecutionContext newCtx = new CodeExecutionContext(execCtx);
		boolean executeNow = ((name==null) && (event==null));
		//boolean isFunction = ((event!=null) || (name!=null));

		if (params==null) params = "";
		if ((name!=null) && (newCtx.getTypeForVariable(name)!=null)) {
			throw new JasperException("Cannot define a function named '"+name+"' as a variable of that name already exists in the execution context");
		}

		//boolean execute = false;
		if (name==null) {
			name = ctx.newVarName("_f", "function", execCtx);
			//if (event==null) execute = true;
		}
		else execCtx.addVariable(name, "function");

		if (!executeNow) {
			code.append("function "+name+"(");
			if (params.trim().length()>0) {
				boolean first = true;
				List<AttribEntry> atts = JasperUtils.readParametersAsList(params,ctx.getProcessorContext());
				for(AttribEntry att : atts) {
					if (first) first = false;
					else code.append(',');
					code.append(att.getName());
					if (att.getType()==null) throw new JasperException("Couldn't find type for js-fn param named '"+att.getName()+"'");
					newCtx.addVariable(att.getName(), att.getType().getName());
				}
			}
			code.append(") {\n");
		} else {
			code.append("(function() {\n");
		}
		code.append("try{\n");
		html = DirectiveUtils.unescapeXml(html);
		JavascriptParser eval = new JavascriptParser(html,newCtx);
		DirectiveUtils.populateImpliedVariables(eval);
		JavascriptParsingResult result = eval.evalCodeBlock();

		code.append(result.getCode().trim());
		code.append("\n}catch(_e){console.error(_e);}\n");
		if (!executeNow) {
			code.append("}\n");
		} else {
			code.append("})();\n");
		}
		
		if (event!=null) {
			String container = ctx.getContainerVarName();
			if (ctx.getContainerVarName()==null) {
				throw new JasperException("You may not assign an event to a js-fn unless it is inside a HTML Template DOM Element");
			}
			
			if (execCtx.getVariableType(DirectiveUtils.EVENT_DISPATCHER_FN_VAR)==null) {
				throw new JasperException("Couldn't attach a js-fn to event '"+event+"' because there is no event dispatcher present");
			}
			
			String events[] = event.split(",");
			String dispatcher = DirectiveUtils.EVENT_DISPATCHER_FN_VAR;
			for(String e : events) {
				String ref = DirectiveUtils.parsePartialExpression(e, execCtx);
				String cb = DirectiveUtils.newVarName("_cb", "function", execCtx);
				code.append("var "+cb+" = "+dispatcher+"("+ref+","+name+");\n");
				code.append(container+".$$remove.push("+cb+");\n");
			}
		}
	}

}

