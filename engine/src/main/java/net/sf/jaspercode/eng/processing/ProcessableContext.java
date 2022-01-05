package net.sf.jaspercode.eng.processing;

import net.sf.jaspercode.api.SourceFile;
import net.sf.jaspercode.api.config.Component;
import net.sf.jaspercode.api.exception.JasperException;
import net.sf.jaspercode.api.resources.FileProcessor;
import net.sf.jaspercode.api.resources.FolderWatcher;
import net.sf.jaspercode.api.types.VariableType;
import net.sf.jaspercode.eng.files.ComponentFile;
import net.sf.jaspercode.eng.files.UserFile;

public interface ProcessableContext {

	String getSystemAttribute(String name);
	VariableType getType(String lang, String name);
	UserFile getUserFile(String path);
	SourceFile getSourceFile(String path);
	Object getObject(String name);

	void addSystemAttribute(String name, String type) throws JasperException;
	void addVariableType(String lang, VariableType variableType);
	void setObject(String name, Object value);
	void addSourceFile(SourceFile src);
	void addComponent(ComponentFile originatorFile, Component comp);
	void addFolderWatcher(ComponentFile componentFile, String folderPath, FolderWatcher folderWatcher);
	void addFileProcessor(ComponentFile componentFile, String filePath, FileProcessor fileProcessor);

}

