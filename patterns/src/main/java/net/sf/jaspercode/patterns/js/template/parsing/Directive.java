package net.sf.jaspercode.patterns.js.template.parsing;

import net.sf.jaspercode.api.JasperException;

public interface Directive {

	public void generateCode(DirectiveContext ctx) throws JasperException;

}

