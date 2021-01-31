package net.sf.jaspercode.eng.processing;

import net.sf.jaspercode.api.SourceFile;
import net.sf.jaspercode.api.types.VariableType;
import net.sf.jaspercode.eng.files.UserFile;

public interface ProcessableContext {

	String getSystemAttribute(String name);
	VariableType getType(String lang, String name);
	UserFile getUserFile(String path);
	SourceFile getSourceFile(String path);
	Object getObject(String name);

}

