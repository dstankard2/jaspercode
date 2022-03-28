
package net.sf.jaspercode.engine.processing;

import java.util.List;
import java.util.Map;

import net.sf.jaspercode.api.plugin.ProcessorLogMessage;
import net.sf.jaspercode.engine.JasperResources;

public abstract class ProcessableBase implements Processable {

	protected ProcessableContext ctx = null;
	protected ProcessorLog log = null;
	protected JasperResources jasperResources = null;
	protected Map<String,String> configs = null;
	protected int itemId;
	protected ProcessableChanges changes;

	protected ProcessableBase(int itemId, Map<String,String> configs, ProcessableContext ctx, JasperResources jasperResources) {
		this.itemId = itemId;
		this.configs = configs;
		this.ctx = ctx;
		this.jasperResources = jasperResources;
		this.log = new ProcessorLog("ProcessableLog");
	}

	public ProcessableChanges getChanges() {
		return changes;
	}

	public int getItemId() {
		return itemId;
	}

	public ProcessorLog getLog() {
		return log;
	}

	// Compares priority of components, for sorting and processing order
	@Override
	public int compareTo(Processable o) {
		if (o==null) return -1;

		int p = this.getPriority();
		int op = o.getPriority();
		if (p>op) return 1;
		else if (p==op) {
			int id = this.getItemId();
			int otherId = o.getItemId();
			if (id > otherId) return 1;
			else if (id == otherId) return 0;
			else return -1;
		}
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

	@Override
	public Map<String, String> getConfigs() {
		return configs;
	}

}

