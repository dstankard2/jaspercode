package net.sf.jaspercode.engine.processing;

import java.util.List;
import java.util.Map;

import net.sf.jaspercode.api.plugin.ProcessorLogMessage;
import net.sf.jaspercode.engine.files.ApplicationFolderImpl;

// A processable object is an object that may be processed to change the state of the application
public interface Processable extends Comparable<Processable> {

	// The item that this processable came from
	int getItemId();
	
	// Processing priority.  Lower numbers are handled first
	int getPriority();
	
	// Log messages for this processable
	List<ProcessorLogMessage> getMessages();

	// Name of this processable
	String getName();
	
	// Changes related to this processable after processing is complete
	ProcessableChanges getChanges();

	// Run processing.  If failed, return false.  Otherwise return true
	boolean process();
	
	// Clear existing log messages
	void clearLogMessages();
	
	// Get the logger for this processable
	ProcessorLog getLog();

	Map<String,String> getConfigs();
	
	ApplicationFolderImpl getFolder();
	
}

