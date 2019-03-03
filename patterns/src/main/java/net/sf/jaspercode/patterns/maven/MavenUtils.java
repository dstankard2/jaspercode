package net.sf.jaspercode.patterns.maven;

import net.sf.jaspercode.api.JasperException;
import net.sf.jaspercode.api.ProcessorContext;
import net.sf.jaspercode.api.resources.ApplicationFolder;
import net.sf.jaspercode.api.resources.ApplicationResource;
import net.sf.jaspercode.patterns.java.http.JavaWebUtils;

public class MavenUtils {

	public static boolean isMavenBuild(ProcessorContext ctx) {
		return getMavenBuildContext(ctx) != null;
	}
	
	public static MavenBuildContext getMavenBuildContext(ProcessorContext ctx) {
		if (ctx.getBuildContext() instanceof MavenBuildContext) {
			return (MavenBuildContext)ctx.getBuildContext();
		}
		return null;
	}
	
	public static ApplicationResource getDefinitionWebRoot(ProcessorContext ctx,String lang) throws JasperException {
		ApplicationResource ret = null;
		String path = null;
		
		if (lang.equals("java")) {
			path = "src/main/java";
		} else if (JavaWebUtils.isWebLanguage(lang)) {
			path = "src/main/webapp";
		} else {
			throw new JasperException("Couldn't find folder for resources of language '"+lang+"'");
		}

		ret = ctx.getBuildContext().getApplicationResource(path);
		
		if (ret==null) {
			throw new JasperException("Couldn't find resource folder '"+path+"' in application definition");
		}
		if (!(ret instanceof ApplicationFolder)) {
			throw new JasperException("Resource '"+path+"' is not a folder");
		}
		
		return ret;
	}
	
	public static void addMavenPlugin(PluginConfig plugin,ProcessorContext ctx) {
		MavenBuildContext buildCtx = getMavenBuildContext(ctx);
		if (buildCtx==null) return;
		buildCtx.addPlugin(plugin);
	}
	
	public static String getGroupId(String artifact) {
		String parts[] = artifact.split(":");
		return parts[0];
	}
	public static String getArtifactId(String artifact) throws JasperException {
		String parts[] = artifact.split(":");
		if (parts.length<2) throw new JasperException("Couldn't find artifactId in maven artifact identifier '"+artifact+"'");
		return parts[1];
	}
	public static String getVersion(String artifact) {
		String parts[] = artifact.split(":");
		if (parts.length<3) return null;
		return parts[2];
	}

}
