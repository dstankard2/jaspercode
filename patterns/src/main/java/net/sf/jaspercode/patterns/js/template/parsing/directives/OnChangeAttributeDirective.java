package net.sf.jaspercode.patterns.js.template.parsing.directives;

import net.sf.jaspercode.api.CodeExecutionContext;
import net.sf.jaspercode.api.annotation.Plugin;
import net.sf.jaspercode.api.exception.JasperException;
import net.sf.jaspercode.patterns.js.template.parsing.AttributeDirectiveBase;
import net.sf.jaspercode.patterns.js.template.parsing.DirectiveContext;
import net.sf.jaspercode.patterns.js.template.parsing.DirectiveUtils;

@Plugin
public class OnChangeAttributeDirective extends AttributeDirectiveBase {

	@Override
	public String getAttributeName() {
		return "js-onchange";
	}

	@Override
	public void generateCode(DirectiveContext ctx) throws JasperException {
		String change = ctx.getTemplateAttributes().get("js-onchange");
		String eltName = ctx.getElementName();
		String var = ctx.getElementVarName();
		StringBuilder code = ctx.getCode();
		CodeExecutionContext execCtx = ctx.getExecCtx();
		
		// The ModelAttributeDirective will handle this attribute.
		if (ctx.getTemplateAttributes().get("js-model")!=null) {
			ctx.continueRenderElement(ctx.getExecCtx());
			return;
		}
		
		String domEvent = null;
		if (eltName.equals("select")) {
			domEvent = "change";
		} else if (eltName.equals("input")) {
			String type = ctx.getDomAttributes().get("type");
			if (type==null) type = "text";
			if (type.equals("text")) domEvent = "keyup";
			else domEvent = "change";
		} else if (eltName.equals("textarea")) {
			domEvent = "keyup";
		} else {
			throw new JasperException("OnChangeDirective cannot be used on element '"+eltName+"'");
		}
		
		String dispatcherRef = DirectiveUtils.EVENT_DISPATCHER_FN_VAR;
		if (execCtx.getVariableType(dispatcherRef)==null) {
			throw new JasperException("There is no event dispatcher available to trigger an event on");
		}

		ctx.continueRenderElement(ctx.getExecCtx());
		String changeEventStr = DirectiveUtils.parsePartialExpression(change, execCtx);
		StringBuilder fn = new StringBuilder();
		fn.append("function() {\n");
		fn.append(dispatcherRef+"("+changeEventStr+");\n}\n");
		code.append(var+".addEventListener('"+domEvent+"',"+fn.toString()+");\n");
	}
	
}

