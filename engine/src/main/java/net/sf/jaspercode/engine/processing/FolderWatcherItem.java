package net.sf.jaspercode.engine.processing;

import java.util.HashMap;
import java.util.Map;

import net.sf.jaspercode.api.resources.FolderWatcher;
import net.sf.jaspercode.engine.JasperResources;
import net.sf.jaspercode.engine.files.ApplicationFolderImpl;

public class FolderWatcherItem implements Item {

	private String path = null;
	private int itemId = 0;
	private int originatorId = 0;
	private FolderWatcher folderWatcher = null;
	private ProcessableContext processableContext = null;
	private JasperResources jasperResources = null;
	private Map<String,FolderWatcherProcessable> procs = new HashMap<>();
	private Map<String,String> configs = null;
	private ApplicationFolderImpl folder;
	
	public FolderWatcherItem(int itemId,String path, FolderWatcher folderWatcher, 
			ProcessableContext processableContext,JasperResources jasperResources,
			Map<String,String> configs, int originatorId, ApplicationFolderImpl folder) {
		this.itemId = itemId;
		this.folderWatcher = folderWatcher;
		this.path = path;
		this.processableContext = processableContext;
		this.jasperResources = jasperResources;
		this.originatorId = originatorId;
		this.configs = configs;
		this.folder = folder;
	}

	@Override
	public void assignItemId(int itemId) {
		this.itemId = itemId;
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
		FolderWatcherProcessable ret = new FolderWatcherProcessable(itemId, processableContext, configs, 
				jasperResources, filePath, folderWatcher, folder);
		procs.put(filePath, ret);
		return ret;
	}
	
	public void clearProcs() {
		procs.clear();
	}
	
}

