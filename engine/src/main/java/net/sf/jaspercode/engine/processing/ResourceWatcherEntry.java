package net.sf.jaspercode.engine.processing;

import net.sf.jaspercode.api.JasperException;
import net.sf.jaspercode.api.resources.ResourceWatcher;
import net.sf.jaspercode.engine.definitions.ComponentFile;
import net.sf.jaspercode.engine.impl.ApplicationContextImpl;
import net.sf.jaspercode.engine.impl.ProcessorContextImpl;

public class ResourceWatcherEntry extends ProcessorContainerBase implements Processable {

	private String path = null;
	ResourceWatcher instance = null;
	ProcessorContextImpl ctx = null;
	boolean first = true;
	
	public ResourceWatcherEntry(ResourceWatcher r, String path, ComponentContainer mgr,ComponentFile originator,ApplicationContextImpl applicationContext) {
		super(originator, mgr, applicationContext);
		this.path = path;
		this.instance = r;
		this.mgr = mgr;
		this.ctx = new ProcessorContextImpl(this,mgr,applicationContext);
		instance.init(ctx);
	}
	
	public int getPriority() {
		return this.instance.getPriority();
	}
	
	public String getPath() {
		return path;
	}
	
	@Override
	public void process() throws JasperException {
		instance.process();
		/*
		if (first) {
			instance.process();
			first = false;
		} else {
			instance.process();
		}
		*/
	}

	@Override
	public String getProcessorName() {
		return "Resource["+path+"]";
	}

}

