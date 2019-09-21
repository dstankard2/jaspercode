package net.sf.jaspercode.engine.processing;

import java.util.Map;

import net.sf.jaspercode.api.exception.JasperException;
import net.sf.jaspercode.api.resources.FolderWatcher;
import net.sf.jaspercode.engine.application.JasperResources;
import net.sf.jaspercode.engine.application.ProcessingContext;
import net.sf.jaspercode.engine.definitions.ComponentFile;
import net.sf.jaspercode.engine.definitions.UserFile;

public class FolderWatcherProcessable extends ProcessableBase {

	private FolderWatcher folderWatcher = null;
	private String userFilePath = null;

	public FolderWatcherProcessable(int originatorId,ProcessingContext processingContext,
			ComponentFile originatorFile, Map<String,String> configs,String userFilePath,
			FolderWatcher folderWatcher,JasperResources jasperResources) {
		super(originatorId, processingContext, originatorFile, configs, originatorId, jasperResources);
		this.userFilePath = userFilePath;
		this.folderWatcher = folderWatcher;
		this.log = new ProcessorLog(getName());
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
		boolean ret = true;

		// TODO: Populate configs should probably not be in the processable but in the item
		// TODO: That might be true in general
		ret = ProcessingUtilities.populateConfigurations(folderWatcher, log, configs);
		if (ret) {
			try {
				UserFile userFile = ctx.getUserFile(userFilePath);
				ProcessorContextImpl c = new ProcessorContextImpl(this, this.folder, log, jasperResources);
				folderWatcher.process(c, userFile);
			} catch(JasperException e) {
				this.log.error(e.getMessage(), e);
			}
		}

		return ret;
	}

}

