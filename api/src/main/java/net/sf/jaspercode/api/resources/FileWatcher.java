package net.sf.jaspercode.api.resources;

import net.sf.jaspercode.api.ProcessorContext;
import net.sf.jaspercode.api.exception.JasperException;

public interface FileWatcher {

	// First method called in lifecycle
	void init(ProcessorContext ctx);

	int getPriority();

	void fileUpdated(ApplicationFile changedFile) throws JasperException;

	void processUpdates() throws JasperException;

	/**
	 * Temporary - should we remove this file watcher when the file is updated?
	 * @return
	 */
	boolean removeOnUnload();
	
}
