package net.sf.jaspercode.patterns.http;

import net.sf.jaspercode.api.ProcessorContext;

public class WebUtils {

	private static final String WEBSERVICE_DEFINITIONS = "PATTERNS_HTTP_ENDPOINTS";

	public static WebServiceContext getWebServiceDefinition(String name,ProcessorContext ctx,boolean force) {
		WebServiceContext ret = null;
		
		if ((name==null) || (name.trim().length()==0)) name = ctx.getBuildContext().getName();
		ret = (WebServiceContext)ctx.getObject(WEBSERVICE_DEFINITIONS+"_" + name);
		if ((ret==null) && (force)) {
			ret = new WebServiceContext();
			ret.setContextRoot(name);
			ctx.setObject(WEBSERVICE_DEFINITIONS+"_"+name, ret);
		}

		return ret;
	}

	public static WebServiceContext getWebServiceDefinition(ProcessorContext ctx) {
		String name = ctx.getBuildContext().getName();
		return getWebServiceDefinition(name,ctx,true);
	}
}

