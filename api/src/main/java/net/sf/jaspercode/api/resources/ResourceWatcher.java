package net.sf.jaspercode.api.resources;

import net.sf.jaspercode.api.ProcessorContext;
import net.sf.jaspercode.api.exception.JasperException;

public interface ResourceWatcher {

	// First method called in lifecycle
	void init(ProcessorContext ctx);

	void process() throws JasperException;

	// Called after init(), to determine processing order
	int getPriority();
	
	String getPath();
	
}
