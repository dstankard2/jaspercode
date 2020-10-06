package net.sf.jaspercode.patterns.js.template.parsing.directives;

import net.sf.jaspercode.api.annotation.Plugin;
import net.sf.jaspercode.api.exception.JasperException;
import net.sf.jaspercode.patterns.js.template.parsing.DirectiveContext;
import net.sf.jaspercode.patterns.js.template.parsing.ElementDirective;

@Plugin
public class VarElementDirective implements ElementDirective {

	@Override
	public String getElementName() {
		return "js-var";
	}

	@Override
	public void generateCode(DirectiveContext ctx) throws JasperException {
		StringBuilder b = ctx.getCode();
		String name = ctx.getDomAttribute("name");
		String type = ctx.getDomAttribute("type");
		String value = ctx.getDomAttribute("value");

		if ((name==null) || (name.trim().length()==0)) {
			throw new JasperException("js-var element directive required attribute 'name'");
		}

		if (ctx.getExecCtx().getVariableType(name)!=null) {
			throw new JasperException("Cannot declare a variable named '"+name+"' as there is already another one");
		}
		
		if ((type!=null) && (type.trim().length()==0)) {
			type = null;
		}
		if (type==null) {
			type = ctx.getProcessorContext().getSystemAttribute(name);
			if (type==null) {
				throw new JasperException("Couldn't find a type for variable '"+name+"'");
			}
		} else {
			String test = ctx.getProcessorContext().getSystemAttribute(name);
			if ((test!=null) && (!test.equals(type))) {
				throw new JasperException("Couldn't declare variable '"+name+"' of type '"+type+"' as it conflicts with an existing system attribute");
			}
		}
		
		b.append("var "+name+";\n");
		if (value!=null) {
			b.append(name + " = "+value+";\n");
		}
		ctx.getExecCtx().addVariable(name, type);
	}

}

