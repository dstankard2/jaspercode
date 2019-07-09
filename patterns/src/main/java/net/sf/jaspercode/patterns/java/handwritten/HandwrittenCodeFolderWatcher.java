package net.sf.jaspercode.patterns.java.handwritten;

import java.io.IOException;

import net.sf.jaspercode.api.ProcessorContext;
import net.sf.jaspercode.api.exception.JasperException;
import net.sf.jaspercode.api.resources.ApplicationFile;
import net.sf.jaspercode.api.resources.FolderWatcher;

public class HandwrittenCodeFolderWatcher implements FolderWatcher {
	private ProcessorContext ctx = null;

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
			try {
				FileProcessor fileProc = new FileProcessor(applicationFile, ctx);
				fileProc.process();
			} catch(IOException e) {
				throw new JasperException("Couldn't process file '"+applicationFile.getPath()+"'", e);
			}
			/*
			String path = applicationFile.getPath();
			if (!watchedFiles.contains(path)) {
				HandwrittenCodeFileWatcher fileWatcher = new HandwrittenCodeFileWatcher(path);
				watchedFiles.add(path);
				ctx.addFileWatcher(path,fileWatcher);
			}
			*/
		}
	}

	@Override
	public int getPriority() {
		return 0;
	}

}
