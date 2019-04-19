package net.sf.jaspercode.engine.processing;

import java.util.List;

import net.sf.jaspercode.api.plugin.ProcessorLogMessage;
import net.sf.jaspercode.engine.definitions.ApplicationFolderImpl;

public interface Processable extends Comparable<Processable> {

	/**
	 * Priority rating for processing this item.  Items are sorted by priority in ascending order.  If 
	 * priority is < 0 then the component will not be processed.
	 * @return Priority rating.
	 */
	int getPriority();

	/**
	 * Returns the list of log messages for this processable
	 * @return messages added during pre/processing.
	 */
	List<ProcessorLogMessage> getMessages();

	/**
	 * Processes this component, handling exceptions.
	 * @return true if successful, false otherwise.
	 */
	boolean process();

	ApplicationFolderImpl getFolder();

	/**
	 * Perform any logic that should be done when the processable is removed from the system.
	 * @throws JasperException If there is a problem
	 */
	//void remove() throws JasperException;

	/**
	 * After a component is done processing, this method is invoked to commit application 
	 * changes to the processable's context.
	 * @return true if successful, false otherwise
	 */
	boolean commitChanges();

	/**
	 * If a processable is unloaded or removed, this method is invoked to revert application 
	 * changes to the processable's context.<br/>
	 * This method should always succeed.
	 */
	void rollbackChanges();
	
	/**
	 * Returns the state of this processable.
	 * @return
	 */
	ProcessingState getState();

	/**
	 * Returns the name of this processable, as understood by the engine.
	 * @return
	 */
	String getName();

}

