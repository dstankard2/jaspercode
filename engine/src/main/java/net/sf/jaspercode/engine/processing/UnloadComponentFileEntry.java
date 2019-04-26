package net.sf.jaspercode.engine.processing;

import java.util.ArrayList;
import java.util.List;

import net.sf.jaspercode.api.plugin.ProcessorLogMessage;
import net.sf.jaspercode.engine.application.ApplicationManager;
import net.sf.jaspercode.engine.definitions.ApplicationFolderImpl;
import net.sf.jaspercode.engine.definitions.ComponentFile;
import net.sf.jaspercode.engine.exception.PreprocessingException;

public class UnloadComponentFileEntry implements Processable {

	private ComponentFile componentFile = null;
	private ApplicationManager applicationManager = null;
	ApplicationFolderImpl folder = null;

	public UnloadComponentFileEntry(ComponentFile componentFile, ApplicationManager applicationManager, ApplicationFolderImpl folder) {
		this.componentFile = componentFile;
		this.applicationManager = applicationManager;
		this.folder = folder;
	}

	@Override
	public int compareTo(Processable o) {
		return -1;
	}

	@Override
	public int getPriority() {
		// TODO Auto-generated method stub
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
		applicationManager.unloadComponentFile(componentFile, true);
		return true;
	}

	@Override
	public ApplicationFolderImpl getFolder() {
		return folder;
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
