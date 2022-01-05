package net.sf.jaspercode.patterns.java.handwritten;

import net.sf.jaspercode.api.ProcessorContext;
import net.sf.jaspercode.api.exception.JasperException;
import net.sf.jaspercode.api.resources.ApplicationFile;
import net.sf.jaspercode.api.resources.FileProcessor;

public class HandwrittenCodeFileProcessor implements FileProcessor {

	ProcessorContext ctx = null;
	int priority = 0;
	FileHandler handler = null;
	String path = null;
	
	String errorMessage = null;
	
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
	public void setFile(ApplicationFile changedFile) {
		try {
			handler = new FileHandler(changedFile,ctx);
		} catch(Exception e) {
			errorMessage = e.getMessage();
		}
	}
	
	@Override
	public String getName() {
		return "HandwrittenCodeFileProcessor["+path+"]";
	}

	@Override
	public void process() throws JasperException {
		if (errorMessage!=null) {
			throw new JasperException("Couldn't read application file - "+errorMessage);
		}
		handler.process();
	}

}

