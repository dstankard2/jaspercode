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
	int priority = 0;
	
	public TemplateFolderWatcher(ApplicationFolder folder, String folderTypeName, String ref, int prirority) {
		this.folder = folder;
		this.folderTypeName = folderTypeName;
		this.priority = priority;
	}

	@Override
	public void init(ProcessorContext ctx) {
		this.ctx = ctx;

	}

	// This watcher is processed just after the template set component
	@Override
	public int getPriority() {
		return priority;
	}

	@Override
	public void process(ApplicationFile changedFile) throws JasperException {
		ctx.setLanguageSupport("Javascript");
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
		ctx.originateVariableType(serviceType);

		handleFile(changedFile,ruleName,serviceType,module,objRef,src);

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

