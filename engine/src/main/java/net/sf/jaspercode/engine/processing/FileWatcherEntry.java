package net.sf.jaspercode.engine.processing;

import java.util.HashMap;

import net.sf.jaspercode.api.ApplicationContext;
import net.sf.jaspercode.api.resources.ApplicationFile;
import net.sf.jaspercode.api.resources.FileWatcher;
import net.sf.jaspercode.engine.application.ProcessingContext;
import net.sf.jaspercode.engine.definitions.ComponentFile;
import net.sf.jaspercode.engine.definitions.UserFile;

/**
 * This represents a resource watcher that needs to be executed for a watched resource path.
 * @author DCS
 */
public class FileWatcherEntry extends ProcessableBase {

	private FileWatcher fileWatcher = null;
	private FileWatcherRecord record = null;
	private String path = null;
	private ApplicationFile applicationFile = null;

	public FileWatcherEntry(String path, ApplicationContext applicationContext, ComponentFile componentFile,ProcessingContext processingContext, int id, FileWatcher fileWatcher,FileWatcherRecord record, UserFile applicationFile) {
		super(applicationContext, componentFile, processingContext, id, path, new HashMap<String,String>());
		this.fileWatcher = fileWatcher;
		this.applicationFile = applicationFile;
		this.record = record;
		this.path = path;
		this.log = new ProcessorLog(getName());
		this.processorContext = new ProcessorContextImpl(componentFile.getFolder(), this, log);
		this.fileWatcher.init(processorContext);
	}

	public String getPath() {
		return path;
	}

	@Override
	public int getPriority() {
		return fileWatcher.getPriority();
	}

	public boolean preprocess() {
		return populateConfigurations(fileWatcher);
	}
	
	public FileWatcherRecord getRecord() {
		return record;
	}

	@Override
	public boolean process() {
		boolean ret = true;
		
		this.state = ProcessingState.PROCESSING;
		try {
			//fileWatcher.init(processorContext);
			fileWatcher.processUpdates();
			//fileWatcher.process(applicationFile);
			this.state = ProcessingState.COMPLETE;
			//record.setLastRun(System.currentTimeMillis());
		} catch(Exception e) {
			ret = false;
			this.log.error(e.getMessage(), e);
			this.state = ProcessingState.ERROR;
			//e.printStackTrace();
		}
		
		return ret;
	}

	@Override
	public String getName() {
		return "FileWatcher["+getPath()+"]";
	}

}

