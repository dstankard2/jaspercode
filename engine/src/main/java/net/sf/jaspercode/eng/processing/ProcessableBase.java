
package net.sf.jaspercode.eng.processing;

import java.util.List;
import java.util.Map;

import net.sf.jaspercode.api.plugin.ProcessorLogMessage;
import net.sf.jaspercode.eng.JasperResources;

public abstract class ProcessableBase implements Comparable<ProcessableBase> {

	protected ProcessableContext ctx = null;
	protected ProcessorLog log = null;
	protected JasperResources jasperResources = null;
	protected Map<String,String> configs = null;
	
	protected ProcessableBase(Map<String,String> configs, ProcessableContext ctx, JasperResources jasperResources) {
		this.configs = configs;
		this.ctx = ctx;
		this.jasperResources = jasperResources;
		this.log = new ProcessorLog("ProcessableLog");
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

