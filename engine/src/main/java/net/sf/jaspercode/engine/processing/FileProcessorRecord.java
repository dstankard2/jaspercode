package net.sf.jaspercode.engine.processing;

import java.util.Map;

import net.sf.jaspercode.api.exception.JasperException;
import net.sf.jaspercode.api.resources.FileProcessor;
import net.sf.jaspercode.engine.JasperResources;
import net.sf.jaspercode.engine.files.ApplicationFolderImpl;
import net.sf.jaspercode.engine.files.UserFile;

public class FileProcessorRecord extends ProcessableBase {

	private String path = null;
	private FileProcessor fileProcessor = null;
	private ProcessorContextImpl procCtxImpl = null;
	
	protected UserFile userFile;
	private ApplicationFolderImpl folder;
	
	public FileProcessorRecord(int itemId, String path, ProcessableContext processableCtx, FileProcessor fileProcessor, 
			Map<String,String> configs, JasperResources jasperResources, ApplicationFolderImpl folder) {
		super(itemId, configs, processableCtx, jasperResources);
		this.path = path;
		this.fileProcessor = fileProcessor;
		this.log = new ProcessorLog(getName());
		this.folder = folder;

		changes = new ProcessableChanges(itemId);

		this.procCtxImpl = new ProcessorContextImpl(ctx, jasperResources, log, configs, folder, changes);
	}

	@Override
	public ProcessorLog getLog() {
		return log;
	}

	public int getItemId() {
		return itemId;
	}

	@Override
	public int getPriority() {
		return fileProcessor.getPriority();
	}

	@Override
	public String getName() {
		return fileProcessor.getName();
	}

	public String getFilePath() {
		return path;
	}

	public boolean init(UserFile userFile) {
		boolean ret = true;

		fileProcessor.init(procCtxImpl);
		fileProcessor.setFile(userFile);
		
		return ret;
	}

	@Override
	public boolean process() {
		boolean ret = true;

		try {
			ret = ProcessingUtilities.populateConfigurations(fileProcessor, log, configs);
			if (ret) {
				fileProcessor.process();
			}

		} catch(JasperException e) {
			ret = false;
			log.error("Exception while processing: "+e.getMessage(), e);
		}
		
		return ret;
	}

	@Override
	public ApplicationFolderImpl getFolder() {
		return folder;
	}

}

