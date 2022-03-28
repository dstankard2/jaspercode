package net.sf.jaspercode.engine.processing;

import net.sf.jaspercode.api.SourceFile;
import net.sf.jaspercode.api.types.VariableType;
import net.sf.jaspercode.engine.files.UserFile;

// ProcessableContext is the source of application information, for a processable
// TODO: Determine if we can just write changes instead of building a changes object.  Then this API would have methods to write changes
public interface ProcessableContext {

	String getSystemAttribute(String name);

	VariableType getType(String lang, String name);

	void modifyType(String lang, VariableType type);

	UserFile getUserFile(String path);

	SourceFile getSourceFile(String path);

	Object getObject(String name);

}

