package net.sf.jaspercode.engine.processing;

import net.sf.jaspercode.api.SourceFile;
import net.sf.jaspercode.api.types.VariableType;
import net.sf.jaspercode.engine.files.UserFile;

public interface ProcessableContext {

	String getSystemAttribute(String name);
	VariableType getType(String lang, String name);
	UserFile getUserFile(String path);
	SourceFile getSourceFile(String path);
	Object getObject(String name);

}

