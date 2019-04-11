package net.sf.jaspercode.patterns.js.template.parsing.directives;

import net.sf.jaspercode.api.annotation.Plugin;
import net.sf.jaspercode.api.exception.JasperException;
import net.sf.jaspercode.patterns.js.template.parsing.DirectiveContext;
import net.sf.jaspercode.patterns.js.template.parsing.ElementDirective;

@Plugin
public class ImportElementDirective implements ElementDirective {

	@Override
	public String getElementName() {
		return "js-import";
	}

	@Override
	public void generateCode(DirectiveContext ctx) throws JasperException {
		String ref = ctx.getDomAttributes().get("ref");
		StringBuilder code = ctx.getCode();
		
		if ((ref==null) || (ref.trim().length()==0)) {
			throw new JasperException("Directive js-import requires a 'ref' to import");
		}
		if (ctx.getExecCtx().getVariableType(ref)!=null) return;
		String typeName = ctx.getProcessorContext().getSystemAttribute(ref);
		if (typeName==null) {
			throw new JasperException("Directive js-import found an invalid ref '"+ref+"'");
		}
		
		code.append("var "+ref+" = window."+ref+";\n");
		ctx.getExecCtx().addVariable(ref, typeName);
	}

}

