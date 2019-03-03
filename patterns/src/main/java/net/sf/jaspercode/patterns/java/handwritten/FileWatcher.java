package net.sf.jaspercode.patterns.java.handwritten;

import java.io.IOException;

import net.sf.jaspercode.api.JasperException;
import net.sf.jaspercode.api.ProcessorContext;
import net.sf.jaspercode.api.resources.ApplicationFile;
import net.sf.jaspercode.api.resources.ResourceWatcher;

public class FileWatcher implements ResourceWatcher {

	ProcessorContext ctx = null;
	int priority = 0;
	String path = null;
	FileProcessor proc = null;
	
	public FileWatcher(String path) {
		this.path = path;
	}

	@Override
	public void init(ProcessorContext ctx) {
		this.ctx = ctx;
		findPriority();
		System.out.println("Priority for '"+path+"' is "+priority);
	}

	private void findPriority() {
		ApplicationFile file = (ApplicationFile)ctx.getResource(path);
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
	
	@Override
	public void process() throws JasperException {
		ctx.setLanguageSupport("Java8");
		// Reload the FileProcessor in case the file still needs to be parsed.
		ApplicationFile file = (ApplicationFile)ctx.getResource(path);
		try {
			proc = new FileProcessor(file,ctx);
			proc.process();
		} catch(IOException e) {
			throw new JasperException("IOException while reading resource '"+path+"'", e);
		}
	}

	public int getPriority() {
		return priority;
	}

}

