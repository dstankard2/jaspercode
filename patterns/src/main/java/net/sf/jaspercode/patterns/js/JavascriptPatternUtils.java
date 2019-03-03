package net.sf.jaspercode.patterns.js;

import java.util.Set;

import net.sf.jaspercode.api.JasperException;
import net.sf.jaspercode.api.JasperUtils;
import net.sf.jaspercode.api.ProcessorContext;
import net.sf.jaspercode.langsupport.java.types.JavaVariableType;
import net.sf.jaspercode.langsupport.java.types.impl.JavaDataObjectType;
import net.sf.jaspercode.langsupport.javascript.types.JavascriptDataObjectType;

public class JavascriptPatternUtils {

	public static void addJavascriptTypeForJavaDataObjectType(JavaDataObjectType javaType,ProcessorContext ctx) throws JasperException {
		
		String name = javaType.getName();
		JavascriptDataObjectType jsType = new JavascriptDataObjectType(javaType.getName(),false,ctx);
		for(String a : javaType.getAttributeNames()) {
			String t = javaType.getAttributeType(a);
			jsType.addAttribute(a, t);
			JavaVariableType attrType = JasperUtils.getType(JavaVariableType.class, t, ctx);
			if (attrType instanceof JavaDataObjectType) {
				addJavascriptTypeForJavaDataObjectType((JavaDataObjectType)attrType,ctx);
			}
		}
		
		ctx.setLanguageSupport("Javascript");
		if (ctx.getVariableType(name)==null) {
			ctx.addVariableType(jsType);
		}

		ctx.setLanguageSupport("Java8");
	}

	public static AjaxClientProvider getAjaxClientProvider(String name,ProcessorContext ctx) {
		Set<Class<AjaxClientProvider>> classes = ctx.getApplicationContext().getPlugins(AjaxClientProvider.class);
		for(Class<AjaxClientProvider> cl : classes) {
			try {
				AjaxClientProvider val = cl.newInstance();
				if (val.getName().equals(name)) return val;
			} catch(Exception e) {
				ctx.getLog().error("Couldn't instantiate Ajax Client Provider "+cl.getCanonicalName());
				e.printStackTrace();
			}
		}
		return null;
	}

}
