package net.sf.jaspercode.patterns.js.template;

import net.sf.jaspercode.api.ComponentProcessor;
import net.sf.jaspercode.api.JasperUtils;
import net.sf.jaspercode.api.ProcessorContext;
import net.sf.jaspercode.api.annotation.Plugin;
import net.sf.jaspercode.api.annotation.Processor;
import net.sf.jaspercode.api.config.Component;
import net.sf.jaspercode.api.exception.JasperException;
import net.sf.jaspercode.api.resources.ApplicationFolder;
import net.sf.jaspercode.api.resources.ApplicationResource;
import net.sf.jaspercode.patterns.PatternPriority;
import net.sf.jaspercode.patterns.xml.js.template.TemplateFolder;
import net.sf.jaspercode.patterns.xml.js.template.TemplateSets;

@Plugin
@Processor(componentClass=TemplateSets.class)
public class TemplateSetsProcessor implements ComponentProcessor {

	private TemplateSets comp = null;
	private ProcessorContext ctx = null;

	@Override
	public void init(Component component, ProcessorContext ctx) {
		this.comp = (TemplateSets)component;
		this.ctx = ctx;
	}

	@Override
	public void process() throws JasperException {
		ctx.setLanguageSupport("Javascript");

		String serviceName = comp.getServiceName();
		if (serviceName.trim().isEmpty()) {
			throw new JasperException("Template directory requires a 'serviceName' attribute");
		}
		if (!JasperUtils.isUpperCamelName(serviceName)) {
			throw new JasperException("Template directory serviceName should be a type name");
		}

		String ref = JasperUtils.getLowerCamelName(serviceName);
		if (ref.equals(serviceName)) {
			throw new JasperException("Could not determine ref for serviceName = '"+serviceName+"'");
		}

		//JavascriptServiceType type = new JavascriptServiceType(serviceName,true,ctx);
		//ctx.addSystemAttribute(ref, serviceName);
		//ctx.addVariableType(type);

		int priority = PatternPriority.HTML_TEMPLATE;

		for(TemplateFolder folder : comp.getFolder()) {

			priority++;

			// Create a type for the folder
			String folderRef = folder.getRef();
			String folderTypeName = serviceName+"_"+folderRef;

			// Add folder type to the service type
			//type.addAttribute(folderRef, folderTypeName);
			ApplicationResource res = ctx.getResource(folder.getPath());
			if (res==null) {
				throw new JasperException("Couldn't find folder directory at path '"+folder.getPath()+"'");
			}
			if (!(res instanceof ApplicationFolder)) {
				throw new JasperException("Resource '"+folder.getPath()+"' is not an application resource");
			}
			TemplateFolderWatcher watcher = new TemplateFolderWatcher(ref, serviceName, folderRef, folderTypeName, priority, (ApplicationFolder)res);
			ctx.addFolderWatcher(res.getPath(), watcher);

			/*
			StandardModuleSource mod = new StandardModuleSource(folderTypeName);
			src.addModule(mod);
			// Templating framework
			mod.addInternalFunction(DirectiveUtils.getInvokeRem());
			mod.addInternalFunction(DirectiveUtils.getRem());
			mod.addInternalFunction(DirectiveUtils.getIns());
			
			JavascriptServiceType folderType = new JavascriptServiceType(folderTypeName,true,ctx);
			ctx.addVariableType(folderType);
			*/
		}
	}

}

