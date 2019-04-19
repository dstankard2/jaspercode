package net.sf.jaspercode.engine.application;

/**
 * Manages the outputManager's calls to the applicationManager, to simplify logic in the applicationManager.
 * @author DCS
 *
 */
public class OutputContext {

	private ApplicationManager applicationManager = null;
	
	public OutputContext(ApplicationManager applicationManager) {
		this.applicationManager = applicationManager;
	}

}

