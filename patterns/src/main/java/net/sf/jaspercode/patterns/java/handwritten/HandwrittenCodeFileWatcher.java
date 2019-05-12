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
		findPriority();
		//System.out.println("Priority for '"+path+"' is "+priority);
	}

	private void findPriority() {
		ApplicationFile file = (ApplicationFile)ctx.getResource(path);

		if (file==null) {
			priority = -1;
		} else {
			try {
				proc = new FileProcessor(file,ctx);
				priority = proc.getPriority();
			} catch(IOException e) {
				proc = null;
				priority = 0;
			} catch(JasperException e) {
				proc = null;
				priority = 0;
			}
		}

	}

	@Override
	public void process(ApplicationFile applicationFile) throws JasperException {
		ctx.setLanguageSupport("Java8");
		try {
			proc = new FileProcessor(applicationFile,ctx);
			proc.process();
		} catch(IOException e) {
			throw new JasperException("IOException while reading resource '"+path+"'", e);
		}
	}

	@Override
	public int getPriority() {
		return priority;
	}
	
}

