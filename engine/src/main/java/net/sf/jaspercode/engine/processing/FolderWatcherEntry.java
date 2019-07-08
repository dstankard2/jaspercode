package net.sf.jaspercode.engine.processing;

import java.util.HashMap;

import net.sf.jaspercode.api.ApplicationContext;
import net.sf.jaspercode.api.resources.ApplicationFile;
import net.sf.jaspercode.api.resources.FolderWatcher;
import net.sf.jaspercode.engine.application.ProcessingContext;
import net.sf.jaspercode.engine.definitions.ComponentFile;

/**
 * This represents a resource watcher that needs to be executed for a watched resource path.
 * @author DCS
 */
public class FolderWatcherEntry extends ProcessableBase {

	private FolderWatcher folderWatcher = null;
	private FolderWatcherRecord record = null;
	private String path = null;
	private ApplicationFile applicationFile = null;

	public FolderWatcherEntry(String path, ApplicationContext applicationContext, ComponentFile componentFile,ProcessingContext processingContext, int id, FolderWatcher folderWatcher,FolderWatcherRecord record, ApplicationFile applicationFile) {
		super(applicationContext, componentFile, processingContext, id, path, new HashMap<String,String>());
		this.folderWatcher = folderWatcher;
		this.applicationFile = applicationFile;
		this.record = record;
		this.path = path;
		this.log = new ProcessorLog(getName());
		this.processorContext = new ProcessorContextImpl(componentFile.getFolder(), this, log);
		this.folderWatcher.init(processorContext);
	}

	public String getPath() {
		return path;
	}

	@Override
	public int getPriority() {
		return folderWatcher.getPriority();
	}

	public boolean preprocess() {
		return populateConfigurations(folderWatcher);
	}

	@Override
	public boolean process() {
		boolean ret = true;
		
		this.state = ProcessingState.PROCESSING;
		try {
			folderWatcher.process(applicationFile);
			this.state = ProcessingState.COMPLETE;
			record.setLastRun(System.currentTimeMillis());
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
		return "FolderWatcher["+getPath()+"] for file '"+applicationFile.getPath()+"'";
	}

}

