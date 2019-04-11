package net.sf.jaspercode.patterns.js.template.parsing.directives;

import net.sf.jaspercode.api.CodeExecutionContext;
import net.sf.jaspercode.api.annotation.Plugin;
import net.sf.jaspercode.api.exception.JasperException;
import net.sf.jaspercode.patterns.js.template.parsing.DirectiveContext;
import net.sf.jaspercode.patterns.js.template.parsing.DirectiveUtils;
import net.sf.jaspercode.patterns.js.template.parsing.ElementDirective;

@Plugin
public class EventDispatcherElementDirective implements ElementDirective {

	@Override
	public String getElementName() {
		return "js-event-dispatcher";
	}

	@Override
	public void generateCode(DirectiveContext ctx) throws JasperException {
		String ref = ctx.getDomAttributes().get("ref");
		StringBuilder code = ctx.getCode();
		CodeExecutionContext execCtx = ctx.getExecCtx();
		String var = DirectiveUtils.EVENT_DISPATCHER_FN_VAR;

		if (ref!=null) {
			ref = ref.trim();
			if (execCtx.getVariableType(ref)!=null) {
				throw new JasperException("Couldn't add event dispatcher as there is already a variable in this template called '"+ref+"'");
			}
		}
		if (execCtx.getVariableType(var)!=null) {
			throw new JasperException("Couldn't add event dispatcher as there is already one in this template");
		}

		String dispatcherVarName = ctx.newVarName("_ev", "function", execCtx);

		code.append("var "+dispatcherVarName+" = new EventDispatcher();\n");
		code.append("var "+var+" = "+dispatcherVarName+".event;\n");
		if (ref!=null) {
			code.append("var "+ref+" = "+var+";\n");
			execCtx.addVariable(ref, "function");
		}
		execCtx.addVariable(var, "function");
	}

}

