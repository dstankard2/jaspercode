package net.sf.jaspercode.api.resources;

import net.sf.jaspercode.api.exception.JasperException;
import net.sf.jaspercode.api.ProcessorContext;

public interface FolderWatcher {

	int getPriority();

	void process(ProcessorContext ctx,ApplicationFile changedFile) throws JasperException;

	String getName();

}

