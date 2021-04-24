package net.sf.jaspercode.engine.processing;

import java.util.HashMap;
import java.util.Map;

import net.sf.jaspercode.api.config.Component;
import net.sf.jaspercode.api.resources.FolderWatcher;
import net.sf.jaspercode.engine.JasperResources;
import net.sf.jaspercode.engine.files.ComponentFile;

public class FolderWatcherItem implements Item {

	private String path = null;
	private int itemId = 0;
	private FolderWatcher folderWatcher = null;
	private ProcessableContext processableContext = null;
	private ComponentFile componentFile = null;
	private JasperResources jasperResources = null;
	private Component component = null;
	private int originatorId = 0;
	private Map<String,FolderWatcherProcessable> procs = new HashMap<>();
	
	public FolderWatcherItem(int itemId,FolderWatcher folderWatcher, 
			String path,ProcessableContext processableContext,ComponentFile componentFile,
			JasperResources jasperResources,Component component, int originatorId) {
		this.itemId = itemId;
		this.folderWatcher = folderWatcher;
		this.path = path;
		this.processableContext = processableContext;
		this.componentFile = componentFile;
		this.jasperResources = jasperResources;
		this.component = component;
		this.originatorId = originatorId;
	}

	public int getOriginatorId() {
		return originatorId;
	}

	public String getName() {
		return folderWatcher.getName();
	}

	@Override
	public int getItemId() {
		return itemId;
	}

	public String getPath() {
		return path;
	}
	
	public FolderWatcherProcessable getProc(String filePath) {
		jasperResources.engineDebug("Folder watcher ID "+itemId+" processing for path "+filePath);
		if (procs.get(filePath)!=null) {
			return null;
		}
		Map<String,String> configs = ProcessingUtilities.getConfigs(componentFile, component);
		FolderWatcherProcessable ret = new FolderWatcherProcessable(itemId,processableContext,component, componentFile,configs,filePath,folderWatcher,jasperResources);
		procs.put(filePath, ret);
		return ret;
	}
	
	public void clearProcs() {
		procs.clear();
	}
	
}

