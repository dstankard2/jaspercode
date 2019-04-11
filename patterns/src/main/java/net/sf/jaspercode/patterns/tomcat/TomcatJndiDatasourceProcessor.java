package net.sf.jaspercode.patterns.tomcat;

import net.sf.jaspercode.api.ComponentProcessor;
import net.sf.jaspercode.api.ProcessorContext;
import net.sf.jaspercode.api.annotation.Plugin;
import net.sf.jaspercode.api.annotation.Processor;
import net.sf.jaspercode.api.config.Component;
import net.sf.jaspercode.api.exception.JasperException;
import net.sf.jaspercode.patterns.java.http.JavaWebUtils;
import net.sf.jaspercode.patterns.web.JavaWebappRuntimePlatform;
import net.sf.jaspercode.patterns.xml.tomcat.TomcatJndiDatasource;

@Plugin
@Processor(componentClass = TomcatJndiDatasource.class)
public class TomcatJndiDatasourceProcessor implements ComponentProcessor {

	private ProcessorContext ctx = null;
	private TomcatJndiDatasource comp = null;

	@Override
	public void init(Component component, ProcessorContext ctx) {
		this.ctx = ctx;
		this.comp = (TomcatJndiDatasource)component;
	}

	@Override
	public void process() throws JasperException {
		DataSourceInfo info = new DataSourceInfo();
		JavaWebappRuntimePlatform platform = JavaWebUtils.getWebPlatform(ctx);
		if (platform==null) {
			throw new JasperException("Cannot create a Tomcat JNDI Datasource unless the current runtime platform is Embed Tomcat");
		}
		
		if (!(platform instanceof EmbedTomcatRuntimePlatform)) {
			throw new JasperException("Cannot create a Tomcat JNDI Datasource unless the current runtime platform is Embed Tomcat");
		}
		
		EmbedTomcatRuntimePlatform tomcat = (EmbedTomcatRuntimePlatform)platform;
		info.setDriverClass(comp.getDriverClass());
		info.setName(comp.getName());
		info.setPassword(comp.getPassword());
		info.setUsername(comp.getUsername());
		info.setUrl(comp.getUrl());
		tomcat.getDataSources().add(info);
		ctx.getBuildContext().addDependency("tomcat-dbcp");
	}

}
