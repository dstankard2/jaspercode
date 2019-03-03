package net.sf.jaspercode.patterns.js.template.parsing.directives;

import net.sf.jaspercode.api.JasperException;
import net.sf.jaspercode.api.annotation.Plugin;
import net.sf.jaspercode.patterns.js.template.parsing.DirectiveContext;
import net.sf.jaspercode.patterns.js.template.parsing.ElementDirective;

@Plugin
public class ImportModuleElementDirective implements ElementDirective {

	@Override
	public String getElementName() {
		return "js-import-module";
	}

	@Override
	public void generateCode(DirectiveContext ctx) throws JasperException {
		String refs = ctx.getDomAttributes().get("refs");
		String location = ctx.getDomAttributes().get("location");

		if (refs==null) {
			throw new JasperException("Element js-import-module requires attribute '"+refs+"'");
		}
		if (location==null) {
			throw new JasperException("Element js-import-module requires attribute '"+location+"'");
		}

		String[] moduleNames = refs.split(",");
		ctx.addModule(location, moduleNames);
	}

}

