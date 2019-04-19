package net.sf.jaspercode.engine.processing;

import java.util.List;

import net.sf.jaspercode.engine.definitions.ApplicationFolderImpl;
import net.sf.jaspercode.engine.exception.PreprocessingException;

public interface FileToProcess {

	public List<Processable> preprocess() throws PreprocessingException;
	public ApplicationFolderImpl getFolder();
	
}

