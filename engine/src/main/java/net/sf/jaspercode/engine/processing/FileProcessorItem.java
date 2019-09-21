package net.sf.jaspercode.engine.processing;

import java.util.Map;

import net.sf.jaspercode.api.ApplicationContext;
import net.sf.jaspercode.api.exception.JasperException;
import net.sf.jaspercode.api.resources.FileProcessor;
import net.sf.jaspercode.engine.application.JasperResources;
import net.sf.jaspercode.engine.application.ProcessingContext;
import net.sf.jaspercode.engine.definitions.ApplicationFolderImpl;
import net.sf.jaspercode.engine.definitions.ComponentFile;
import net.sf.jaspercode.engine.definitions.UserFile;

public class FileProcessorItem extends ProcessableBase {

	private String path = null;
	private FileProcessor fileProcessor = null;
	//protected ComponentFile originatorFile = null;
	protected int originatorId = 0;
	protected ApplicationContext applicationContext = null;
	
	protected UserFile userFile;
	
	public FileProcessorItem(int id, String path,ApplicationContext applicationContext,
			ProcessingContext processingContext,FileProcessor fileProcessor,
			ComponentFile componentFile,ApplicationFolderImpl folder, 
			Map<String,String> configs,int originatorId,JasperResources jasperResources) {
		super(id, processingContext, componentFile, configs, originatorId, jasperResources);
		this.path = path;
		this.applicationContext = applicationContext;
		this.fileProcessor = fileProcessor;
		this.originatorId = originatorId;

		ProcessorContextImpl impl = new ProcessorContextImpl(this,folder,log,jasperResources);
		fileProcessor.init(impl);

		this.log = new ProcessorLog(getName());
	}

	@Override
	public ProcessorLog getLog() {
		return log;
	}

	public int getId() {
		return id;
	}

	@Override
	public int getPriority() {
		return fileProcessor.getPriority();
	}

	@Override
	public String getName() {
		return fileProcessor.getName();
	}

	public void fileUpdated(UserFile userFile) throws JasperException {
		//ProcessorContextImpl impl = new ProcessorContextImpl(this, folder,log);
		fileProcessor.setFile(userFile);
	}
	
	public String getFilePath() {
		return path;
	}

	@Override
	public boolean process() {
		boolean ret = true;
		
		try {
			this.state = ProcessingState.PROCESSING;
			ret = ProcessingUtilities.populateConfigurations(fileProcessor, log, configs);
			if (!ret) {
				this.state = ProcessingState.ERROR;
				return ret;
			}
			fileProcessor.process();
			this.state = ProcessingState.COMPLETE;
		} catch(JasperException e) {
			ret = false;
			this.state = ProcessingState.ERROR;
			log.error("Exception while processing: "+e.getMessage(), e);
		}
		
		return ret;
	}

}

