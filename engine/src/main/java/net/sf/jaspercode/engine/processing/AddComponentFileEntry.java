package net.sf.jaspercode.engine.processing;

import java.util.ArrayList;
import java.util.List;

import net.sf.jaspercode.api.plugin.ProcessorLogMessage;
import net.sf.jaspercode.engine.application.ProcessingManager;
import net.sf.jaspercode.engine.definitions.ApplicationFolderImpl;
import net.sf.jaspercode.engine.definitions.ComponentFile;
import net.sf.jaspercode.engine.exception.PreprocessingException;

public class AddComponentFileEntry implements FileToProcess {

	private ComponentFile componentFile = null;
	private ProcessingManager processingManager = null;

	public AddComponentFileEntry(ComponentFile componentFile, ProcessingManager processingManager) {
		this.componentFile = componentFile;
		this.processingManager = processingManager;
	}

	@Override
	public int compareTo(Processable o) {
		return -1;
	}

	@Override
	public int getPriority() {
		return 0;
	}

	@Override
	public List<ProcessorLogMessage> getMessages() {
		return new ArrayList<>();
	}

	public void preprocess() throws PreprocessingException {
	}

	@Override
	public boolean process() {
		boolean ret = false;
		try {
			processingManager.addComponentFile(componentFile);
			ret = true;
		} catch(PreprocessingException e) {
			e.printStackTrace();
		}
		return ret;
	}

	@Override
	public ApplicationFolderImpl getFolder() {
		return componentFile.getFolder();
	}

	// There's nothing to commit
	@Override
	public boolean commitChanges() {
		return true;
	}
	@Override
	public void rollbackChanges() {
		// no-op
	}

	@Override
	public ProcessingState getState() {
		return ProcessingState.COMPLETE;
	}

	@Override
	public String getName() {
		return null;
	}
	
}
