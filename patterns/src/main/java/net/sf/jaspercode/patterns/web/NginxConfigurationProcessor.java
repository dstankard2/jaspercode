package net.sf.jaspercode.patterns.web;

import net.sf.jaspercode.api.ComponentProcessor;
import net.sf.jaspercode.api.JasperException;
import net.sf.jaspercode.api.ProcessorContext;
import net.sf.jaspercode.api.annotation.Plugin;
import net.sf.jaspercode.api.annotation.Processor;
import net.sf.jaspercode.api.config.Component;
import net.sf.jaspercode.patterns.xml.web.NginxConfiguration;

@Plugin
@Processor(componentClass = NginxConfiguration.class)
public class NginxConfigurationProcessor implements ComponentProcessor {

	ProcessorContext ctx = null;
	NginxConfiguration comp = null;
	
	@Override
	public void init(Component component, ProcessorContext ctx) {
		this.ctx = ctx;
		this.comp = (NginxConfiguration)component;
	}

	@Override
	public void process() throws JasperException {
		NginxRuntimePlatform platform = new NginxRuntimePlatform(comp.getNginxExecutable(), comp.getConfigFile());
		ctx.getBuildContext().setRuntimePlatform(platform);
		ctx.getLog().info("Note: NGINX must already be running on this machine as Runtime Platform deployment will perform 'nginx -t'");
	}

}

