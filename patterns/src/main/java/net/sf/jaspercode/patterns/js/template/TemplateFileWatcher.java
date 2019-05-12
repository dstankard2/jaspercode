package net.sf.jaspercode.patterns.js.template;

import java.io.InputStreamReader;

import net.sf.jaspercode.api.CodeExecutionContext;
import net.sf.jaspercode.api.JasperUtils;
import net.sf.jaspercode.api.ProcessorContext;
import net.sf.jaspercode.api.exception.JasperException;
import net.sf.jaspercode.api.resources.ApplicationFile;
import net.sf.jaspercode.api.resources.FileWatcher;
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

public class TemplateFileWatcher implements FileWatcher {
	private ProcessorContext ctx = null;
	private String srcPath = null;
	private String module = null;
	private String typeName = null;
	
	public TemplateFileWatcher(String srcPath, String module, String typeName) {
		this.srcPath = srcPath;
		this.module = module;
		this.typeName = typeName;
	}

	@Override
	public void init(ProcessorContext ctx) {
		this.ctx = ctx;
	}

	@Override
	public int getPriority() {
		return PatternPriority.HTML_TEMPLATE;
	}

	@Override
	public void process(ApplicationFile applicationFile) throws JasperException {
		ctx.setLanguageSupport("Javascript");
		ModuleSourceFile src = JasperUtils.getSourceFile(ModuleSourceFile.class, srcPath, ctx);
		JavascriptServiceType type = JasperUtils.getType(JavascriptServiceType.class, typeName, ctx);

		String ruleName = applicationFile.getName();
		ruleName = ruleName.substring(0, ruleName.indexOf(".htm"));
		StandardModuleSource mod = (StandardModuleSource)src.getModule(module);
		handleFile(applicationFile, ruleName, type, mod, "obj", src);
	}

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
		for(ModuleImport m : code.getModules()) {
			src.addModule(m);
		}
	}

}
