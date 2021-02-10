package net.sf.jaspercode.engine.application;

import java.util.Map;

import net.sf.jaspercode.api.SourceFile;
import net.sf.jaspercode.engine.files.UserFile;

// Context within which application processing takes place.
// Mostly related to output file manipulation
public interface ProcessingContext {

	Map<String,UserFile> getUserFiles();
	void writeUserFile(UserFile userFile);
	void removeUserFile(UserFile userFile);
	SourceFile getSourceFile(String path);
	void writeSourceFile(SourceFile srcFile);
	void removeSourceFile(String path);

}

