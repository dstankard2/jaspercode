package net.sf.jaspercode.patterns.js.template.parsing.directives;

import net.sf.jaspercode.api.JasperException;
import net.sf.jaspercode.api.annotation.Plugin;
import net.sf.jaspercode.api.types.ServiceOperation;
import net.sf.jaspercode.patterns.js.template.parsing.DirectiveContext;
import net.sf.jaspercode.patterns.js.template.parsing.ElementDirective;

@Plugin
public class ParamElementDirective implements ElementDirective {

	@Override
	public String getElementName() {
		return "js-param";
	}

	@Override
	public void generateCode(DirectiveContext ctx) throws JasperException {
		String name = ctx.getDomAttributes().get("name");
		String type = ctx.getDomAttributes().get("type");

		if ((name==null) || (name.trim().length()==0)) {
			throw new JasperException("Directive js-param requires a name");
		}
		
		if ((type==null) || (type.trim().length()==0)) {
			type = ctx.getProcessorContext().getSystemAttribute(name);
		}
		if (type==null) throw new JasperException("Couldn't find a type for template parameter '"+name+"'");
		ServiceOperation op = ctx.getFunction();
		if (op.getParamNames().contains(name)) {
			throw new JasperException("Template function already has a parameter called '"+name+"'");
		}
		op.addParam(name, type);
		ctx.getExecCtx().addVariable(name, type);
	}

}
