package net.sf.jaspercode.plugins.monitor;

import net.sf.jaspercode.api.annotation.Plugin;
import net.sf.jaspercode.api.plugin.EnginePlugin;
import net.sf.jaspercode.api.plugin.PluginContext;

@Plugin
public class TestPlugin implements EnginePlugin {

	PluginContext ctx = null;
	
	@Override
	public String getPluginName() {
		return "test";
	}

	@Override
	public void setPluginContext(PluginContext ctx) {
		this.ctx = ctx;
	}

	@Override
	public void engineStart() {
		ctx.getLog().info("TestPlugin started");
	}

	public void scanStart(String applicationName) {
		ctx.getLog().info("Start scan "+applicationName);
	}

	@Override
	public void scanFinish(String applicationName) {
		ctx.getLog().info("Finish scan "+applicationName);
	}

}

