package net.sf.jaspercode.patterns.java.handwritten;

import java.util.HashMap;
import java.util.Map;

import net.sf.jaspercode.api.ProcessorContext;
import net.sf.jaspercode.api.exception.JasperException;
import net.sf.jaspercode.api.resources.ApplicationFile;
import net.sf.jaspercode.api.resources.FolderWatcher;

public class HandwrittenCodeFolderWatcher implements FolderWatcher {
	private ProcessorContext ctx = null;
	Map<String,HandwrittenCodeFileWatcher> fileWatchers = new HashMap<>();
	
	public HandwrittenCodeFolderWatcher() {
	}

	@Override
	public void init(ProcessorContext ctx) {
		this.ctx = ctx;
	}

	@Override
	public void process(ApplicationFile applicationFile) throws JasperException {
		String name = applicationFile.getName();
		if (!name.endsWith(".java")) return;
		
		String path = applicationFile.getPath();
		if (fileWatchers.get(path)!=null) return;
		
		HandwrittenCodeFileWatcher w = new HandwrittenCodeFileWatcher(path);
		ctx.addFileWatcher(path, w);
		fileWatchers.put(path, w);
	}

	@Override
	public int getPriority() {
		return 0;
	}

}
