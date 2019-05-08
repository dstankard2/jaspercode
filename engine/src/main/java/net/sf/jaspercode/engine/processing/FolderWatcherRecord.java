package net.sf.jaspercode.engine.processing;

import java.util.HashMap;
import java.util.Map;

import net.sf.jaspercode.api.ApplicationContext;
import net.sf.jaspercode.api.resources.ApplicationFile;
import net.sf.jaspercode.api.resources.FolderWatcher;
import net.sf.jaspercode.engine.application.ProcessingContext;
import net.sf.jaspercode.engine.definitions.ComponentFile;

// TODO: Not certain that the record and entry need to be separate
public class FolderWatcherRecord implements Tracked {
	protected FolderWatcher watcher = null;
	protected ComponentFile originatorFile = null;
	protected int originatorId = 0;
	protected int id = 0;
	protected ApplicationContext applicationContext = null;
	protected ProcessingContext processingContext = null;
	protected long lastRun = 0;
	protected FolderWatcherEntry entry = null;
	protected String path = null;
	protected Map<String,Long> filesProcessed = new HashMap<>();
	
	public FolderWatcherRecord(String path, ApplicationContext applicationContext,ProcessingContext processingContext,FolderWatcher watcher, ComponentFile originatorFile, int id, int originatorId) {
		this.applicationContext = applicationContext;
		this.processingContext = processingContext;
		this.watcher = watcher;
		this.originatorFile = originatorFile;
		this.id = id;
		this.originatorId = originatorId;
		this.path = path;
	}

	public Map<String,Long> getFilesProcessed() {
		return filesProcessed;
	}

	@Override
	public int getId() {
		return id;
	}

	@Override
	public String getName() {
		return "FolderWatcher["+getPath()+"]";
	}

	@Override
	public int getOriginatorId() {
		return originatorId;
	}
	
	public String getPath() {
		return path;
	}
	
	public long getLastRun() {
		return lastRun;
	}
	
	public void setLastRun(long lastRun) {
		this.lastRun = lastRun;
	}
	
	public boolean isActive() {
		return entry != null;
	}

	public void deactivate() {
		this.entry = null;
	}

	public FolderWatcherEntry entry(ApplicationFile applicationFile) {
		return new FolderWatcherEntry(path, applicationContext, originatorFile, processingContext, id, watcher, this, applicationFile);
	}

}

