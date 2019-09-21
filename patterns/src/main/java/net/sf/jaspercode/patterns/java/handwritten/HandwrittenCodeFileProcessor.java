package net.sf.jaspercode.patterns.java.handwritten;

import java.io.IOException;

import net.sf.jaspercode.api.ProcessorContext;
import net.sf.jaspercode.api.exception.JasperException;
import net.sf.jaspercode.api.resources.ApplicationFile;
import net.sf.jaspercode.api.resources.FileProcessor;

public class HandwrittenCodeFileProcessor implements FileProcessor {

	ProcessorContext ctx = null;
	int priority = 0;
	FileHandler handler = null;
	String path = null;
	
	public HandwrittenCodeFileProcessor(String path) {
		this.path = path;
	}

	@Override
	public void init(ProcessorContext ctx) {
		this.ctx = ctx;
	}

	@Override
	public int getPriority() {
		return handler.getPriority();
	}

	@Override
	public void setFile(ApplicationFile changedFile) throws JasperException {
		try {
			handler = new FileHandler(changedFile,ctx);
		} catch(IOException e) {
			throw new JasperException("Couldn't read file '"+changedFile.getPath()+"'", e);
		}
	}
	
	@Override
	public String getName() {
		return "HandwrittenCodeFileProcessor["+path+"]";
	}

	@Override
	public void process() throws JasperException {
		handler.process();
	}

}

