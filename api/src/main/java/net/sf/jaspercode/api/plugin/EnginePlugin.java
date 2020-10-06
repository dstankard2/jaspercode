package net.sf.jaspercode.api.plugin;


public interface EnginePlugin {

	void setPluginContext(PluginContext ctx);
	
	String getPluginName();
	
	void engineStart();

	void scanFinish(String applicationName);

}
