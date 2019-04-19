package net.sf.jaspercode.engine.processing;

/**
 * An object that is tracked by the engine.
 * @author DCS
 */
public interface Tracked {

	/**
	 * Returns the processing manager's internal ID for this processable component
	 * @return id of this component.
	 */
	int getId();
	
	String getName();
	
	/**
	 * Returns the internal ID of the tracked item that added this item.
	 * Returns x < 1 if this item has no originator
	 * @return
	 */
	int getOriginatorId();

}

