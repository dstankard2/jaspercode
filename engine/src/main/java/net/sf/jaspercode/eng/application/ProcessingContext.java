package net.sf.jaspercode.eng.application;

import net.sf.jaspercode.api.SourceFile;
import net.sf.jaspercode.eng.files.UserFile;

// Context within which application processing takes place.
// Mostly related to writing output files
public interface ProcessingContext {

	void writeUserFile(UserFile userFile);

	void writeSourceFile(SourceFile srcFile);

}

