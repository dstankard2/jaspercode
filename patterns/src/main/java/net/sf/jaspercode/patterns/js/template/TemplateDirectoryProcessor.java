package net.sf.jaspercode.patterns.js.template;

import java.io.InputStreamReader;
import java.util.List;

import net.sf.jaspercode.api.CodeExecutionContext;
import net.sf.jaspercode.api.ComponentProcessor;
import net.sf.jaspercode.api.JasperUtils;
import net.sf.jaspercode.api.ProcessorContext;
import net.sf.jaspercode.api.annotation.Plugin;
import net.sf.jaspercode.api.annotation.Processor;
import net.sf.jaspercode.api.config.Component;
import net.sf.jaspercode.api.exception.JasperException;
import net.sf.jaspercode.api.resources.ApplicationFile;
import net.sf.jaspercode.api.resources.ApplicationFolder;
import net.sf.jaspercode.api.resources.ApplicationResource;
import net.sf.jaspercode.api.types.ServiceOperation;
import net.sf.jaspercode.langsupport.javascript.JavascriptCode;
import net.sf.jaspercode.langsupport.javascript.JavascriptUtils;
import net.sf.jaspercode.langsupport.javascript.ModuleImport;
import net.sf.jaspercode.langsupport.javascript.modules.ModuleFunction;
import net.sf.jaspercode.langsupport.javascript.modules.ModuleSourceFile;
import net.sf.jaspercode.langsupport.javascript.modules.StandardModuleSource;
import net.sf.jaspercode.langsupport.javascript.types.JavascriptServiceType;
import net.sf.jaspercode.patterns.js.template.parsing.DirectiveUtils;
import net.sf.jaspercode.patterns.js.template.parsing.TemplateParser;
import net.sf.jaspercode.patterns.xml.js.template.Subfolder;
import net.sf.jaspercode.patterns.xml.js.template.TemplateDirectory;

@Plugin
@Processor(componentClass=TemplateDirectory.class)
public class TemplateDirectoryProcessor implements ComponentProcessor {

	private TemplateDirectory comp = null;
	private ProcessorContext ctx = null;

	@Override
	public void init(Component component, ProcessorContext ctx) {
		this.comp = (TemplateDirectory)component;
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

		ApplicationResource res = ctx.getBuildContext().getApplicationResource(comp.getFolder());
		if (res==null) {
			throw new JasperException("Could not find template folder '"+comp.getFolder()+"'");
		}
		if (!(res instanceof ApplicationFolder)) {
			throw new JasperException("Resource '"+comp.getFolder()+"' specified as template folder is not a folder");
		}
		ApplicationFolder root = (ApplicationFolder)res;

		StandardModuleSource rootModule = new StandardModuleSource(serviceName);
		src.addModule(rootModule);
		
		TemplateRootWatcher watcher = new TemplateRootWatcher(root, serviceName);
		ctx.addFolderWatcher(root.getPath(), watcher);
		/*
		// Each subfolder has a template directory watcher
		for(Subfolder sub : comp.getSubfolder()) {
			String path = sub.getPath();
			String r = sub.getRef();
			String fullRef = ref+'.'+r;
			ApplicationResource templateFolder = root.getResource(path);
			if (templateFolder==null) {
				throw new JasperException("Couldn't find subfolder '"+path+"'");
			}
			if (!(templateFolder instanceof ApplicationFolder)) {
				throw new JasperException("Resource '"+templateFolder.getPath()+"' is not a folder");
			}
			TemplateDirectoryWatcher folderWatcher = new TemplateDirectoryWatcher((ApplicationFolder)templateFolder);
			ctx.addFolderWatcher(templateFolder.getPath(), folderWatcher);
			//handleApplicationFolder((ApplicationFolder)templateFolder, fullRef, path, type, src, rootModule);
		}
		 */
		//ctx.getLog().info("Test");
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

