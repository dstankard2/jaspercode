package net.sf.jaspercode.api;

import net.sf.jaspercode.api.config.Component;

public interface ComponentProcessor {

	void init(Component component,ProcessorContext ctx);
	void process() throws JasperException;
	
}
