package net.sf.jaspercode.patterns.js.template;

import java.io.InputStreamReader;

import org.apache.commons.lang3.tuple.Pair;

import net.sf.jaspercode.api.CodeExecutionContext;
import net.sf.jaspercode.api.JasperUtils;
import net.sf.jaspercode.api.ProcessorContext;
import net.sf.jaspercode.api.exception.JasperException;
import net.sf.jaspercode.api.resources.ApplicationFile;
import net.sf.jaspercode.api.resources.ApplicationFolder;
import net.sf.jaspercode.api.resources.FolderWatcher;
import net.sf.jaspercode.api.types.ServiceOperation;
import net.sf.jaspercode.langsupport.javascript.JavascriptCode;
import net.sf.jaspercode.langsupport.javascript.JavascriptUtils;
import net.sf.jaspercode.langsupport.javascript.modules.ModuleFunction;
import net.sf.jaspercode.langsupport.javascript.modules.ModuleSourceFile;
import net.sf.jaspercode.langsupport.javascript.modules.StandardModuleSource;
import net.sf.jaspercode.langsupport.javascript.types.ExportedModuleType;
import net.sf.jaspercode.langsupport.javascript.types.ModuleType;
import net.sf.jaspercode.patterns.js.template.parsing.DirectiveUtils;
import net.sf.jaspercode.patterns.js.template.parsing.TemplateParser;

public class TemplateSingleFolderWatcher implements FolderWatcher {

	private ApplicationFolder folder = null;
	int priority = 0;
	
	private String serviceRef = null;
	private String serviceName = null;
	
	public TemplateSingleFolderWatcher(String serviceRef, String serviceName, int priority, ApplicationFolder folder) {
		this.serviceRef = serviceRef;
		this.serviceName = serviceName;
		this.folder = folder;
		this.priority = priority;
	}

	@Override
	public String getName() {
		return "TemplateSingleFolderWatcher["+folder.getPath()+"]";
	}

	// This watcher is processed just after the template set component
	@Override
	public int getPriority() {
		return priority;
	}

	protected Pair<ModuleType,StandardModuleSource> ensureFolderInfo(ProcessorContext ctx) throws JasperException {
		ModuleType folderType = null;
		StandardModuleSource module = null;
		//new StandardModuleSource(folderTypeName);
		//JavascriptServiceType rootType = templatesInfo.getKey();
		ModuleSourceFile src = JavascriptUtils.getModuleSource(ctx);

		ctx.addSystemAttribute(serviceRef, serviceName);
		try {
			folderType = JasperUtils.getType(ModuleType.class, serviceName, ctx);
		} catch(JasperException e) {
		}
		
		if (folderType==null) {
			String webPath = JavascriptUtils.getModulePath(ctx);
			folderType = new ModuleType(serviceName,webPath, ExportedModuleType.CONST);
			//folderType = new JavascriptServiceType(serviceName,true,ctx);
			ctx.addVariableType(folderType);
			module = new StandardModuleSource(serviceName);
			module.setExportType(ExportedModuleType.CONST);
			src.addModule(module);
			ctx.addVariableType(folderType);
			DirectiveUtils.ensureIns(src);
			DirectiveUtils.ensureRem(src);
			DirectiveUtils.ensureInvokeRem(src);
		} else {
			ctx.originateVariableType(folderType);
			module = (StandardModuleSource)src.getModule(serviceName);
			if (module==null) {
				module = new StandardModuleSource(serviceName);
				module.setExportType(ExportedModuleType.CONST);
				src.addModule(module);
				ctx.addVariableType(folderType);
				DirectiveUtils.ensureIns(src);
				DirectiveUtils.ensureRem(src);
				DirectiveUtils.ensureInvokeRem(src);
			}
		}
		
		ctx.originateVariableType(folderType);

		return Pair.of(folderType,module);
	}

	@Override
	public void process(ProcessorContext ctx, ApplicationFile changedFile) throws JasperException {
		String filename = changedFile.getName();
		String ruleName = null;
		
		ctx.setLanguageSupport("Javascript");

		if (changedFile.getFolder()!=folder) {
			return;
		}
		if (filename.endsWith(".html")) {
			ruleName = filename.substring(0, filename.length()-5);
		} else if (filename.endsWith(".htm")) {
			ruleName = filename.substring(0, filename.length()-4);
		} else {
			return;
		}

		ModuleSourceFile src = JavascriptUtils.getModuleSource(ctx);

		Pair<ModuleType,StandardModuleSource> folderInfo = ensureFolderInfo(ctx);

		ModuleType serviceType = folderInfo.getKey();
		StandardModuleSource module = folderInfo.getRight();

		// TODO: Figure this out
		String objRef = "templates.pages";

		handleFile(ctx, changedFile,ruleName,serviceType,module,objRef,src);

	}

	// TODO: objRef should probably come from a config property
	protected void handleFile(ProcessorContext ctx,ApplicationFile file,String ruleName, ModuleType folderType, StandardModuleSource mod, String objRef, ModuleSourceFile src) throws JasperException {
		int c = 0;
		StringBuilder templateString = new StringBuilder();

		try (InputStreamReader reader = new InputStreamReader(file.getInputStream())) {
			while((c = reader.read())>=0) {
				templateString.append((char)c);
			}
		} catch(Exception e) {
			throw new JasperException("Couldn't read HTML template file",e);
		}

		ServiceOperation op = new ServiceOperation(ruleName);
		ModuleFunction fn = new ModuleFunction();
		fn.setName(ruleName);
		op.returnType("DOMElement");
		ctx.getLog().info("Parsing template "+file.getPath());
		TemplateParser parser = new TemplateParser(templateString.toString(),ctx,objRef,op);
		CodeExecutionContext execCtx = new CodeExecutionContext(ctx);
		JavascriptCode code = parser.generateJavascriptCode(execCtx);
		fn.setParameters(op);
		fn.setCode(code);
		mod.addFunction(fn);
		folderType.addOperation(op);
		ctx.originateVariableType(folderType);
		for(Pair<String,String> s : code.getImportedModules()) {
			src.importModule(s);
		}
	}

}

