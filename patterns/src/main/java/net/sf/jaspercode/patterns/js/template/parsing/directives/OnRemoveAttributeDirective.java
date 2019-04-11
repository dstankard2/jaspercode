package net.sf.jaspercode.patterns.js.template.parsing.directives;

import net.sf.jaspercode.api.CodeExecutionContext;
import net.sf.jaspercode.api.annotation.Plugin;
import net.sf.jaspercode.api.exception.JasperException;
import net.sf.jaspercode.patterns.js.parsing.JavascriptParser;
import net.sf.jaspercode.patterns.js.parsing.JavascriptParsingResult;
import net.sf.jaspercode.patterns.js.template.parsing.AttributeDirectiveBase;
import net.sf.jaspercode.patterns.js.template.parsing.DirectiveContext;
import net.sf.jaspercode.patterns.js.template.parsing.DirectiveUtils;

@Plugin
public class OnRemoveAttributeDirective extends AttributeDirectiveBase {

	@Override
	public String getAttributeName() {
		return "js-onremove";
	}

	@Override
	public void generateCode(DirectiveContext ctx) throws JasperException {
		String code = ctx.getTemplateAttributes().get("js-onremove");

		ctx.continueRenderElement();
		StringBuilder fn = ctx.getCode();
		CodeExecutionContext execCtx = ctx.getExecCtx();
		CodeExecutionContext newCtx = new CodeExecutionContext(execCtx);
		JavascriptParser eval = new JavascriptParser(code, newCtx);
		DirectiveUtils.populateImpliedVariables(eval);
		JavascriptParsingResult result = eval.evalCodeBlock();
		fn.append(ctx.getElementVarName()+".$$remove.push(function() {\n");
		fn.append(result.getCode());
		fn.append("\n});\n");
	}

}
