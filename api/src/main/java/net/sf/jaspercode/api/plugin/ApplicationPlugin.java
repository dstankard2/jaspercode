package net.sf.jaspercode.api.plugin;

import net.sf.jaspercode.api.exception.JasperException;
import net.sf.jaspercode.api.snapshot.ApplicationSnapshot;

public interface ApplicationPlugin {

	String getPluginName();
	
	void setApplicationName(String applicationName);

	void setPluginContext(PluginContext ctx);

	void scanStart() throws JasperException;
	
	void scanComplete(ApplicationSnapshot snapshot) throws JasperException;

}

