package net.sf.jaspercode.eng.processing;

import java.util.List;
import java.util.Map;

import net.sf.jaspercode.api.BuildComponentProcessor;
import net.sf.jaspercode.api.BuildContext;
import net.sf.jaspercode.api.config.BuildComponent;
import net.sf.jaspercode.api.exception.JasperException;
import net.sf.jaspercode.api.plugin.ProcessorLogMessage;
import net.sf.jaspercode.eng.BuildComponentPattern;
import net.sf.jaspercode.eng.JasperResources;
import net.sf.jaspercode.eng.exception.EngineException;
import net.sf.jaspercode.eng.files.ApplicationFolderImpl;
import net.sf.jaspercode.eng.files.DefaultBuildContext;

public class BuildComponentItem {

	protected BuildComponent buildComp = null;
	protected BuildComponentPattern pattern = null;
	protected ProcessorLog log = null;
	protected ProcessableContext ctx = null;
	protected BuildContext buildContext;
	protected BuildComponentProcessor processor = null;
	protected JasperResources jasperResources = null;
	protected BuildProcessorContextImpl bctx = null;
	protected Map<String,String> configs;
	protected ApplicationFolderImpl folder;

	public BuildComponentItem(BuildComponent buildComp, BuildComponentPattern pattern,
			JasperResources jasperResources, ProcessableContext ctx, Map<String,String> configs, 
			ApplicationFolderImpl folder) {
		this.buildComp = buildComp;
		this.pattern = pattern;
		this.log = new ProcessorLog(buildComp.getComponentName());
		this.ctx = ctx;
		this.jasperResources = jasperResources;
		this.configs = configs;
		this.folder = folder;
	}

	public BuildComponent getBuildComponent() {
		return buildComp;
	}
	
	public ProcessorLog getLog() {
		return this.log;
	}

	public BuildContext getBuildContext() {
		return buildContext;
	}

	public List<ProcessorLogMessage> getMessages() {
		return log.getMessages(false);
	}

	public void clearLogMessages() {
		this.log.getMessages(true);
	}

	public String getName() {
		return buildComp.getComponentName();
	}

	public void init() throws EngineException {

			if (pattern!=null) {
				try {
					bctx = new BuildProcessorContextImpl(folder, jasperResources, log, configs, ctx);
					ProcessingUtilities.populateConfigurations(buildComp, log, configs);
					processor = pattern.getProcessor(buildComp);
					processor.setBuildComponent(buildComp);
					processor.setBuildProcessorContext(bctx);
					processor.initialize();
					this.buildContext = processor.createBuildContext();
				} catch(JasperException e) {
					this.log.error("Exception while initializing build", e);
					throw new EngineException();
				}
			} else {
				this.buildContext = new DefaultBuildContext(this.log, bctx);
			}
			folder.setBuildComponentItem(this);
	}

	public void process() throws EngineException {
		
		if (pattern!=null) {
			try {
				this.processor.generateBuild();
			} catch(JasperException e) {
				this.log.error(e.getMessage(), e);
				throw new EngineException();
			}
		}
	}

	// API required by BuildManager
	
	public void clean() {
		log.error("BuildComponentItem.clean() not implemented");
	}
	
	public void compile() {
		log.error("BuildComponentItem.compile() not implemented");
	}
	
	public void build() {
		log.error("BuildComponentItem.build() not implemented");
	}
	
	public void deploy() {
		log.error("BuildComponentItem.deploy() not implemented");
	}
	
	public void undeploy() {
		log.error("BuildComponentItem.undeploy() not implemented");
	}

	// End of API required by BuildManager
}

