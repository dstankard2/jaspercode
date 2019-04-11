package net.sf.jaspercode.patterns.js.page;

import net.sf.jaspercode.api.ProcessorContext;
import net.sf.jaspercode.api.exception.JasperException;
import net.sf.jaspercode.langsupport.javascript.JavascriptCode;
import net.sf.jaspercode.langsupport.javascript.types.JavascriptType;

public class PageModelType extends JavascriptType {

	public PageModelType(String pageName,boolean module,ProcessorContext ctx) throws JasperException {
		super(PageUtils.getPageModelTypeName(pageName),false,ctx);
	}
	
	public JavascriptCode instantiate(String ref) {
		ctx.getLog().warn("Attempted to instantiate PageModel type with name '"+getName()+"' but this action is not supported");
		return null;
	}
	
}
