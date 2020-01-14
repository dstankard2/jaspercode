package net.sf.jaspercode.patterns.js.template.parsing.directives;

import net.sf.jaspercode.api.annotation.Plugin;
import net.sf.jaspercode.api.exception.JasperException;
import net.sf.jaspercode.patterns.js.template.parsing.AttributeDirectiveBase;
import net.sf.jaspercode.patterns.js.template.parsing.DirectiveContext;

@Plugin
public class OnSubmitAttributeDirective extends AttributeDirectiveBase {

	@Override
	public String getAttributeName() {
		return "js-onsubmit";
	}

	@Override
	public void generateCode(DirectiveContext ctx) throws JasperException {
		StringBuilder b = ctx.getCode();
		String click = ctx.getTemplateAttributes().get("js-onsubmit");
		
		ctx.continueRenderElement(ctx.getExecCtx());
		String var = ctx.getElementVarName();

		b.append(var+".addEventListener('submit', function($event) {\n");
		b.append("$event.stopPropagation();\n");
		b.append(click);
		if (!click.endsWith(";")) {
			b.append(";");
		}
		b.append("\n});\n");
	}

}

