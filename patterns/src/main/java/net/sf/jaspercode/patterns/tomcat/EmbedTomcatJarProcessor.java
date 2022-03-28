package net.sf.jaspercode.patterns.tomcat;

import net.sf.jaspercode.api.BuildContext;
import net.sf.jaspercode.api.ComponentProcessor;
import net.sf.jaspercode.api.ProcessorContext;
import net.sf.jaspercode.api.annotation.Plugin;
import net.sf.jaspercode.api.annotation.Processor;
import net.sf.jaspercode.api.config.Component;
import net.sf.jaspercode.api.exception.JasperException;
import net.sf.jaspercode.patterns.java.http.JavaWebUtils;
import net.sf.jaspercode.patterns.maven.ExecutionConfig;
import net.sf.jaspercode.patterns.maven.MavenBuildContext;
import net.sf.jaspercode.patterns.maven.MavenUtils;
import net.sf.jaspercode.patterns.maven.PluginConfig;
import net.sf.jaspercode.patterns.xml.tomcat.EmbedTomcatJar;

@Plugin
@Processor(componentClass = EmbedTomcatJar.class)
public class EmbedTomcatJarProcessor implements ComponentProcessor {

	EmbedTomcatJar comp = null;
	ProcessorContext ctx = null;
	
	@Override
	public void init(Component component, ProcessorContext ctx) {
		this.comp = (EmbedTomcatJar)component;
		this.ctx = ctx;
	}

	public void process() throws JasperException {
		BuildContext buildCtx = ctx.getBuildContext();
		String context = comp.getContext();
		String jarName = comp.getJarName();
		Integer port = comp.getPort();
		String pkg = comp.getPkg();
		String jarPath = null;
		//Integer debugPort = comp.getDebugPort();

		ctx.getLog().info("Processing embed tomcat application "+comp.getComponentName());

		ctx.getLog().info("Embed Tomcat is added webapp variable types");
		JavaWebUtils.addServletTypes(ctx);
		EmbedTomcatFinalizer fin = new EmbedTomcatFinalizer(jarName,context,port);
		ctx.addComponent(fin);
		
		ctx.getBuildContext().addDependency("tomcat-embed-core");
		ctx.getBuildContext().addDependency("tomcat-embed-jasper");
		ctx.getBuildContext().addDependency("tomcat-jasper");
		
		// For a Maven Build, add the Maven assembly plugin to build a Jar with dependencies
		if (MavenUtils.isMavenBuild(ctx)) {
			PluginConfig cfg = new PluginConfig("org.apache.maven.plugins:maven-assembly-plugin");
			cfg.getConfiguration().addNestingProperty("archive").addPropertyWithNestedProperties("manifestEntries").addPropertySingleValue("Main-Class", pkg+'.'+"TomcatMain");
			ExecutionConfig exec = new ExecutionConfig();
			exec.setPhase("package");
			cfg.getExecutions().add(exec);
			exec.getConfiguration().addPropertyValueList("descriptorRefs", "descriptorRef").addValue("jar-with-dependencies");
			exec.getGoals().add("single");
			MavenUtils.addMavenPlugin(cfg, ctx);
			
			MavenBuildContext bctx = MavenUtils.getMavenBuildContext(ctx);

			bctx.addBuildPhase("package");
			bctx.setFinalName(jarName);

			cfg = new PluginConfig("org.codehaus.mojo:exec-maven-plugin");
			exec = new ExecutionConfig();
			cfg.getExecutions().add(exec);
			exec.setId("Embed Tomcat");
			exec.setPhase("install");
			exec.getGoals().add("exec");
			exec.getConfiguration().addProperty("executable", "java -jar target\\"+jarName+"-jar-with-dependencies.jar");
			jarPath = "target\\"+jarName+"-jar-with-dependencies.jar";
		}
		EmbedTomcatRuntimePlatform platform = new EmbedTomcatRuntimePlatform(jarPath, comp.getDebugPort());
		buildCtx.setRuntimePlatform(platform);
		platform.setContextRoot(context);
	}
	
}
