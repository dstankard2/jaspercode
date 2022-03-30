package net.sf.jaspercode.engine.processing;

import java.util.Map;

import net.sf.jaspercode.api.exception.JasperException;
import net.sf.jaspercode.api.resources.FolderWatcher;
import net.sf.jaspercode.api.ProcessorContext;
import net.sf.jaspercode.engine.JasperResources;
import net.sf.jaspercode.engine.files.ApplicationFolderImpl;
import net.sf.jaspercode.engine.files.UserFile;

public class FolderWatcherProcessable extends ProcessableBase {
	
	private String filePath;
	private FolderWatcher folderWatcher;
	private Map<String,String> configs;
	private ApplicationFolderImpl folder;
	private FolderWatcherItem folderWatcherItem;

	public FolderWatcherProcessable(int itemId, ProcessableContext ctx, Map<String,String> configs,
			JasperResources jasperResources, String filePath, FolderWatcher folderWatcher, 
			ApplicationFolderImpl folder, FolderWatcherItem folderWatcherItem) {
		super(itemId, configs, ctx, jasperResources);
		this.filePath = filePath;
		this.folderWatcher = folderWatcher;
		this.configs = configs;
		this.folder = folder;
		this.folderWatcherItem = folderWatcherItem;
	}

	@Override
	public int getPriority() {
		return folderWatcher.getPriority();
	}

	@Override
	public String getName() {
		return "FolderWatcher["+filePath+"]";
	}

	public void remove() {
		folderWatcherItem.removeProcessable(filePath);
	}

	@Override
	public boolean process() {
		boolean ret = true;
		
		changes = new ProcessableChanges(itemId);
		
		ProcessorContext procCtx = new ProcessorContextImpl(ctx,  jasperResources, log, configs, folder, changes);
		
		try {
			UserFile userFile = ctx.getUserFile(filePath);
			if (userFile!=null) {
				folderWatcher.process(procCtx,  userFile);
			} else {
				ret = false;
			}
		} catch(JasperException e) {
			this.log.error("Exception in processing of folder watcher", e);
			ret = false;
		}

		return ret;
	}

	@Override
	public ApplicationFolderImpl getFolder() {
		return folder;
	}

}

