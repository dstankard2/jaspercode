package net.sf.jaspercode.engine.processing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.sf.jaspercode.api.BuildComponentProcessor;
import net.sf.jaspercode.api.BuildContext;
import net.sf.jaspercode.api.SourceFile;
import net.sf.jaspercode.api.config.BuildComponent;
import net.sf.jaspercode.api.config.Component;
import net.sf.jaspercode.api.exception.JasperException;
import net.sf.jaspercode.api.plugin.ProcessorLogMessage;
import net.sf.jaspercode.engine.BuildComponentPattern;
import net.sf.jaspercode.engine.application.JasperResources;
import net.sf.jaspercode.engine.application.ProcessingContext;
import net.sf.jaspercode.engine.definitions.ApplicationFolderImpl;
import net.sf.jaspercode.engine.definitions.ComponentFile;
import net.sf.jaspercode.engine.definitions.DefaultBuildContext;

public class BuildComponentItem implements Item {

	protected int id = 0;
	protected BuildComponent buildComp = null;
	protected BuildComponentPattern pattern = null;
	protected ProcessorLog log = null;
	protected ApplicationFolderImpl folder = null;
	protected Map<String,String> configs = null;
	protected ProcessingState state = null;
	protected BuildProcessorContextImpl buildProcessorContext = null;
	protected ProcessingContext ctx = null;
	protected BuildContext buildContext;
	protected BuildComponentProcessor processor = null;
	protected ComponentFile componentFile = null;

	// Data modified
	protected List<Component> addedComponents = new ArrayList<>();
	protected List<SourceFile> sourceFiles = new ArrayList<>();
	protected Map<String,Object> objects = new HashMap<>();

	public BuildComponentItem(int id,BuildComponent buildComp,BuildComponentPattern pattern,
			Map<String,String> configs, JasperResources jasperResources, ProcessingContext ctx,
			ComponentFile componentFile) {
		this.id = id;
		this.buildComp = buildComp;
		this.pattern = pattern;
		this.folder = componentFile.getFolder();
		this.configs = configs;
		this.log = new ProcessorLog(buildComp.getComponentName());
		this.state = ProcessingState.TO_PROCESS;
		this.buildProcessorContext = new BuildProcessorContextImpl(folder, this, buildComp, jasperResources, log, configs);
		this.ctx = ctx;
		this.componentFile = componentFile;
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
	public int getId() {
		return id;
	}

	// A build never has an originator
	@Override
	public int getOriginatorId() {
		return 0;
	}

	public boolean init() {
			boolean ret = true;

			if (pattern!=null) {
				try {
					ProcessingUtilities.populateConfigurations(buildComp, log, configs);
					processor = pattern.getProcessor(buildComp);
					processor.initialize(buildComp, buildProcessorContext);
					this.buildContext = processor.createBuildContext();
				} catch(JasperException e) {
					this.log.error("Exception while initializing build", e);
					ret = false;
				}
			} else {
				this.buildContext = new DefaultBuildContext(this.log, this.buildProcessorContext);
			}
			folder.setBuildComponentItem(this);
			
			return ret;
		
	}

	public boolean process() {
		boolean ret = true;
		
		this.state = ProcessingState.PROCESSING;
		
		if (pattern!=null) {
			try {
				this.processor.generateBuild();
				this.state = ProcessingState.COMPLETE;
			} catch(JasperException e) {
				this.log.error(e.getMessage(), e);
				this.state = ProcessingState.ERROR;
				ret = false;
			}
		}

		return ret;
	}

	public boolean commitChanges() {
		boolean ret = true;
		
		for(Component comp : this.addedComponents) {
			try {
				this.ctx.addComponent(id, comp, componentFile);
			} catch(JasperException e) {
				this.state = ProcessingState.ERROR;
				ret = false;
			}
		}
		addedComponents.clear();

		for(SourceFile src : this.sourceFiles) {
			ctx.saveSourceFile(id, src);
		}
		this.sourceFiles.clear();
		
		for(Entry<String,Object> entry : this.objects.entrySet()) {
			ctx.setObject(id, entry.getKey(), entry.getValue());
		}
		this.objects.clear();
		
		return ret;
	}

	public boolean rollbackChanges() {
		boolean ret = true;
		
		return ret;
	}

	// API Required for the build processor context
	
	public SourceFile getSourceFile(String path) {
		SourceFile ret = ctx.getSourceFile(path);
		this.sourceFiles.add(ret);
		return ret;
	}

	public void saveSourceFile(SourceFile sourceFile) {
		this.sourceFiles.add(sourceFile);
	}

	public void addComponent(Component component) {
		this.addedComponents.add(component);
	}

	public Object getObject(String name) {
		Object ob = ctx.getObject(id, name);
		setObject(name,ob);
		return ob;
	}
	
	public void setObject(String name,Object object) {
		objects.put(name, object);
	}
	
	// End of build processor context requirements

	// API required by BuildManager
	
	public void clean() {
		// TODO: Implement
		log.error("BuildComponentItem.clean() not implemented");
	}
	
	public void build() {
		//  TODO: Implement
		log.error("BuildComponentItem.build() not implemented");
	}
	
	public void deploy() {
		// TODO: Implement
		log.error("BuildComponentItem.deploy() not implemented");
	}
	
	public void undeploy() {
		// TODO: Implement
		log.error("BuildComponentItem.undeploy() not implemented");
	}

	// End of API required by BuildManager
}

