package net.sf.jaspercode.engine.processing;

import java.util.ArrayList;
import java.util.List;

import net.sf.jaspercode.api.ApplicationContext;
import net.sf.jaspercode.api.resources.ApplicationFile;
import net.sf.jaspercode.api.resources.FileWatcher;
import net.sf.jaspercode.engine.application.ProcessingContext;
import net.sf.jaspercode.engine.definitions.ComponentFile;

// TODO: ResourceWatcherRecord abstract class
// TODO: entry returns processable
// TODO: isActive()
public class FileWatcherRecord implements Tracked {
	protected FileWatcher fileWatcher = null;
	protected ComponentFile originatorFile = null;
	protected int originatorId = 0;
	protected int id = 0;
	protected ApplicationContext applicationContext = null;
	protected ProcessingContext processingContext = null;
	protected long lastRun = 0;
	protected List<String> matchedFiles = new ArrayList<>();
	protected FileWatcherEntry currentEntry = null;
	protected String path = null;
	
	public FileWatcherRecord(String path, ApplicationContext applicationContext,ProcessingContext processingContext,FileWatcher fileWatcher, ComponentFile originatorFile, int id, int originatorId) {
		this.applicationContext = applicationContext;
		this.processingContext = processingContext;
		this.fileWatcher = fileWatcher;
		this.originatorFile = originatorFile;
		this.id = id;
		this.originatorId = originatorId;
		this.path = path;
	}

	@Override
	public int getId() {
		return id;
	}

	@Override
	public String getName() {
		return "FileWatcher["+getPath()+"]";
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
		return currentEntry != null;
	}

	public void deactivate() {
		this.currentEntry = null;
	}

	public FileWatcherEntry currentEntry() {
		return currentEntry;
	}

	public FileWatcherEntry entry(ApplicationFile applicationFile) {
		currentEntry = new FileWatcherEntry(path, applicationContext, originatorFile, processingContext, id, fileWatcher, this, applicationFile);
		return currentEntry;
	}

}

