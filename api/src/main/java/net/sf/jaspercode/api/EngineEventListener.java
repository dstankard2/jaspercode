package net.sf.jaspercode.api;

/**
 * A plugin that can listen for events in the engine.  Must be annotated with @Plugin to be found by the engine.
 * This is currently not supported.
 * @author DCS
 */
public interface EngineEventListener {

	void engineStart();
	
	void engineShutdown();

}

