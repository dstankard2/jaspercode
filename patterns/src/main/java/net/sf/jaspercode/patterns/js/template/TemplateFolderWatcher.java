package net.sf.jaspercode.patterns.js.template;

import java.io.InputStreamReader;

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
import net.sf.jaspercode.langsupport.javascript.ModuleImport;
import net.sf.jaspercode.langsupport.javascript.modules.ModuleFunction;
import net.sf.jaspercode.langsupport.javascript.modules.ModuleSourceFile;
import net.sf.jaspercode.langsupport.javascript.modules.StandardModuleSource;
import net.sf.jaspercode.langsupport.javascript.types.JavascriptServiceType;
import net.sf.jaspercode.patterns.PatternPriority;
import net.sf.jaspercode.patterns.js.template.parsing.TemplateParser;

public class TemplateFolderWatcher implements FolderWatcher {

	private ApplicationFolder folder = null;
	private ProcessorContext ctx = null;
	String folderTypeName = null;
	//private Map<String,TemplateFileWatcher> templates = new HashMap<>();
	
	public TemplateFolderWatcher(ApplicationFolder folder, String folderTypeName, String ref) {
		this.folder = folder;
		this.folderTypeName = folderTypeName;
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
		//JavascriptServiceType rootServiceType = JasperUtils.getType(JavascriptServiceType.class, rootServiceTypeName, ctx);
		String filename = changedFile.getName();
		String ruleName = null;

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
		JavascriptServiceType serviceType = JasperUtils.getType(JavascriptServiceType.class, this.folderTypeName, ctx);
		String objRef = "templates.pages";
		StandardModuleSource module = (StandardModuleSource)src.getModule(folderTypeName);
		
		handleFile(changedFile,ruleName,serviceType,module,objRef,src);
		//StandardModuleSource rootModule = (StandardModuleSource)src.getModule(rootServiceTypeName);


		/*
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
			*/
	}

	// TODO: objRef should probably come from a config property
	protected void handleFile(ApplicationFile file,String ruleName, JavascriptServiceType folderType, StandardModuleSource mod, String objRef, ModuleSourceFile src) throws JasperException {
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
		for(ModuleImport m : code.getModules()) {
			src.addModule(m);
		}
	}

}

