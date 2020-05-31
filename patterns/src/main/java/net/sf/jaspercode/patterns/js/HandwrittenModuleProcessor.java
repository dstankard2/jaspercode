package net.sf.jaspercode.patterns.js;

import net.sf.jaspercode.api.ComponentProcessor;
import net.sf.jaspercode.api.ProcessorContext;
import net.sf.jaspercode.api.annotation.Plugin;
import net.sf.jaspercode.api.annotation.Processor;
import net.sf.jaspercode.api.config.Component;
import net.sf.jaspercode.api.exception.JasperException;
import net.sf.jaspercode.langsupport.javascript.JavascriptUtils;
import net.sf.jaspercode.langsupport.javascript.types.ExportedModuleType;
import net.sf.jaspercode.langsupport.javascript.types.ModuleType;
import net.sf.jaspercode.patterns.xml.js.HandwrittenModule;

@Plugin
@Processor(componentClass = HandwrittenModule.class)
public class HandwrittenModuleProcessor implements ComponentProcessor {
	HandwrittenModule comp = null;
	ProcessorContext ctx = null;

	@Override
	public void init(Component component, ProcessorContext ctx) {
		this.comp = (HandwrittenModule)component;
		this.ctx = ctx;
	}

	@Override
	public void process() throws JasperException {
		String name = comp.getName();
		String exportTypeName = comp.getExportType();
		String ref = comp.getRef();
		String path = comp.getWebPath();
		ExportedModuleType exportType = null;
		String typeName = null;
		
		ctx.setLanguageSupport("Javascript");
		
		if (exportTypeName.equalsIgnoreCase("const")) {
			exportType = ExportedModuleType.CONST;
		} else if (exportTypeName.equalsIgnoreCase("function")) {
			exportType = ExportedModuleType.CONSTRUCTOR;
		} else {
			throw new JasperException("A handwritten Javascript module must have export type of either 'const' or 'function'");
		}

		if (name.trim().length()>0) {
			typeName = name;
		} else if (ref.trim().length()>0) {
			typeName = "Module_"+ref;
		} else {
			throw new JasperException("A handwritten Javascript module must have either 'name' or 'ref'");
		}

		if (path.trim().length()==0) {
			throw new JasperException("A handwritten Javascript module must have a 'path'");
		} else if (!path.endsWith(".js")) {
			throw new JasperException("A handwritten Javascript module must have a 'path' ending in '.js'");
		}
		String webPath = JavascriptUtils.getModulePath(path, ctx);

		ModuleType type = new ModuleType(typeName, webPath, exportType);
		ctx.addVariableType(type);

		if (ref.trim().length()>0) {
			ctx.addSystemAttribute(ref, typeName);
		}
	}

}
