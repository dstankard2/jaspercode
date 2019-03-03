package net.sf.jaspercode.engine.processing;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import net.sf.jaspercode.api.BuildComponentProcessor;
import net.sf.jaspercode.api.BuildContext;
import net.sf.jaspercode.api.Command;
import net.sf.jaspercode.api.JasperException;
import net.sf.jaspercode.api.SourceFile;
import net.sf.jaspercode.api.config.BuildComponent;
import net.sf.jaspercode.engine.BuildComponentPattern;
import net.sf.jaspercode.engine.definitions.ApplicationFolderImpl;
import net.sf.jaspercode.engine.definitions.ComponentFile;
import net.sf.jaspercode.engine.impl.ApplicationContextImpl;
import net.sf.jaspercode.engine.impl.BuildProcessorContextImpl;

public class BuildComponentEntry extends ProcessorContainerBase {

	private BuildComponent buildComponent;
	private BuildComponentProcessor processor = null;
	private List<SourceFile> sourceFiles = new ArrayList<>();
	private BuildProcessorContextImpl ctx = null;
	private BuildContext buildContext = null;
	private BuildComponentPattern pattern;
	private List<ProcessContainer> deployProcesses = new ArrayList<>();

	private long lastBuildMillis = 0L;
	private long lastDeployMillis = 0L;
	
	public BuildComponentEntry(ComponentFile componentFile,BuildComponent buildComponent,ComponentContainer mgr,BuildComponentPattern pattern,ApplicationContextImpl applicationContext) {
		super(componentFile,mgr,applicationContext);
		this.buildComponent = buildComponent;
		this.pattern = pattern;
		folder.setBuildComponentEntry(this);
	}

	@Override
	public String getProcessorName() {
		return buildComponent.getComponentName();
	}

	public void initialize() throws JasperException {
		processor = pattern.getProcessor(buildComponent);
		ctx = new BuildProcessorContextImpl(mgr, folder, originator, this);
		processor.initialize(buildComponent, ctx);
		buildContext = processor.createBuildContext();
	}

	@Override
	// A build component is always processed first in the folder
	public int getPriority() {
		return -1;
	}
	
	public void process() throws JasperException {
		processor.generateBuild();
	}
	
	public BuildComponent getBuildComponent() {
		return buildComponent;
	}
	public void setBuildComponent(BuildComponent buildComponent) {
		this.buildComponent = buildComponent;
	}
	public BuildComponentProcessor getProcessor() {
		return processor;
	}
	public void setProcessor(BuildComponentProcessor processor) {
		this.processor = processor;
	}
	public List<SourceFile> getSourceFiles() {
		return sourceFiles;
	}
	public void setSourceFiles(List<SourceFile> sourceFiles) {
		this.sourceFiles = sourceFiles;
	}
	public BuildProcessorContextImpl getCtx() {
		return ctx;
	}
	public void setCtx(BuildProcessorContextImpl ctx) {
		this.ctx = ctx;
	}

	public BuildContext getBuildContext() {
		return buildContext;
	}

	public void setBuildContext(BuildContext buildContext) {
		this.buildContext = buildContext;
	}

	protected void handleSynchronousProcess(Process p) throws IOException,InterruptedException {
		InputStream outputIn = p.getInputStream();
		InputStreamReader reader = new InputStreamReader(outputIn);
		BufferedReader b = new BufferedReader(reader);

		while((p.isAlive()) || (outputIn.available()>0)) {
			if (outputIn.available()>0) {
				String line = b.readLine();
				getLog().info(line);
			} else {
				try {
					Thread.sleep(50);
				} catch(Exception e) { }
			}
		}
		
	}
	
	protected void handleCommandLine(String command,File workingDirectory) {
		
	}
	
	protected Process runCommand(Command command) {
		ApplicationFolderImpl workspaceFolder = this.getFolder();
		String cmd = command.getCommandString();
		String path = workspaceFolder.getPath();
		File outputRoot = mgr.getOutputDirectory();
		File workingFolder = new File(outputRoot, path);
		boolean asynch = command.asynch();
		Process ret = null;
		
		if (cmd==null) return null;
		
		getLog().info("Running command '"+cmd+"' for path '"+getFolder().getPath()+"'");
		
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
	
	
	
	public void checkBuild() {
		long lastModified = this.getFolder().getLastModified();
		if (lastBuildMillis < lastModified) {
			build();
		}
	}
	
	public void checkDeploy() {
		long lastModified = this.getFolder().getLastModified();
		if (lastDeployMillis < lastModified) {
			deploy();
		}
	}

	public void build() {
		lastBuildMillis = System.currentTimeMillis();
 		List<Command> cmds = processor.build();
		
		for(Command cmd : cmds) {
			runCommand(cmd);
		}
	}
	
	public void undeploy() {
		
		for(ProcessContainer p : deployProcesses) {
			p.terminate();
		}
		deployProcesses.clear();

		// Undeploy the runtime platform
		if (buildContext.getRuntimePlatform()==null) return;
		List<Command> cmds = buildContext.getRuntimePlatform().undeploy();
		for(Command cmd : cmds) {
			runCommand(cmd);
		}
	}
	
	@Override
	public void finalize() {
		for(ProcessContainer p : deployProcesses) {
			p.terminate();
		}
		deployProcesses.clear();
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

}
