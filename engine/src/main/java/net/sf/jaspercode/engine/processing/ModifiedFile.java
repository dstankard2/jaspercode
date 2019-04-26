package net.sf.jaspercode.engine.processing;

import java.util.ArrayList;
import java.util.List;

import net.sf.jaspercode.api.config.BuildComponent;
import net.sf.jaspercode.api.config.Component;
import net.sf.jaspercode.api.logging.ProcessorLogLevel;
import net.sf.jaspercode.api.plugin.ProcessorLogMessage;
import net.sf.jaspercode.engine.BuildComponentPattern;
import net.sf.jaspercode.engine.ComponentPattern;
import net.sf.jaspercode.engine.application.ApplicationManager;
import net.sf.jaspercode.engine.application.JasperResources;
import net.sf.jaspercode.engine.application.ProcessingContext;
import net.sf.jaspercode.engine.application.ProcessingManager;
import net.sf.jaspercode.engine.definitions.ApplicationFolderImpl;
import net.sf.jaspercode.engine.definitions.ComponentFile;
import net.sf.jaspercode.engine.definitions.UserFile;
import net.sf.jaspercode.engine.definitions.WatchedResource;
import net.sf.jaspercode.engine.exception.PreprocessingException;

public class ModifiedFile implements FileToProcess {

	private WatchedResource newFile = null;
	private WatchedResource oldFile = null;
	private ProcessingManager processingManager = null;
	private ApplicationManager applicationManager = null;
	private JasperResources jasperResources = null;

	public ModifiedFile(WatchedResource newFile, WatchedResource oldFile, ProcessingManager processingManager,
			ApplicationManager applicationManager, JasperResources jasperResources) {
		super();
		this.newFile = newFile;
		this.oldFile = oldFile;
		this.processingManager = processingManager;
		this.applicationManager = applicationManager;
		this.jasperResources = jasperResources;
	}

	@Override
	public List<Processable> preprocess() throws PreprocessingException {
		List<Processable> ret = new ArrayList<>();

		if (oldFile instanceof ComponentFile) {
			ret.add(new UnloadComponentFileEntry((ComponentFile)oldFile, applicationManager, oldFile.getFolder()));
		}

		if (newFile instanceof ComponentFile) {
			ComponentFile componentFile = (ComponentFile)newFile;
			for(Component comp : componentFile.getComponentSet().getComponent()) {
				int id = processingManager.newId();
				if (comp instanceof BuildComponent) {
					BuildComponent buildComp = (BuildComponent)comp;
					BuildComponentPattern pattern = processingManager.getPatterns().getBuildPattern(buildComp.getClass());
					BuildComponentEntry e = new BuildComponentEntry(componentFile, new ProcessingContext(processingManager), jasperResources, buildComp,  pattern, id, 0);
					try {
						e.preprocess();
						e.commitChanges();
						ret.add(e);
					} finally {
						logState(e.getMessages());
					}
				} else {
					ComponentPattern pattern = processingManager.getPatterns().getPattern(comp.getClass());
					if (pattern==null) {
						throw new PreprocessingException("Could not find component pattern for component '"+comp.getComponentName()+"'");
					}
					ComponentEntry e = new ComponentEntry(jasperResources, componentFile, new ProcessingContext(processingManager), comp, pattern, id, 0);
					e.preprocess();
					ret.add(e);
				}
			}
		} else if (newFile instanceof UserFile) {
			// shouldn't happen
		}

		return ret;
	}

	protected void logState(List<ProcessorLogMessage> msgs) {
		//System.err.println("[ERROR] Couldn't add file '"+file.getPath()+"'");
		for(ProcessorLogMessage msg : msgs) {
			String m = String.format("[%s] %s", msg.getLevel().name(), msg.getMessage());
			if (msg.getLevel()==ProcessorLogLevel.ERROR) {
				System.err.println(m);
				if (msg.getThrowable()!=null)
					msg.getThrowable().printStackTrace();
			} else {
				System.out.println(m);
				if (msg.getThrowable()!=null)
					msg.getThrowable().printStackTrace();
			}
		}
	}
	
	@Override
	public ApplicationFolderImpl getFolder() {
		return newFile.getFolder();
	}

}

