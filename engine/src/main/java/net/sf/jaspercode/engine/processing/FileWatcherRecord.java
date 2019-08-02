package net.sf.jaspercode.engine.processing;

import net.sf.jaspercode.api.ApplicationContext;
import net.sf.jaspercode.api.exception.JasperException;
import net.sf.jaspercode.api.resources.FileWatcher;
import net.sf.jaspercode.engine.application.ProcessingContext;
import net.sf.jaspercode.engine.definitions.ComponentFile;
import net.sf.jaspercode.engine.definitions.UserFile;

// TODO: Not certain that the record and entry need to be separate
public class FileWatcherRecord implements Tracked {
	protected FileWatcher watcher = null;
	protected ComponentFile originatorFile = null;
	protected int originatorId = 0;
	protected int id = 0;
	protected ApplicationContext applicationContext = null;
	protected ProcessingContext processingContext = null;
	protected FolderWatcherEntry entry = null;
	protected String path = null;
	//protected Map<String,Long> filesProcessed = new HashMap<>();
	protected long lastProcessed = 0L;
	
	public FileWatcherRecord(String path, ApplicationContext applicationContext,ProcessingContext processingContext,FileWatcher watcher, ComponentFile originatorFile, int id, int originatorId) {
		this.applicationContext = applicationContext;
		this.processingContext = processingContext;
		this.watcher = watcher;
		this.originatorFile = originatorFile;
		this.id = id;
		this.originatorId = originatorId;
		this.path = path;
	}

	public void resetLastProcessed() {
		lastProcessed = 0;
	}

	public long getLastProcessed() {
		return lastProcessed;
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
	
	public boolean isActive() {
		return entry != null;
	}

	public FileWatcherEntry entry(UserFile userFile) throws JasperException {
		FileWatcherEntry ret = new FileWatcherEntry(path, applicationContext, originatorFile, processingContext, id, watcher, this, userFile);
		watcher.init(ret.getProcessorContext());
		watcher.fileUpdated(userFile);
		lastProcessed = userFile.getLastModified();
		//return new FileWatcherEntry(path, applicationContext, originatorFile, processingContext, id, watcher, this, userFile);
		return ret;
	}
	
}

