package net.sf.jaspercode.engine.processing;

import java.util.Map;

import net.sf.jaspercode.api.config.Component;
import net.sf.jaspercode.api.exception.JasperException;
import net.sf.jaspercode.api.resources.FolderWatcher;
import net.sf.jaspercode.engine.JasperResources;
import net.sf.jaspercode.engine.files.ComponentFile;
import net.sf.jaspercode.engine.files.UserFile;

public class FolderWatcherProcessable extends ProcessableBase {

	private FolderWatcher folderWatcher = null;
	private String userFilePath = null;
	private Component component = null;

	public FolderWatcherProcessable(int itemId,ProcessableContext processableContext,
			Component component, ComponentFile originatorFile, Map<String,String> configs,String userFilePath,
			FolderWatcher folderWatcher,JasperResources jasperResources) {
		super(itemId, processableContext, originatorFile, jasperResources);
		this.userFilePath = userFilePath;
		this.folderWatcher = folderWatcher;
		this.log = new ProcessorLog(getName());
		this.component = component;
	}

	public String getUserFilePath() {
		return userFilePath;
	}

	@Override
	public int getPriority() {
		return folderWatcher.getPriority();
	}

	@Override
	public String getName() {
		return folderWatcher.getName()+"["+userFilePath+"]";
	}

	@Override
	public boolean process() {
		boolean ret = false;
		Map<String,String> configs = ProcessingUtilities.getConfigs(originatorFile, component);
		changes = new ProcessableChanges(itemId, originatorFile, component);
		
		ret = ProcessingUtilities.populateConfigurations(folderWatcher, log, configs);
		if (ret) {
			try {
				UserFile userFile = (UserFile)processableCtx.getUserFile(userFilePath);
				ProcessorContextImpl c = new ProcessorContextImpl(itemId, processableCtx, jasperResources, log, folder, configs, changes);
				folderWatcher.process(c, userFile);
				ret = true;
			} catch(JasperException e) {
				// TODO: Have to produce error condition in caller
				this.log.error(e.getMessage(), e);
				ret = false;
			}
		}
		
		if (ret) {
			this.state = ProcessingState.COMPLETE;
		} else {
			this.state = ProcessingState.ERROR;
		}

		return ret;
	}

}

