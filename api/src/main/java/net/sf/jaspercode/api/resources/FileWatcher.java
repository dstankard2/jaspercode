package net.sf.jaspercode.api.resources;

import net.sf.jaspercode.api.ProcessorContext;
import net.sf.jaspercode.api.exception.JasperException;

public interface FileWatcher extends ResourceWatcher {

	// First method called in lifecycle
	void init(ProcessorContext ctx);

	int getPriority();

	void process(ApplicationFile applicationFile) throws JasperException;

}
