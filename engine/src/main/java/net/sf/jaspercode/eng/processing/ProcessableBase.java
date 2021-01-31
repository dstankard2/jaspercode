
package net.sf.jaspercode.eng.processing;

import java.util.List;

import net.sf.jaspercode.api.plugin.ProcessorLogMessage;
import net.sf.jaspercode.eng.JasperResources;
import net.sf.jaspercode.eng.files.ApplicationFolderImpl;
import net.sf.jaspercode.eng.files.ComponentFile;

public abstract class ProcessableBase implements Processable {

	protected int itemId = 0;
	protected ProcessableContext processableCtx = null;
	protected ProcessorLog log = null;
	protected ApplicationFolderImpl folder = null;
	protected ProcessingState state = ProcessingState.TO_PROCESS;
	protected ComponentFile originatorFile = null;
	protected JasperResources jasperResources = null;
	
	protected ProcessableChanges changes = null;

	protected ProcessableBase(int itemId, ProcessableContext processingCtx,ComponentFile originatorFile,
			JasperResources jasperResources) {
		this.itemId = itemId;
		this.processableCtx = processingCtx;
		this.originatorFile = originatorFile;
		this.folder = originatorFile.getFolder();
		this.jasperResources = jasperResources;
		this.changes = null;
	}

	public ComponentFile getComponentFile() {
		return originatorFile;
	}

	@Override
	public ProcessorLog getLog() {
		return log;
	}

	// Compares priority of components, for sorting
	@Override
	public int compareTo(Processable o) {
		if (o==null) return -1;

		int p = this.getPriority();
		int op = o.getPriority();
		if (p>op) return 1;
		else if (p==op) return 0;
		else return -1;
	}
	
	@Override
	public void clearLogMessages() {
		if (this.log!=null) {
			this.log.getMessages(true);
		}
	}
	@Override
	public int getItemId() {
		return itemId;
	}

	@Override
	public ProcessingState getState() {
		return this.state;
	}

	@Override
	public abstract int getPriority();

	@Override
	public List<ProcessorLogMessage> getMessages() {
		return log.getMessages(false);
	}

	@Override
	public abstract String getName();

	@Override
	public abstract boolean process();

	@Override
	public ProcessableChanges getChanges() {
		return changes;
	}

}

