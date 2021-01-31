package net.sf.jaspercode.patterns.java.dataobject;

import net.sf.jaspercode.api.ComponentProcessor;
import net.sf.jaspercode.api.JasperUtils;
import net.sf.jaspercode.api.ProcessorContext;
import net.sf.jaspercode.api.annotation.Plugin;
import net.sf.jaspercode.api.annotation.Processor;
import net.sf.jaspercode.api.config.Component;
import net.sf.jaspercode.api.exception.JasperException;
import net.sf.jaspercode.langsupport.java.types.impl.JavaDataObjectType;
import net.sf.jaspercode.langsupport.javascript.types.JavascriptDataObjectType;
import net.sf.jaspercode.patterns.xml.java.dataobject.JsonObject;

@Plugin
@Processor(componentClass = JsonObject.class)
public class JsonObjectProcessor implements ComponentProcessor {

	JsonObject comp = null;
	ProcessorContext ctx = null;
	
	@Override
	public void init(Component component, ProcessorContext ctx) {
		this.comp = (JsonObject)component;
		this.ctx = ctx;
	}

	@Override
	public void process() throws JasperException {
		String name = comp.getName();
		ctx.setLanguageSupport("Java8");
		
		if (name.trim().length()==0) {
			throw new JasperException("name attribute is required for JSON object");
		}

		JavaDataObjectType javaType = JasperUtils.getType(JavaDataObjectType.class, name, ctx);
		JavascriptDataObjectType newType = new JavascriptDataObjectType(name, false,ctx);
		
		ctx.setLanguageSupport("Javascript");
		for(String attr : javaType.getAttributeNames()) {
			String type = javaType.getAttributeType(attr);
			newType.addAttribute(attr, type);
		}
		ctx.addVariableType(newType);
	}

}

