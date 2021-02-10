package net.sf.jaspercode.engine.processing;

import java.util.Map;

import net.sf.jaspercode.api.config.Component;
import net.sf.jaspercode.api.exception.JasperException;
import net.sf.jaspercode.api.resources.FileProcessor;
import net.sf.jaspercode.engine.JasperResources;
import net.sf.jaspercode.engine.files.ComponentFile;
import net.sf.jaspercode.engine.files.UserFile;

public class FileProcessorRecord extends ProcessableBase {

	private String path = null;
	private FileProcessor fileProcessor = null;
	private ProcessorContextImpl procCtxImpl = null;
	
	protected UserFile userFile;
	private Map<String,String> configs = null;
	
	public FileProcessorRecord(int itemId, String path, Component component, ProcessableContext processableCtx,
			FileProcessor fileProcessor, ComponentFile componentFile, JasperResources jasperResources) {
		super(itemId, processableCtx, componentFile, jasperResources);
		this.path = path;
		this.fileProcessor = fileProcessor;
		this.log = new ProcessorLog(getName());
		//this.component = component;

		configs = ProcessingUtilities.getConfigs(originatorFile, component);
		changes = new ProcessableChanges(itemId, originatorFile, component);
		
		this.procCtxImpl = new ProcessorContextImpl(itemId, processableCtx, jasperResources, log, folder, configs, changes);
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

		try {
			fileProcessor.init(procCtxImpl);
			fileProcessor.setFile(userFile);
		} catch(JasperException e) {
			ret = false;
		}
		
		return ret;
	}

	@Override
	public boolean process() {
		boolean ret = true;

		try {
			this.state = ProcessingState.PROCESSING;
			ret = ProcessingUtilities.populateConfigurations(fileProcessor, log, configs);
			if (ret) {
				fileProcessor.process();
				this.state = ProcessingState.COMPLETE;
			}
			if (!ret) {
				this.state = ProcessingState.ERROR;
			}
			//changes = procCtxImpl.getChanges();
		} catch(JasperException e) {
			ret = false;
			this.state = ProcessingState.ERROR;
			log.error("Exception while processing: "+e.getMessage(), e);
		}
		
		return ret;
	}

}

