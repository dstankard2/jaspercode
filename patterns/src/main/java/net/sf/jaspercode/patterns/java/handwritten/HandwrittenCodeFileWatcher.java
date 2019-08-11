package net.sf.jaspercode.patterns.java.handwritten;

import java.io.IOException;

import net.sf.jaspercode.api.ProcessorContext;
import net.sf.jaspercode.api.exception.JasperException;
import net.sf.jaspercode.api.resources.ApplicationFile;
import net.sf.jaspercode.api.resources.FileWatcher;

public class HandwrittenCodeFileWatcher implements FileWatcher {

	ProcessorContext ctx = null;
	int priority = 0;
	FileProcessor proc = null;
	String path = null;
	
	public HandwrittenCodeFileWatcher(String path) {
		this.path = path;
	}
	@Override
	public void init(ProcessorContext ctx) {
		this.ctx = ctx;
	}

	@Override
	public int getPriority() {
		return proc.getPriority();
	}

	@Override
	public void fileUpdated(ApplicationFile changedFile) throws JasperException {
		try {
			proc = new FileProcessor(changedFile,ctx);
		} catch(IOException e) {
			throw new JasperException("Couldn't read file '"+changedFile.getPath()+"'", e);
		}
	}

	@Override
	public void processUpdates() throws JasperException {
		proc.process();
	}

	@Override
	public boolean removeOnUnload() {
		return true;
	}
	
}

