package net.sf.jaspercode.eng.processing;

import java.util.Map;

import net.sf.jaspercode.api.ProcessorContext;
import net.sf.jaspercode.api.exception.JasperException;
import net.sf.jaspercode.api.resources.FolderWatcher;
import net.sf.jaspercode.eng.JasperResources;
import net.sf.jaspercode.eng.files.ApplicationFolderImpl;
import net.sf.jaspercode.eng.files.UserFile;

public class FolderWatcherProcessable extends ProcessableBase {
	
	private UserFile userFile;
	private FolderWatcher folderWatcher;
	private Map<String,String> configs;
	private ApplicationFolderImpl folder;

	public FolderWatcherProcessable(ProcessableContext ctx, Map<String,String> configs,
			JasperResources jasperResources, UserFile userFile, FolderWatcher folderWatcher, 
			ApplicationFolderImpl folder) {
		super(configs, ctx, jasperResources);
		this.userFile = userFile;
		this.folderWatcher = folderWatcher;
		this.configs = configs;
		this.folder = folder;
	}

	@Override
	public int getPriority() {
		return folderWatcher.getPriority();
	}

	@Override
	public String getName() {
		return "FolderWatcher["+userFile.getPath()+"]";
	}

	@Override
	public boolean process() {
		boolean ret = true;
		
		ProcessorContext procCtx = new ProcessorContextImpl(ctx,  jasperResources, log, configs, folder);
		
		try {
			folderWatcher.process(procCtx,  userFile);
		} catch(JasperException e) {
			this.log.error("Exception in processing of folder watcher", e);
			ret = false;
		}

		return ret;
	}

}

