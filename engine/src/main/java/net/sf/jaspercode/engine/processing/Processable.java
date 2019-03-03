package net.sf.jaspercode.engine.processing;

import net.sf.jaspercode.api.JasperException;

public interface Processable {

	/**
	 * Priority rating for processing this item.  Items are sorted by priority in ascending order.  If 
	 * priority is < 0 then the component will not be processed.
	 * @return Priority rating.
	 */
	int getPriority();
	
	/**
	 * Process this component.
	 * @throws JasperException
	 */
	void process() throws JasperException;
	
}
