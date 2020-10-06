package net.sf.jaspercode.patterns.js.template.parsing.directives;

import net.sf.jaspercode.api.JasperUtils;
import net.sf.jaspercode.api.annotation.Plugin;
import net.sf.jaspercode.api.exception.JasperException;
import net.sf.jaspercode.langsupport.javascript.types.ExportedModuleType;
import net.sf.jaspercode.langsupport.javascript.types.ModuleType;
import net.sf.jaspercode.patterns.js.template.parsing.DirectiveContext;
import net.sf.jaspercode.patterns.js.template.parsing.ElementDirective;

@Plugin
public class ImportElementDirective implements ElementDirective {

	@Override
	public String getElementName() {
		return "js-import";
	}

	@Override
	public void generateCode(DirectiveContext ctx) throws JasperException {
		String ref = ctx.getDomAttribute("ref");
		String typeName = ctx.getDomAttribute("type");
		StringBuilder code = ctx.getCode();
		ModuleType type = null;
		
		if (ctx.getExecCtx().getVariableType(ref)!=null) return;
		
		if ((typeName!=null) && (ref!=null)) {
			if ((ctx.getProcessorContext().getSystemAttribute(ref)!=null) 
					&& (!typeName.equals(ctx.getProcessorContext().getSystemAttribute(ref)))) {
				throw new JasperException("Found inconsistent types for import ref '"+ref+"'");
			}
		} else if (typeName!=null) {
			// no-op
		} else if (ref!=null) {
			typeName = ctx.getProcessorContext().getSystemAttribute(ref);
			if (typeName==null) {
				throw new JasperException("Attempted to import unknown ref '"+ref+"'");
			}
		} else {
			throw new JasperException("Import element directive must have a 'ref' or a 'type'");
		}

		type = JasperUtils.getType(ModuleType.class, typeName, ctx.getProcessorContext());

		ctx.importModule(typeName, type.getWebPath());
		if (type.getExportType()==ExportedModuleType.CONSTRUCTOR) {
			if (ref!=null) {
				code.append("let "+ref+" = "+typeName+"();\n");
			}
		} else if (type.getExportType()==ExportedModuleType.CONST) {
			if (ref!=null) {
				code.append("let "+ref+" = "+typeName+";\n");
			}
		}
		
		if (ref!=null) {
			ctx.getExecCtx().addVariable(ref, typeName);
		}
		
	}

}

