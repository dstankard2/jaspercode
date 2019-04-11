package net.sf.jaspercode.patterns.js.page;

import net.sf.jaspercode.api.JasperUtils;
import net.sf.jaspercode.api.ProcessorContext;
import net.sf.jaspercode.api.exception.JasperException;
import net.sf.jaspercode.api.types.ServiceOperation;
import net.sf.jaspercode.langsupport.javascript.types.JavascriptServiceType;

public class PageUtils {

	public static PageInfo getPageInfo(String pageName, ProcessorContext ctx) {
		PageInfo ret = null;
		
		ret = (PageInfo)ctx.getObject("jaspercode.patterns.page."+pageName);
		
		return ret;
	}
	
	public static void addPageInfo(PageInfo pageInfo, ProcessorContext ctx) throws JasperException {
		String pageName = pageInfo.getName();
		ctx.setObject("jaspercode.patterns.page."+pageName, pageInfo);
		PageBuilderComponent comp = new PageBuilderComponent(pageInfo);
		ctx.addComponent(comp);
		
		// Create model type for this page
		PageModelType modelType = new PageModelType(pageName,false,ctx);
		ctx.addVariableType(modelType);
		JavascriptServiceType pageType = new JavascriptServiceType(pageName,true,ctx);
		ctx.addVariableType(pageType);
		pageInfo.setModelType(modelType);
		pageInfo.setPageType(pageType);

		// Add attributes to page type
		pageType.addAttribute("model", modelType.getName());
		ServiceOperation event = new ServiceOperation("event");
		event.addParam("eventName", "string");
		event.addParam("callback", "function");
		pageType.addOperation(event);
	}
	
	public static String getPageModelTypeName(String pageName) {
		return "PageModel_"+pageName;
	}

	public static PageModelType getPageModelType(String pageName,ProcessorContext ctx) throws JasperException {
		String name = getPageModelTypeName(pageName);
		return JasperUtils.getType(PageModelType.class, name, ctx);
	}
	
	public static void addModelAttribute(String pageName,String attrib,String type,ProcessorContext ctx) throws JasperException {
		PageModelType t = getPageModelType(pageName,ctx);
		t.addAttribute(attrib, type);
	}
	
	public static JavascriptServiceType getPageType(String pageName,ProcessorContext ctx) throws JasperException {
		return JasperUtils.getType(JavascriptServiceType.class, "Page_"+pageName, ctx);
	}
	
}

