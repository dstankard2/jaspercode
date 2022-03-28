package net.sf.jaspercode.engine.processing;

import java.util.List;
import java.util.Map;

import net.sf.jaspercode.api.BuildContext;
import net.sf.jaspercode.api.config.BuildComponent;
import net.sf.jaspercode.api.exception.JasperException;
import net.sf.jaspercode.api.plugin.ProcessorLogMessage;
import net.sf.jaspercode.api.BuildComponentProcessor;
import net.sf.jaspercode.engine.BuildComponentPattern;
import net.sf.jaspercode.engine.JasperResources;
import net.sf.jaspercode.engine.files.ApplicationFolderImpl;
import net.sf.jaspercode.engine.files.ComponentFile;
import net.sf.jaspercode.engine.files.DefaultBuildContext;

public class BuildComponentItem implements Item {

	protected int itemId = 0;
	protected BuildComponent buildComp = null;
	protected BuildComponentPattern pattern = null;
	protected ProcessorLog log = null;
	protected ApplicationFolderImpl folder = null;
	protected ProcessingState state = null;
	protected ProcessableContext ctx = null;
	protected BuildContext buildContext;
	protected BuildComponentProcessor processor = null;
	protected ComponentFile componentFile = null;
	protected JasperResources jasperResources = null;
	protected BuildProcessorContextImpl bctx = null;
	protected Map<String, String> configs;

	public BuildComponentItem(int itemId, BuildComponent buildComp, BuildComponentPattern pattern,
			JasperResources jasperResources, ProcessableContext ctx, ComponentFile componentFile,
			Map<String, String> configs) {
		this.itemId = itemId;
		this.buildComp = buildComp;
		this.pattern = pattern;
		this.folder = componentFile.getFolder();
		this.log = new ProcessorLog(buildComp.getComponentName());
		this.state = ProcessingState.TO_PROCESS;
		this.ctx = ctx;
		this.componentFile = componentFile;
		this.jasperResources = jasperResources;
		this.configs = configs;
	}

	@Override
	public void assignItemId(int itemId) {
		this.itemId = itemId;
	}

	// For creating the default build component item
	public BuildComponentItem(BuildComponent buildComp, ApplicationFolderImpl folder) {
		this.itemId = -1;
		this.buildComp = buildComp;
		this.folder = folder;
		this.log = new ProcessorLog(buildComp.getComponentName());
		this.state = ProcessingState.TO_PROCESS;
		this.configs = folder.getProperties();
	}

	public Map<String,String> getConfigs() {
		return configs;
	}
	
	public int getOriginatorId() {
		return 0;
	}

	public BuildComponent getBuildComponent() {
		return buildComp;
	}

	public ComponentFile getComponentFile() {
		return componentFile;
	}

	public ProcessorLog getLog() {
		return this.log;
	}

	public ApplicationFolderImpl getFolder() {
		return folder;
	}

	public BuildContext getBuildContext() {
		return buildContext;
	}

	public ProcessingState getState() {
		return this.state;
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

	@Override
	public int getItemId() {
		return itemId;
	}

	public ProcessableChanges init() {
		ProcessableChanges ret = new ProcessableChanges(itemId);

		bctx = new BuildProcessorContextImpl(folder, jasperResources, log, configs, ctx);

		if (pattern != null) {
			try {
				boolean configSuccess = ProcessingUtilities.populateConfigurations(buildComp, log, configs);
				if (!configSuccess) {
					ret = null;
					this.log.error("Unable to initialize configurations for build from Jasper Properties");
				} else {
					bctx.setChanges(ret);
					processor = pattern.getProcessor(buildComp);
					processor.setBuildComponent(buildComp);
					processor.setBuildProcessorContext(bctx);
					processor.initialize();
					this.buildContext = processor.createBuildContext();
				}
			} catch (JasperException e) {
				this.log.error("Exception while initializing build", e);
				ret = null;
			}
		} else {
			this.buildContext = new DefaultBuildContext(this.log, bctx);
		}
		if (folder!=null) {
			folder.setBuildComponentItem(this);
		}

		return ret;

	}

	public ProcessableChanges process() {
		ProcessableChanges changes = new ProcessableChanges(itemId);

		this.state = ProcessingState.PROCESSING;
		bctx.setChanges(changes);

		if (pattern != null) {
			try {
				this.processor.generateBuild();
				this.state = ProcessingState.COMPLETE;
			} catch (JasperException e) {
				this.log.error(e.getMessage(), e);
				this.state = ProcessingState.ERROR;
				changes = null;
			}
		}

		return changes;
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
