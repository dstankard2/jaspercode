package net.sf.jaspercode.patterns.web;

import net.sf.jaspercode.api.ComponentProcessor;
import net.sf.jaspercode.api.JasperException;
import net.sf.jaspercode.api.ProcessorContext;
import net.sf.jaspercode.api.annotation.Plugin;
import net.sf.jaspercode.api.annotation.Processor;
import net.sf.jaspercode.api.config.Component;
import net.sf.jaspercode.patterns.maven.ExecutionConfig;
import net.sf.jaspercode.patterns.maven.MavenBuildContext;
import net.sf.jaspercode.patterns.maven.MavenUtils;
import net.sf.jaspercode.patterns.maven.PluginConfig;
import net.sf.jaspercode.patterns.xml.web.SassFiles;

@Plugin
@Processor(componentClass = SassFiles.class)
public class SassFilesProcessor implements ComponentProcessor {

	private SassFiles comp = null;
	private ProcessorContext ctx = null;
	
	@Override
	public void init(Component component, ProcessorContext ctx) {
		this.comp = (SassFiles)component;
		this.ctx = ctx;
	}

	@Override
	public void process() throws JasperException {
		String src = comp.getSourcePath();
		String dest = comp.getOutputPath();
		
		if (src.trim().length()==0) {
			throw new JasperException("No sourcePath specified for sassFiles component");
		}
		if (dest.trim().length()==0) {
			throw new JasperException("No outputPath specified for sassFiles component");
		}
		
		if (MavenUtils.isMavenBuild(ctx)) {
			handleMaven(src,dest,ctx);
		} else {
			ctx.getLog().warn("SassFiles component not generating any files");
		}
	}
	
	protected void handleMaven(String src,String dest,ProcessorContext ctx) {
		MavenBuildContext bctx = MavenUtils.getMavenBuildContext(ctx);
		PluginConfig cfg = new PluginConfig("org.jasig.maven:sass-maven-plugin:1.1.1");
		ExecutionConfig execConfig = new ExecutionConfig();
		String srcPath = ctx.getResource(".").getPath();
		String buildPath = bctx.getApplicationFolderPath();

		cfg.getExecutions().add(execConfig);
		srcPath = srcPath.substring(buildPath.length()) + src;
		execConfig.setId("sassProcessSource");
		execConfig.setPhase("generate-sources");
		execConfig.getGoals().add("update-stylesheets");
		ctx.getLog().warn("Defaulting to sass-maven-plugin version 1.1.1");
		
		execConfig.getConfiguration().addProperty("sassSourceDirectory", srcPath);
		//execConfig.getConfiguration().addProperty("buildDirectory", "${basedir}/"+dest);
		//execConfig.getConfiguration().addProperty("baseOutputDirectory", "${baseDir}/"+dest);
		execConfig.getConfiguration().addProperty("destination", dest);

		bctx.addPlugin(cfg);
	}

}
