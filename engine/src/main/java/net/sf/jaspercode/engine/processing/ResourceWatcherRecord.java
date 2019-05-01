package net.sf.jaspercode.engine.processing;

import java.util.ArrayList;
import java.util.List;

import net.sf.jaspercode.api.ApplicationContext;
import net.sf.jaspercode.api.resources.ResourceWatcher;
import net.sf.jaspercode.engine.application.ProcessingContext;
import net.sf.jaspercode.engine.definitions.ComponentFile;

// TODO: Not certain that the record and entry need to be separate
public class ResourceWatcherRecord implements Tracked {
	protected ResourceWatcher watcher = null;
	protected ComponentFile originatorFile = null;
	protected int originatorId = 0;
	protected int id = 0;
	protected ApplicationContext applicationContext = null;
	protected ProcessingContext processingContext = null;
	protected long lastRun = 0;
	protected List<String> matchedFiles = new ArrayList<>();
	protected ResourceWatcherEntry entry = null;
	
	public ResourceWatcherRecord(ApplicationContext applicationContext,ProcessingContext processingContext,ResourceWatcher watcher, ComponentFile originatorFile, int id, int originatorId) {
		this.applicationContext = applicationContext;
		this.processingContext = processingContext;
		this.watcher = watcher;
		this.originatorFile = originatorFile;
		this.id = id;
		this.originatorId = originatorId;
		this.entry = new ResourceWatcherEntry(applicationContext, originatorFile, processingContext, id, watcher, this);
	}

	@Override
	public int getId() {
		return id;
	}

	@Override
	public String getName() {
		return "ResourceWatcher["+getPath()+"]";
	}

	@Override
	public int getOriginatorId() {
		return originatorId;
	}
	
	public String getPath() {
		return watcher.getPath();
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

	public ResourceWatcherEntry entry() {
		if (entry==null) {
			entry = new ResourceWatcherEntry(applicationContext, originatorFile, processingContext, id, watcher, this);
		}
		return entry;
	}

}

