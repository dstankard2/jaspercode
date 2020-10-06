package net.sf.jaspercode.patterns.maven;

import java.util.Arrays;
import java.util.List;

import net.sf.jaspercode.api.BuildContext;
import net.sf.jaspercode.api.BuildProcessorContext;
import net.sf.jaspercode.api.RuntimePlatform;
import net.sf.jaspercode.api.exception.JasperException;
import net.sf.jaspercode.api.resources.ApplicationResource;

public class MavenBuildContext implements BuildContext {

	private static final List<String> webappFileExtensions = Arrays.asList("js","html","htm");

	private BuildProcessorContext ctx = null;
	private MavenBuildComponentProcessor proc = null;
	private RuntimePlatform runtimePlatform = null;

	public MavenBuildContext(BuildProcessorContext ctx,MavenBuildComponentProcessor proc) {
		this.ctx = ctx;
		this.proc = proc;
	}

	public PluginConfig getPlugin(String artifact) {
		return proc.getPlugin(artifact);
	}

	public void addPlugin(PluginConfig cfg) {
		proc.addPlugin(cfg);
	}

	public String getArtifact() {
		return proc.getArtifact();
	}

	public String getName() {
		return getArtifact();
	}

	@Override
	public void addDependency(BuildContext buildCtx) {
		if (buildCtx==null) return;
		if (!(buildCtx instanceof MavenBuildContext)) {
			ctx.getLog().warn("A Maven build context may only depend on another Maven build context");
		} else {
			MavenBuildContext mavenCtx = (MavenBuildContext)buildCtx;
			if (mavenCtx==this) return;
			String dep = mavenCtx.getArtifact();
			proc.addDependency(dep);
		}
	}

	@Override
	public String getApplicationFolderPath() {
		return ctx.getFolder().getPath();
	}

	@Override
	public ApplicationResource getApplicationResource(String relativePath) {
		return ctx.getFolder().getResource(relativePath);
	}

	@Override
	public void addDependency(String name) {
		proc.addDependency(name);
	}

	public String getOutputRootPath() {
		return getOutputRootPath(null);
	}
	
	@Override
	public String getOutputRootPath(String fileExt) {
		String path = ctx.getFolder().getPath();
		
		if (fileExt!=null) {
			if (fileExt.equals("java")) {
				path = path + "src/main/java";
			} else if (webappFileExtensions.contains(fileExt)) {
				path = path + "src/main/webapp";
			}
		}

		return path;
	}
	
	public String getPackaging() {
		return proc.getPackaging();
	}

	public void addBuildPhase(String goal) {
		proc.addBuildPhase(goal);
	}
	
	@Override
	public RuntimePlatform getRuntimePlatform() {
		return runtimePlatform;
	}

	@Override
	public void setRuntimePlatform(RuntimePlatform platform) throws JasperException {
		this.runtimePlatform = platform;
	}

	@Override
	public void addBuildCommand(String cmd) {
		proc.addBuildCommand(cmd);
	}

	public void setFinalName(String finalName) {
		proc.setFinalName(finalName);
	}

}

