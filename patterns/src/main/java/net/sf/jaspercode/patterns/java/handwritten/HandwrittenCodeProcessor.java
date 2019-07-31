package net.sf.jaspercode.patterns.java.handwritten;

import net.sf.jaspercode.api.ComponentProcessor;
import net.sf.jaspercode.api.ProcessorContext;
import net.sf.jaspercode.api.annotation.Plugin;
import net.sf.jaspercode.api.annotation.Processor;
import net.sf.jaspercode.api.config.Component;
import net.sf.jaspercode.api.exception.JasperException;
import net.sf.jaspercode.api.resources.ApplicationFile;
import net.sf.jaspercode.api.resources.ApplicationFolder;
import net.sf.jaspercode.api.resources.ApplicationResource;
import net.sf.jaspercode.patterns.xml.java.handwritten.HandwrittenCode;

@Plugin
@Processor(componentClass = HandwrittenCode.class)
public class HandwrittenCodeProcessor implements ComponentProcessor {
	ProcessorContext ctx = null;
	HandwrittenCode comp = null;
	String watchPath = null;

	@Override
	public void init(Component comp, ProcessorContext ctx) {
		HandwrittenCode component = (HandwrittenCode)comp;
		this.comp = component;
		this.ctx = ctx;
	}

	@Override
	public void process() throws JasperException {
		String path = comp.getPath();
		
		ApplicationResource res = ctx.getBuildContext().getApplicationResource(path);
		
		if (res==null) {
			throw new JasperException("Could not find resource '"+path+"'");
		}
		
		if (res instanceof ApplicationFile) {
			throw new JasperException("Handwritten Java code component can only watch a directory");
		} else {
			ApplicationFolder folder = (ApplicationFolder)res;
			watchDirectory(folder);
		}
	}
	
	protected void watchDirectory(ApplicationFolder folder) {
		HandwrittenCodeFolderWatcher folderWatcher = new HandwrittenCodeFolderWatcher();
		ctx.addFolderWatcher(folder.getPath(),folderWatcher);
	}

}

