package net.sf.jaspercode.engine.processing;

// Represents an entry tracked by the engine
// An item may be a processable or may be used to create processables (folder watcher)
public interface Item {

	int getItemId();
	int getOriginatorId();
	String getName();
	void assignItemId(int itemId);

}

