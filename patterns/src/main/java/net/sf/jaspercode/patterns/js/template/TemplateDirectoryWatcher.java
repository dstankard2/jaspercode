package net.sf.jaspercode.patterns.js.template;

import java.util.HashMap;
import java.util.Map;

import net.sf.jaspercode.api.JasperUtils;
import net.sf.jaspercode.api.ProcessorContext;
import net.sf.jaspercode.api.exception.JasperException;
import net.sf.jaspercode.api.resources.ApplicationFile;
import net.sf.jaspercode.api.resources.ApplicationFolder;
import net.sf.jaspercode.api.resources.FolderWatcher;
import net.sf.jaspercode.langsupport.javascript.modules.ModuleSourceFile;
import net.sf.jaspercode.patterns.PatternPriority;

public class TemplateDirectoryWatcher implements FolderWatcher {
	private Map<String,TemplateFileWatcher> watchers = new HashMap<>();
	private ApplicationFolder folder = null;
	private ProcessorContext ctx = null;
	private String sourcePath = null;
	//private String folderName = null;
	private String typeName = null;
	private String module = null;
	
	public TemplateDirectoryWatcher(ApplicationFolder folder, String sourcePath, /*String folderName, */String typeName, String module) {
		this.folder = folder;
		this.sourcePath = sourcePath;
		//this.folderName = folderName;
		this.typeName = typeName;
		this.module = module;
	}

	@Override
	public void init(ProcessorContext ctx) {
		this.ctx = ctx;
	}

	@Override
	public int getPriority() {
		return PatternPriority.HTML_TEMPLATE - 1;
	}

	@Override
	public void process(ApplicationFile changedFile) throws JasperException {
		if (changedFile.getFolder()!=folder) return;
		String name = changedFile.getName();
		
		if ((!name.endsWith(".html")) && (!name.endsWith(".htm"))) {
			return;
		}
		
		if (watchers.get(name)==null) {
			TemplateFileWatcher w = new TemplateFileWatcher(sourcePath, module, typeName);
			ctx.addFileWatcher(changedFile.getPath(), w);
			watchers.put(name, w);
		}
		
		//ModuleSourceFile src = JasperUtils.getSourceFile(ModuleSourceFile.class, sourcePath, ctx);
	}

}
