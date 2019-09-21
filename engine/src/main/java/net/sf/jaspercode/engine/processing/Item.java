package net.sf.jaspercode.engine.processing;

// Represents an entry tracked by the engine
public interface Item {

	int getId();
	int getOriginatorId();
	String getName();
	//boolean commit();
	//void unload();
	//ProcessingState getState();

}

