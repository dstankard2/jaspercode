package net.sf.jaspercode.eng.processing;

import java.util.Map;

import net.sf.jaspercode.api.ProcessorContext;
import net.sf.jaspercode.api.exception.JasperException;
import net.sf.jaspercode.api.resources.FolderWatcher;
import net.sf.jaspercode.eng.JasperResources;
import net.sf.jaspercode.eng.files.ComponentFile;
import net.sf.jaspercode.eng.files.UserFile;

public class FolderWatcherProcessable extends ProcessableBase {
	
	private UserFile userFile;
	private FolderWatcher folderWatcher;

	public FolderWatcherProcessable(ProcessableContext ctx, ComponentFile originatorFile,
			JasperResources jasperResources, UserFile userFile, FolderWatcher folderWatcher) {
		super(ctx, originatorFile, jasperResources);
		this.userFile = userFile;
		this.folderWatcher = folderWatcher;
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
		
		Map<String,String> configs = ProcessingUtilities.getConfigs(originatorFile, null);
		ProcessorContext procCtx = new ProcessorContextImpl(ctx,  jasperResources, log, folder, configs, originatorFile);
		
		try {
			folderWatcher.process(procCtx,  userFile);
		} catch(JasperException e) {
			this.log.error("Exception in processing of folder watcher", e);
			ret = false;
		}

		return ret;
	}

}

