package net.sf.jaspercode.patterns.js.template.parsing.directives;

import net.sf.jaspercode.api.JasperException;
import net.sf.jaspercode.api.annotation.Plugin;
import net.sf.jaspercode.patterns.js.parsing.JavascriptParser;
import net.sf.jaspercode.patterns.js.parsing.JavascriptParsingResult;
import net.sf.jaspercode.patterns.js.template.parsing.AttributeDirectiveBase;
import net.sf.jaspercode.patterns.js.template.parsing.DirectiveContext;
import net.sf.jaspercode.patterns.js.template.parsing.DirectiveUtils;

@Plugin
public class EnabledAttributeDirective extends AttributeDirectiveBase {

	@Override
	public String getAttributeName() {
		return "js-enabled";
	}

	@Override
	public void generateCode(DirectiveContext ctx) throws JasperException {
		StringBuilder code = ctx.getCode();
		String value = ctx.getTemplateAttributes().get("js-enabled");
		
		ctx.continueRenderElement(ctx.getExecCtx());
		
		JavascriptParser eval = new JavascriptParser(value,ctx.getExecCtx());
		DirectiveUtils.populateImpliedVariables(eval);
		JavascriptParsingResult res = eval.evalExpression();
		String cond = res.getCode();
		code.append("try {\n");
		code.append("if ("+cond+") "+ctx.getElementVarName()+".disabled = false;\n");
		code.append("else "+ctx.getElementVarName()+".disabled = true;\n");
		code.append("}catch(_err){");
		code.append(ctx.getElementVarName()+".disabled = true;");
		code.append("}\n");
	}

}

