package net.sf.jaspercode.eng.processing;

import java.util.Map;

import net.sf.jaspercode.api.ProcessorContext;
import net.sf.jaspercode.api.exception.JasperException;
import net.sf.jaspercode.api.resources.FileProcessor;
import net.sf.jaspercode.eng.JasperResources;
import net.sf.jaspercode.eng.files.ApplicationFolderImpl;
import net.sf.jaspercode.eng.files.UserFile;

public class FileProcessable extends ProcessableBase {
	private FileProcessor fileProcessor;
	private ProcessorContext procCtx;
	private UserFile userFile;
	
	public FileProcessable(ProcessableContext ctx, Map<String,String> configs,
			JasperResources jasperResources, FileProcessor fileProcessor, UserFile userFile, 
			ApplicationFolderImpl folder) {
		super(configs, ctx, jasperResources);
		this.fileProcessor = fileProcessor;

		this.procCtx = new ProcessorContextImpl(ctx, jasperResources, log, configs, folder);
		this.fileProcessor.setFile(userFile);
		this.fileProcessor.init(procCtx);
		this.userFile = userFile;
	}

	@Override
	public int getPriority() {
		return fileProcessor.getPriority();
	}

	@Override
	public String getName() {
		return fileProcessor.getName();
	}

	@Override
	public boolean process() {
		boolean ret = true;
		
		try {
			fileProcessor.init(procCtx);
			fileProcessor.setFile(userFile);
			fileProcessor.process();
		} catch(JasperException e) {
			log.error("Couldn't process file", e);
			ret = false;
		}
		
		return ret;
	}

}
