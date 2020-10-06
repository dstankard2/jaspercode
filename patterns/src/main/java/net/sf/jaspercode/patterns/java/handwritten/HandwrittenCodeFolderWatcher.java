package net.sf.jaspercode.patterns.java.handwritten;

import net.sf.jaspercode.api.ProcessorContext;
import net.sf.jaspercode.api.exception.JasperException;
import net.sf.jaspercode.api.resources.ApplicationFile;
import net.sf.jaspercode.api.resources.FolderWatcher;

public class HandwrittenCodeFolderWatcher implements FolderWatcher {
	//private ProcessorContext ctx = null;
	private String path = null;
	
	public HandwrittenCodeFolderWatcher(String path) {
		this.path = path;
	}

	@Override
	public String getName() {
		return "HandwrittenCodeFolderWatcher["+path+"]";
	}

	/*
	@Override
	public void init(ProcessorContext ctx) {
		this.ctx = ctx;
	}
	*/

	@Override
	public void process(ProcessorContext ctx, ApplicationFile applicationFile) throws JasperException {
		String name = applicationFile.getName();
		if (!name.endsWith(".java")) return;
		
		String path = applicationFile.getPath();
		
		HandwrittenCodeFileProcessor w = new HandwrittenCodeFileProcessor(path);
		ctx.addFileProcessor(path, w);
	}

	@Override
	public int getPriority() {
		return 0;
	}

}
