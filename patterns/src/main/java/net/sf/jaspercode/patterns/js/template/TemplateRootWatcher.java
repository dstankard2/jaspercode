package net.sf.jaspercode.patterns.js.template;

import java.util.HashMap;
import java.util.Map;

import net.sf.jaspercode.api.JasperUtils;
import net.sf.jaspercode.api.ProcessorContext;
import net.sf.jaspercode.api.exception.JasperException;
import net.sf.jaspercode.api.resources.ApplicationFile;
import net.sf.jaspercode.api.resources.ApplicationFolder;
import net.sf.jaspercode.api.resources.FolderWatcher;
import net.sf.jaspercode.langsupport.javascript.JavascriptUtils;
import net.sf.jaspercode.langsupport.javascript.modules.ModuleSourceFile;
import net.sf.jaspercode.langsupport.javascript.modules.StandardModuleSource;
import net.sf.jaspercode.langsupport.javascript.types.JavascriptServiceType;
import net.sf.jaspercode.patterns.PatternPriority;
import net.sf.jaspercode.patterns.js.template.parsing.DirectiveUtils;

public class TemplateRootWatcher implements FolderWatcher {

	private ApplicationFolder rootFolder = null;
	private ProcessorContext ctx = null;
	private String rootPath = null;
	private String rootServiceTypeName = null;
	private Map<String,TemplateDirectoryWatcher> templateSets = new HashMap<>();
	
	public TemplateRootWatcher(ApplicationFolder rootFolder, String rootServiceTypeName) {
		this.rootFolder = rootFolder;
		this.rootPath = rootFolder.getPath();
		this.rootServiceTypeName = rootServiceTypeName;
	}

	@Override
	public void init(ProcessorContext ctx) {
		this.ctx = ctx;
	}

	@Override
	public int getPriority() {
		return PatternPriority.HTML_TEMPLATE - 2;
	}

	@Override
	public void process(ApplicationFile changedFile) throws JasperException {
		ctx.setLanguageSupport("Javascript");
		JavascriptServiceType rootServiceType = JasperUtils.getType(JavascriptServiceType.class, rootServiceTypeName, ctx);
		String filename = changedFile.getName();

		if ((!filename.endsWith(".htm")) && (!filename.endsWith(".html"))) {
			return;
		}

		String subName = changedFile.getPath().substring(rootPath.length());

		int i = subName.indexOf('/');
		if (i<=0) { // Skip this one
			return;
		}
		
		String folderName = subName.substring(0, i);
		if (templateSets.get(folderName)==null) {
			String typeName = rootServiceTypeName + "_" + JasperUtils.getUpperCamelName(folderName);
			ModuleSourceFile src = JavascriptUtils.getModuleSource(ctx);
			StandardModuleSource rootModule = (StandardModuleSource)src.getModule(rootServiceTypeName);
			String path = rootPath+folderName;
			ApplicationFolder templateFolder = (ApplicationFolder)rootFolder.getResource(folderName);
			TemplateDirectoryWatcher w = new TemplateDirectoryWatcher(templateFolder, src.getPath(), typeName, typeName);
			ctx.addFolderWatcher(path, w);
			StandardModuleSource mod = new StandardModuleSource(typeName);
			src.addModule(mod);
			rootModule.addProperty(folderName, typeName);
			rootModule.getInitCode().append("_" + folderName + " = " + typeName+"();\n");
			templateSets.put(folderName, w);
			JavascriptServiceType srv = new JavascriptServiceType(typeName, true, ctx);
			ctx.addVariableType(srv);

			mod.addInternalFunction(DirectiveUtils.getInvokeRem());
			mod.addInternalFunction(DirectiveUtils.getRem());
			mod.addInternalFunction(DirectiveUtils.getIns());
		}
	}

}

