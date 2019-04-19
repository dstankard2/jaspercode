package net.sf.jaspercode.engine.processing;

import java.util.ArrayList;
import java.util.List;

import net.sf.jaspercode.engine.application.ProcessingManager;
import net.sf.jaspercode.engine.definitions.ApplicationFolderImpl;
import net.sf.jaspercode.engine.definitions.WatchedResource;
import net.sf.jaspercode.engine.exception.PreprocessingException;

public class ModifiedFile implements FileToProcess {

	private WatchedResource newFile = null;
	private WatchedResource oldFile = null;
	private ProcessingManager processingManager = null;

	public ModifiedFile(WatchedResource newFile, WatchedResource oldFile, ProcessingManager processingManager) {
		super();
		this.newFile = newFile;
		this.oldFile = oldFile;
		this.processingManager = processingManager;
	}

	@Override
	public List<Processable> preprocess() throws PreprocessingException {
		List<Processable> ret = new ArrayList<>();
		
		// it was a component file and is now a userFile (probably because the new XML is invalid)
		
		// it was a component file and is still
		
		// it was a user file and is still
		
		// it was a user file and is now a component file (probably because the new XML is valid)

		return ret;
	}

	@Override
	public ApplicationFolderImpl getFolder() {
		return newFile.getFolder();
	}

}

