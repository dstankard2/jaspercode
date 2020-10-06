package net.sf.jaspercode.patterns.tomcat;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jboss.forge.roaster.model.source.JavaClassSource;

import net.sf.jaspercode.api.ComponentProcessor;
import net.sf.jaspercode.api.ProcessorContext;
import net.sf.jaspercode.api.annotation.Plugin;
import net.sf.jaspercode.api.annotation.Processor;
import net.sf.jaspercode.api.config.Component;
import net.sf.jaspercode.api.exception.JasperException;
import net.sf.jaspercode.langsupport.java.JavaClassSourceFile;

@Plugin
@Processor(componentClass = EmbedTomcatFinalizer.class)
public class EmbedTomcatMain implements ComponentProcessor {

	EmbedTomcatFinalizer component = null;
	ProcessorContext ctx = null;
	
	@Override
	public void init(Component component, ProcessorContext ctx) {
		this.component = (EmbedTomcatFinalizer)component;
		this.ctx = ctx;
	}

	public void process() throws JasperException {
		JavaClassSourceFile src = new JavaClassSourceFile(ctx);
		JavaClassSource j = src.getSrc();
		String webappDirLocation = "src/main/webapp";
		String className = "TomcatMain";
		int port = component.getPort();
		String context = component.getContextRoot();
		EmbedTomcatRuntimePlatform platform = (EmbedTomcatRuntimePlatform)ctx.getBuildContext().getRuntimePlatform();
		String pkg = component.getPkg();

		platform.getDependencies().stream().forEach(dep -> {
			ctx.getBuildContext().addDependency(dep);
		});
		
		ctx.getLog().warn("Assembling Tomcat Main File");

		j.setName(className);
		j.setPackage(pkg);
		StringBuilder body = new StringBuilder();
		j.addImport("org.apache.catalina.startup.Tomcat");
		j.addImport("org.apache.catalina.WebResourceRoot");
		j.addImport("org.apache.catalina.core.StandardContext");
		j.addImport("org.apache.catalina.webresources.StandardRoot");
		j.addImport("java.io.File");

		body.append("try {\n");
		body.append("Tomcat tomcat = new Tomcat();\n");
		body.append("tomcat.setPort("+port+");\ntomcat.enableNaming();\n");
		body.append("StandardContext ctx = (StandardContext) tomcat.addWebapp(\""+context+"\", new File(\""+webappDirLocation+"\").getAbsolutePath());\n");
		body.append("WebResourceRoot resources = new StandardRoot(ctx);\n");
		body.append("ctx.setResources(resources);\n");

		List<String> servletNames = platform.getServletNames();
		for(String name : servletNames) {
			String servletClassName = platform.getServletClass(name);
			body.append("tomcat.addServlet(ctx, \""+name+"\",new "+servletClassName+"());\n");
			List<String> uris = platform.getMappings(name);
			for(String uri : uris) {
				body.append("ctx.addServletMappingDecoded(\""+uri+"\",\""+name+"\");\n");
			}
		}
		Map<String,String> filters = platform.getFilters();
		if (filters.size()>0) {
			j.addImport("org.apache.tomcat.util.descriptor.web.FilterDef");
			for(Entry<String,String> entry : filters.entrySet()) {
				String name = entry.getKey();
				String cl = entry.getValue();
				String var = "filterDef_"+name;
				body.append("FilterDef "+var+" = new FilterDef();\n");
				body.append(var+".setFilterName(\""+name+"\");\n");
				body.append(var+".setFilterClass(\""+cl+"\");\n");
				body.append("ctx.addFilterDef("+var+");\n");
			}
		}
		Map<String,List<String>> filterMappings = platform.getFilterMappings();
		if (filterMappings.size()>0) {
			j.addImport("org.apache.tomcat.util.descriptor.web.FilterMap");
			body.append("FilterMap map = null;\n");
			for(Entry<String,List<String>> entry : filterMappings.entrySet()) {
				String uri = entry.getKey();
				List<String> filtersForUri = entry.getValue();
				for(String filter : filtersForUri) {
					body.append("map = new FilterMap();\n");
					body.append("map.setFilterName(\""+filter+"\");\n");
					body.append("map.addURLPattern(\""+uri+"\");\n");
					body.append("ctx.addFilterMap(map);\n");
				}
				
			}
		}
		
		List<DataSourceInfo> dataSources = platform.getDataSources();
		if (dataSources.size()>0) {
			j.addImport("org.apache.tomcat.util.descriptor.web.ContextResource");
			body.append("ContextResource res = new ContextResource();\n");
			for(DataSourceInfo info : dataSources) {
				body.append("res.setName(\""+info.getName()+"\");\n");
				body.append("res.setAuth(\"Container\");\n");
				body.append("res.setType(\"javax.sql.DataSource\");\n");
				body.append("res.setScope(\"Sharable\");\n");
				body.append("res.setProperty(\"driverClassName\", \""+info.getDriverClass()+"\");\n");
				body.append("res.setProperty(\"url\", \""+info.getUrl()+"\");");
				body.append("res.setProperty(\"username\", \""+info.getUsername()+"\");");
				body.append("res.setProperty(\"password\", \""+info.getPassword()+"\");\n");

				body.append("ctx.getNamingResources().addResource(res);\n");
			}
		}
		
		List<String> servletContextListeners = platform.getServletContextListeners();
		for(String l : servletContextListeners) {
			body.append("ctx.addApplicationListener(\""+l+"\");\n");
		}
		
		Map<String,String> websocketEndpoints = platform.getWebsocketEndpoints();

		if (websocketEndpoints.size()>0) {
			j.addImport("org.apache.tomcat.websocket.server.WsSci");
			body.append("ctx.addServletContainerInitializer(new WsSci(), null);\n");
		}
		body.append("tomcat.start();\ntomcat.getConnector().start();\n");
		
		if (websocketEndpoints.size()>0) {
			j.addImport("javax.websocket.server.ServerContainer");
			j.addImport("javax.websocket.server.ServerEndpointConfig");
			body.append("ServerContainer serverContainer = (ServerContainer) ctx.getServletContext().getAttribute(" + 
					"\"javax.websocket.server.ServerContainer\");\n");
			body.append("ServerEndpointConfig cfg = null;\n");
			for(Entry<String,String> entry : websocketEndpoints.entrySet()) {
				String url = entry.getKey();
				String cl = entry.getValue();
				body.append("cfg = ServerEndpointConfig.Builder.create("+cl+".class, \""+url+"\").build();\n");
				body.append("serverContainer.addEndpoint(cfg);");
			}
		}
		
		body.append("tomcat.getServer().await();\n");
		
		body.append("} catch(Exception e) {\n");
		body.append("e.printStackTrace();\n}\n");
		
		j.addMethod().setPublic().setName("main").setStatic(true).setBody(body.toString()).addParameter("String[]", "args");
		
		ctx.addSourceFile(src);
	}

}
