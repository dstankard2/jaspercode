package net.sf.jaspercode.engine.processing;

import java.util.List;

import net.sf.jaspercode.api.plugin.ProcessorLogMessage;

// Replace Processable
public interface Processable extends Comparable<Processable> {

	int getOriginatorId();
	ProcessingState getState();
	int getPriority();
	List<ProcessorLogMessage> getMessages();
	String getName();
	boolean process();
	boolean commitChanges();
	//void rollbackChanges();
	void clearLogMessages();
	ProcessorLog getLog();

}
