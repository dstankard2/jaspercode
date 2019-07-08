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
import net.sf.jaspercode.langsupport.javascript.JavascriptUtils;
import net.sf.jaspercode.langsupport.javascript.modules.ModuleSourceFile;
import net.sf.jaspercode.langsupport.javascript.modules.StandardModuleSource;
import net.sf.jaspercode.langsupport.javascript.types.JavascriptServiceType;
import net.sf.jaspercode.patterns.js.template.parsing.DirectiveUtils;
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
		ModuleSourceFile src = JavascriptUtils.getModuleSource(ctx);

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

		JavascriptServiceType type = new JavascriptServiceType(serviceName,true,ctx);
		ctx.addSystemAttribute(ref, serviceName);
		ctx.addVariableType(type);

		StandardModuleSource rootModule = new StandardModuleSource(serviceName);
		src.addModule(rootModule);

		for(TemplateFolder folder : comp.getFolder()) {
			// Create a type for the folder
			String folderRef = folder.getRef();
			String folderTypeName = serviceName+"_"+folderRef;
			
			// Add folder type to the service type
			type.addAttribute(folderRef, folderTypeName);
			ApplicationResource res = ctx.getResource(folder.getPath());
			if (res==null) {
				throw new JasperException("Couldn't find folder directory at path '"+folder.getPath()+"'");
			}
			if (!(res instanceof ApplicationFolder)) {
				throw new JasperException("Resource '"+folder.getPath()+"' is not an application resource");
			}
			TemplateFolderWatcher watcher = new TemplateFolderWatcher((ApplicationFolder)res, folderTypeName, folderRef);
			ctx.addFolderWatcher(folder.getPath(), watcher);
			
			//ModuleSourceFile src = JavascriptUtils.getModuleSource(ctx);
			//StandardModuleSource rootModule = (StandardModuleSource)src.getModule(rootServiceTypeName);
			rootModule.addProperty(folderRef, folderTypeName);
			rootModule.getInitCode().append("_" + folderRef + " = " + folderTypeName+"();\n");
			
			StandardModuleSource mod = new StandardModuleSource(folderTypeName);
			src.addModule(mod);
			// Templating framework
			mod.addInternalFunction(DirectiveUtils.getInvokeRem());
			mod.addInternalFunction(DirectiveUtils.getRem());
			mod.addInternalFunction(DirectiveUtils.getIns());
			
			JavascriptServiceType folderType = new JavascriptServiceType(folderTypeName,true,ctx);
			ctx.addVariableType(folderType);
		}
	}

	/*
	protected void handleApplicationFolder(ApplicationFolder templateFolder,String ref, String path, JavascriptServiceType type, ModuleSourceFile src, StandardModuleSource rootModule) throws JasperException {
		List<String> names =  templateFolder.getContentNames();
		JavascriptServiceType folderType = null;
		String subName = path.replace('/', '_');
		subName = subName.replace('-', '_');
		String folderName = JasperUtils.getUpperCamelName(subName);
		String identifier = ref.substring(ref.indexOf('.')+1);

		String folderTypeName = null;
		if (ctx.getVariableType(subName)!=null) {
			folderTypeName = type.getName() + '_' + folderName;
		} else {
			folderTypeName = folderName;
		}
		rootModule.addProperty(identifier, "object");
		rootModule.getInitCode().append("_"+identifier+" = "+folderName+"();\n");
		
		StandardModuleSource mod = new StandardModuleSource(folderTypeName);
		folderType = new JavascriptServiceType(folderTypeName, true, ctx);
		type.addAttribute(identifier, folderTypeName);
		ctx.addVariableType(folderType);
		ctx.addSystemAttribute(ref, folderTypeName);
		src.addModule(mod);

		addStandardFunctions(mod);

		for(String name : names) {
			//if (!name.equals("movementMode.html")) continue;
			if (!name.endsWith(".html")) continue;
			String ruleName = JasperUtils.getLowerCamelName(name.substring(0,name.indexOf(".html")));
			ApplicationResource res = templateFolder.getResource(name);
			if (res instanceof ApplicationFile) {
				handleFile((ApplicationFile)res, ruleName, folderType, mod, ref, src);
			}
		}
	}

	protected void addStandardFunctions(StandardModuleSource mod) {
		mod.addInternalFunction(DirectiveUtils.getInvokeRem());
		mod.addInternalFunction(DirectiveUtils.getRem());
		mod.addInternalFunction(DirectiveUtils.getIns());
	}
	*/
	
}

