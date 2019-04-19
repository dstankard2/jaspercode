package net.sf.jaspercode.engine.definitions;

import net.sf.jaspercode.api.BuildContext;
import net.sf.jaspercode.api.BuildProcessorContext;
import net.sf.jaspercode.api.RuntimePlatform;
import net.sf.jaspercode.api.exception.JasperException;
import net.sf.jaspercode.api.resources.ApplicationResource;
import net.sf.jaspercode.engine.processing.ProcessorLog;

/**
 * A build context that will be used when there is no build component.
 * It is always in the root folder
 */
public class DefaultBuildContext implements BuildContext {

	private ProcessorLog log = null;
	private BuildProcessorContext ctx = null;
	private RuntimePlatform platform = null;
	
	public DefaultBuildContext(ProcessorLog log, BuildProcessorContext ctx) {
		this.log = log;
		this.ctx = ctx;
	}

	@Override
	public void addDependency(String name) {
		log.info("Dependency '"+name+"' is added to the build context");
	}

	@Override
	public void addDependency(BuildContext buildCtx) {
		
	}

	@Override
	public String getOutputRootPath(String fileExt) throws JasperException {
		String cfgName = "outputPath."+fileExt;
		String value = this.ctx.getProperty(cfgName);
		if (value==null) {
			//this.log.error("Default Build Context requires configuration property '"+cfgName+"' to determine output path for file extension '"+fileExt+"'");
			throw new JasperException("Default Build Context requires configuration property '"+cfgName+"' to determine output path for file extension '"+fileExt+"'");
		}
		// TODO Auto-generated method stub
		return value;
	}

	@Override
	public String getOutputRootPath() {
		return null;
	}

	@Override
	public String getApplicationFolderPath() {
		return ctx.getFolder().getPath();
	}

	@Override
	public ApplicationResource getApplicationResource(String path) {
		return ctx.getFolder().getResource(path);
	}

	@Override
	public RuntimePlatform getRuntimePlatform() {
		return platform;
	}

	@Override
	public void setRuntimePlatform(RuntimePlatform platform) throws JasperException {
		log.warn("Setting runtime platform for the default build context is a no-op");
		this.platform = platform;
	}

	@Override
	public String getName() {
		return "Default";
	}

	@Override
	public void addBuildCommand(String cmd) {
		log.error("Default build context cannot add build command '"+cmd+"'");
	}

	
	
}
