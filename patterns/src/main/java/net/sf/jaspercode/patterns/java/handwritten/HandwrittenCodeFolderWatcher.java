package net.sf.jaspercode.patterns.java.handwritten;

import java.util.HashSet;
import java.util.Set;

import net.sf.jaspercode.api.ProcessorContext;
import net.sf.jaspercode.api.exception.JasperException;
import net.sf.jaspercode.api.resources.ApplicationFile;
import net.sf.jaspercode.api.resources.FolderWatcher;

public class HandwrittenCodeFolderWatcher implements FolderWatcher {
	private ProcessorContext ctx = null;
	private Set<String> watchedFiles = new HashSet<>();

	public HandwrittenCodeFolderWatcher() {
	}
	
	@Override
	public void init(ProcessorContext ctx) {
		this.ctx = ctx;
	}

	@Override
	public void process(ApplicationFile applicationFile) throws JasperException {
		String name = applicationFile.getName();
		if (name.endsWith(".java")) {
			String path = applicationFile.getPath();
			if (!watchedFiles.contains(path)) {
				HandwrittenCodeFileWatcher fileWatcher = new HandwrittenCodeFileWatcher(path);
				watchedFiles.add(path);
				ctx.addFileWatcher(path,fileWatcher);
			}
		}
	}

	@Override
	public int getPriority() {
		return 0;
	}

}
