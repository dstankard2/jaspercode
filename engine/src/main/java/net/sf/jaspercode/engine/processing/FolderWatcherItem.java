package net.sf.jaspercode.engine.processing;

import java.util.Map;

import net.sf.jaspercode.api.resources.FolderWatcher;
import net.sf.jaspercode.engine.application.JasperResources;
import net.sf.jaspercode.engine.application.ProcessingContext;
import net.sf.jaspercode.engine.definitions.ComponentFile;

public class FolderWatcherItem implements Item {

	private String path = null;
	private int id = 0;
	private FolderWatcher folderWatcher = null;
	private int originatorId = 0;
	private ProcessingContext processingContext = null;
	private Map<String,String> configs = null;
	private ComponentFile componentFile = null;
	private JasperResources jasperResources = null;

	public FolderWatcherItem(int id,FolderWatcher folderWatcher, int originatorId,
			String path,ProcessingContext processingContext,ComponentFile componentFile,
			Map<String,String> configs,JasperResources jasperResources) {
		this.id = id;
		this.folderWatcher = folderWatcher;
		this.originatorId = originatorId;
		this.path = path;
		this.processingContext = processingContext;
		this.componentFile = componentFile;
		this.configs = configs;
		this.jasperResources = jasperResources;
	}

	public String getName() {
		return folderWatcher.getName();
	}

	@Override
	public int getOriginatorId() {
		return originatorId;
	}

	@Override
	public int getId() {
		return id;
	}

	public String getPath() {
		return path;
	}
	
	public FolderWatcherProcessable getProc(String filePath) {
		FolderWatcherProcessable ret = new FolderWatcherProcessable(id,processingContext,componentFile,configs,filePath,folderWatcher,jasperResources);

		return ret;
	}
	
}

