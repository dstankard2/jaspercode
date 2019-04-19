package net.sf.jaspercode.engine.processing;

import java.util.ArrayList;
import java.util.List;

import net.sf.jaspercode.api.ApplicationContext;
import net.sf.jaspercode.api.config.BuildComponent;
import net.sf.jaspercode.api.config.Component;
import net.sf.jaspercode.api.logging.ProcessorLogLevel;
import net.sf.jaspercode.api.plugin.ProcessorLogMessage;
import net.sf.jaspercode.engine.BuildComponentPattern;
import net.sf.jaspercode.engine.ComponentPattern;
import net.sf.jaspercode.engine.application.ProcessingContext;
import net.sf.jaspercode.engine.application.ProcessingManager;
import net.sf.jaspercode.engine.definitions.ApplicationFolderImpl;
import net.sf.jaspercode.engine.definitions.ComponentFile;
import net.sf.jaspercode.engine.definitions.WatchedResource;
import net.sf.jaspercode.engine.exception.PreprocessingException;

public class AddedFile implements FileToProcess {

	private WatchedResource file = null;
	private ProcessingManager processingManager = null;
	private ApplicationContext applicationContext = null;

	public AddedFile(WatchedResource file, ProcessingManager processingManager, ApplicationContext applicationContext) {
		super();
		this.file = file;
		this.processingManager = processingManager;
		this.applicationContext = applicationContext;
	}

	@Override
	public List<Processable> preprocess() throws PreprocessingException {
		List<Processable> ret = new ArrayList<>();
		
		/* userFiles are no-op
		// The file is a user file
		if (file instanceof UserFile) {
			List<ResourceWatcherRecord> watchers = processingManager.getResourceWatcherRecords();
			for(ResourceWatcherRecord w : watchers) {
				if (w.getPath().startsWith(file.getPath())) {
					ResourceWatcherEntry entry = w.entry();
					entry.preprocess();
					ret.add(entry);
				}
			}
		}
		*/
		
		// the file is a component file.  Add new components
		if (file instanceof ComponentFile) {
			ComponentFile componentFile = (ComponentFile)file;
			for(Component comp : componentFile.getComponentSet().getComponent()) {
				int id = processingManager.newId();
				if (comp instanceof BuildComponent) {
					BuildComponent buildComp = (BuildComponent)comp;
					BuildComponentPattern pattern = processingManager.getPatterns().getBuildPattern(buildComp.getClass());
					BuildComponentEntry e = new BuildComponentEntry(componentFile, new ProcessingContext(processingManager), applicationContext, buildComp,  pattern, id, 0);
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
					ComponentEntry e = new ComponentEntry(applicationContext, componentFile, new ProcessingContext(processingManager), comp, pattern, id, 0);
					e.preprocess();
					ret.add(e);
				}
			}
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
		return file.getFolder();
	}

}
