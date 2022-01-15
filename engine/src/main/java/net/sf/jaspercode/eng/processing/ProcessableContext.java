package net.sf.jaspercode.eng.processing;

import java.util.Map;

import net.sf.jaspercode.api.SourceFile;
import net.sf.jaspercode.api.config.Component;
import net.sf.jaspercode.api.exception.JasperException;
import net.sf.jaspercode.api.resources.FileProcessor;
import net.sf.jaspercode.api.resources.FolderWatcher;
import net.sf.jaspercode.api.types.VariableType;
import net.sf.jaspercode.eng.files.ApplicationFolderImpl;
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
	void addComponent(Map<String,String> configs, Component comp, ApplicationFolderImpl folder);
	void addFolderWatcher(Map<String,String> configs, String folderPath, FolderWatcher folderWatcher, ApplicationFolderImpl folder);
	void addFileProcessor(Map<String,String> configs, String filePath, FileProcessor fileProcessor, ApplicationFolderImpl folder);

}

