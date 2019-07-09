package net.sf.jaspercode.api.resources;

import net.sf.jaspercode.api.ProcessorContext;
import net.sf.jaspercode.api.exception.JasperException;

public interface FolderWatcher {

	// First method called in lifecycle
	void init(ProcessorContext ctx);

	int getPriority();

	void process(ApplicationFile changedFile) throws JasperException;

}
