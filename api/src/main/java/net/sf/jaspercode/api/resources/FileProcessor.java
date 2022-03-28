package net.sf.jaspercode.api.resources;

import net.sf.jaspercode.api.exception.JasperException;
import net.sf.jaspercode.api.ProcessorContext;

public interface FileProcessor {

	int getPriority();
	void init(ProcessorContext ctx);
	void setFile(ApplicationFile applicationFile);
	void process() throws JasperException;
	String getName();

}

