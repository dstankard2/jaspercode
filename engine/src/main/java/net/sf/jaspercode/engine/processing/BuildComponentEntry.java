package net.sf.jaspercode.engine.processing;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.sf.jaspercode.api.ApplicationContext;
import net.sf.jaspercode.api.BuildComponentProcessor;
import net.sf.jaspercode.api.BuildContext;
import net.sf.jaspercode.api.Command;
import net.sf.jaspercode.api.SourceFile;
import net.sf.jaspercode.api.config.BuildComponent;
import net.sf.jaspercode.api.config.Component;
import net.sf.jaspercode.api.exception.JasperException;
import net.sf.jaspercode.api.plugin.ProcessorLogMessage;
import net.sf.jaspercode.engine.BuildComponentPattern;
import net.sf.jaspercode.engine.application.ProcessContainer;
import net.sf.jaspercode.engine.application.ProcessingContext;
import net.sf.jaspercode.engine.definitions.ApplicationFolderImpl;
import net.sf.jaspercode.engine.definitions.ComponentFile;
import net.sf.jaspercode.engine.definitions.DefaultBuildContext;

public class BuildComponentEntry extends ConfigurableProcessable implements Tracked {

	private ProcessingContext processingContext = null;

	private ApplicationFolderImpl folder = null;
	private BuildComponent buildComponent = null;
	private BuildContext buildContext = null;
	private BuildComponentPattern pattern;
	private int id = 0;
	private int originatorId = 0;
	private BuildProcessorContextImpl buildProcessorContext = null;
	private ComponentFile componentFile = null;
	private List<ProcessContainer> deployProcesses = new ArrayList<>();
	private ProcessorLog applicationLog = null;

	Map<String,Object> objects = new HashMap<>();
	List<String> objectDependencies = new ArrayList<>();

	private List<Component> addedComponents = new ArrayList<>();
	private List<SourceFile> sourceFiles = new ArrayList<>();

	private ProcessingState state = null;
	private ProcessorLog log = null;
	
	private BuildComponentProcessor processor = null;
	
	private File outputRoot = null;
	
	public BuildComponentEntry(ComponentFile componentFile,ProcessingContext processingContext, ApplicationContext applicationContext,BuildComponent buildComponent,BuildComponentPattern pattern, int id, int originatorId, File outputRoot, ProcessorLog applicationLog) {
		this.componentFile = componentFile;
		this.processingContext = processingContext;
		if (componentFile!=null) {
			this.folder = componentFile.getFolder();
		}
		this.buildComponent = buildComponent;
		this.pattern = pattern;
		this.id = id;
		this.originatorId = originatorId;
		this.log = new ProcessorLog(getName());
		this.buildProcessorContext = new BuildProcessorContextImpl(folder, this, buildComponent, applicationContext, log);
		this.outputRoot = outputRoot;
		this.applicationLog = applicationLog;
	}

	public BuildComponent getBuildComponent() {
		return buildComponent;
	}

	@Override
	public List<ProcessorLogMessage> getMessages() {
		return log.getMessages(false);
	}

	protected String getProperty(String name) {
		return componentFile.getFolder().getProperties().get(name);
	}

	/*
	protected boolean handleConfigProperty(ConfigProperty property, Class<?>[] params) throws PreprocessingException {
		Object ret = null;
		String configValue = null;

		if (params.length!=1) {
			throw new PreprocessingException("A method annotated with @ConfigProperty must take a single parameter which is either string or integer");
		}
		
		boolean required = property.required();
		String name = property.name();

		configValue = getProperty(name);
		if ((configValue==null) && (required)) {
			throw new RequiredConfigurationException(name);
		}

		if (configValue==null) {
			ret = null;
		}
		if (configValue!=null) {
			if (params[0]==String.class) {
				ret = configValue;
			}
			else if (params[0]==Integer.class) {
				try {
					ret = Integer.parseInt(configValue);
				} catch(NumberFormatException e) {
					throw new PreprocessingException("Configuration '"+name+"' must be an integer");
				}
			}
			else if (params[0]==Boolean.class) {
				ret = Boolean.FALSE;
				if (configValue.equalsIgnoreCase("true")) ret = Boolean.TRUE;
				else if (configValue.equalsIgnoreCase("T")) ret = Boolean.TRUE;
				else if (configValue.equalsIgnoreCase("Y")) ret = Boolean.TRUE;
				else if (configValue.equalsIgnoreCase("false")) ret = Boolean.FALSE;
				else if (configValue.equalsIgnoreCase("F")) ret = Boolean.TRUE;
				else if (configValue.equalsIgnoreCase("N")) ret = Boolean.TRUE;
				else {
					throw new PreprocessingException("Found invalid value '"+configValue+"' for boolean configuration '"+name+"'");
				}
			}
		}

		return ret;
	}
	*/

	// Pattern will be null if the build component is the default build
	public boolean preprocess() {
		boolean ret = false;
		
		this.state = ProcessingState.PREPROCESSING;
		ret = super.populateConfigurations(buildComponent);
		this.state = ProcessingState.PREPROCESSED;

		return ret;
	}
	
	public boolean init() {
		boolean ret = true;

		if (pattern!=null) {
			try {
				processor = pattern.getProcessor(buildComponent);
				processor.initialize(buildComponent, buildProcessorContext);
				this.buildContext = processor.createBuildContext();
			} catch(JasperException e) {
				this.log.error("Exception while initializing build", e);
				ret = false;
			}
		} else {
			this.buildContext = new DefaultBuildContext(this.log, this.buildProcessorContext);
		}
		folder.setBuildComponentEntry(this);
		
		return ret;
	}

	public BuildContext getBuildContext() {
		return buildContext;
	}

	@Override
	public int getPriority() {
		return 0;
	}

	// Pattern will be null if the build component is the default build
	@Override
	public boolean process() {
		this.state = ProcessingState.PROCESSING;
		boolean ret = true;
		if (pattern!=null) {
			try {
				processor.generateBuild();
				this.state = ProcessingState.COMPLETE;
			} catch(JasperException e) {
				ret = false;
				this.state = ProcessingState.ERROR;
				e.printStackTrace();
			}
		}
		
		return ret;
	}

	// Compares priority of components, for sorting
	@Override
	public int compareTo(Processable o) {
		if (o==null) return -1;

		int p = this.getPriority();
		int op = o.getPriority();
		if (p>op) return 1;
		else if (p==op) return 0;
		else return -1;
	}

	@Override
	public ApplicationFolderImpl getFolder() {
		return folder;
	}

	@Override
	public void rollbackChanges() {
	}

	@Override
	public boolean commitChanges() {
		boolean ret = true;

		for(Component comp : addedComponents) {
			try {
				processingContext.addComponent(id, comp, componentFile);
			} catch(JasperException e) {
				if (e.getCause()!=null) {
					log.error(e.getMessage(), e.getCause());
				} else {
					log.error(e.getMessage());
				}
				return false;
			}
		}
		addedComponents.clear();
		
		for(Entry<String,Object> entry : objects.entrySet()) {
			processingContext.setObject(id, entry.getKey(), entry.getValue());
		}
		objects.clear();
		
		for(SourceFile src : sourceFiles) {
			processingContext.saveSourceFile(id, src);
		}
		sourceFiles.clear();

		return ret;
	}

	@Override
	public int getId() {
		return id;
	}

	@Override
	public int getOriginatorId() {
		return originatorId;
	}
	
	@Override
	public ProcessingState getState() {
		return state;
	}

	@Override
	public String getName() {
		return this.buildComponent.getComponentName();
	}
	
	// Methods used by the buildProcessorContext
	
	public void addComponent(Component component) {
		this.addedComponents.add(component);
	}

	public Object getObject(String name) {
		return processingContext.getObject(id, name);
	}
	
	public void setObject(String name,Object object) {
		objects.put(name, object);
	}
	
	public SourceFile getSourceFile(String path) {
		SourceFile ret = processingContext.getSourceFile(path);
		this.sourceFiles.add(ret);
		return ret;
	}
	public void saveSourceFile(SourceFile sourceFile) {
		this.sourceFiles.add(sourceFile);
	}
	
	// Handle engine build commands

	protected void handleSynchronousProcess(Process p) throws IOException,InterruptedException {
		InputStream outputIn = p.getInputStream();
		InputStreamReader reader = new InputStreamReader(outputIn);
		BufferedReader b = new BufferedReader(reader);

		while((p.isAlive()) || (outputIn.available()>0)) {
			if (outputIn.available()>0) {
				String line = b.readLine();
				applicationLog.info(line);
				applicationLog.flushToSystem();
			} else {
				try {
					Thread.sleep(50);
				} catch(Exception e) { }
			}
		}
		
	}
	
	protected Process runCommand(Command command) {
		ApplicationFolderImpl workspaceFolder = this.getFolder();
		String cmd = command.getCommandString();
		String path = workspaceFolder.getPath();
		File workingFolder = new File(outputRoot, path);
		boolean asynch = command.asynch();
		Process ret = null;
		
		if (cmd==null) return null;
		
		log.info("Running command '"+cmd+"' for path '"+getFolder().getPath()+"'");
		
		try {
			if (asynch) {
				//cmd = "cmd.exe /C start cmd.exe /K " + cmd+"";
				//cmd = cmd + " /c start";
			}
			Runtime r = Runtime.getRuntime();
			Process proc = r.exec(cmd, null, workingFolder);
			if (!asynch) {
				handleSynchronousProcess(proc);
			} else {
				//proc.destroyForcibly();
				//handleCommandLine(cmd,workingFolder);
				//handleSynchronousProcess(proc);
				ret = proc;
			}
		} catch(IOException e) {
			e.printStackTrace();
		} catch(InterruptedException e) {
			e.printStackTrace();
		}
		
		return ret;
	}
	
	@Override
	public void finalize() {
		for(ProcessContainer p : deployProcesses) {
			p.terminate();
		}
		deployProcesses.clear();
	}
	
	public void build() {
		//lastBuildMillis = System.currentTimeMillis();
 		List<Command> cmds = processor.build();
		
		for(Command cmd : cmds) {
			runCommand(cmd);
		}
	}
	
	public void deploy() {
		undeploy();
		if (buildContext.getRuntimePlatform()==null) return;

		List<Command> cmds = buildContext.getRuntimePlatform().deploy();

		for(Command cmd : cmds) {
			Process proc = runCommand(cmd);
			if (proc!=null) {
				ProcessContainer cont = ProcessContainer.create(cmd.getCommandString(), proc);
				deployProcesses.add(cont);
			}
		}
	}
	
	public void undeploy() {
		System.err.println("*** Build Component undeploy");
	}
	
	public void clean() {
 		List<Command> cmds = processor.clean();
		
		for(Command cmd : cmds) {
			runCommand(cmd);
		}
	}

}

