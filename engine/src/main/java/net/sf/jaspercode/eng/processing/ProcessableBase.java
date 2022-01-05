
package net.sf.jaspercode.eng.processing;

import java.util.List;

import net.sf.jaspercode.api.plugin.ProcessorLogMessage;
import net.sf.jaspercode.eng.JasperResources;
import net.sf.jaspercode.eng.files.ApplicationFolderImpl;
import net.sf.jaspercode.eng.files.ComponentFile;

public abstract class ProcessableBase implements Comparable<ProcessableBase> {

	protected ProcessableContext ctx = null;
	protected ProcessorLog log = null;
	protected ApplicationFolderImpl folder = null;
	protected ComponentFile originatorFile = null;
	protected JasperResources jasperResources = null;
	
	protected ProcessableBase(ProcessableContext ctx, ComponentFile originatorFile,
			JasperResources jasperResources) {
		this.ctx = ctx;
		this.originatorFile = originatorFile;
		this.folder = originatorFile.getFolder();
		this.jasperResources = jasperResources;
		this.log = new ProcessorLog("ProcessableLog");
	}

	public ComponentFile getComponentFile() {
		return originatorFile;
	}

	public ProcessorLog getLog() {
		return log;
	}

	// Compares priority of components, for sorting
	@Override
	public int compareTo(ProcessableBase o) {
		if (o==null) return -1;

		int p = this.getPriority();
		int op = o.getPriority();
		if (p>op) return 1;
		else if (p==op) return 0;
		else return -1;
	}
	
	public void clearLogMessages() {
		if (this.log!=null) {
			this.log.getMessages(true);
		}
	}

	public abstract int getPriority();

	public List<ProcessorLogMessage> getMessages() {
		return log.getMessages(false);
	}

	public abstract String getName();

	public abstract boolean process();

}

