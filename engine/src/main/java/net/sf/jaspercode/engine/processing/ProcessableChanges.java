package net.sf.jaspercode.engine.processing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;

import lombok.Data;
import net.sf.jaspercode.api.SourceFile;
import net.sf.jaspercode.api.config.Component;
import net.sf.jaspercode.api.resources.FileProcessor;
import net.sf.jaspercode.api.resources.FolderWatcher;
import net.sf.jaspercode.api.types.VariableType;

/*
 * TODO: It's possible this mechanism might not be necessary, 
 * and it may be ok to just write changes to the application data as they occur
 */
@Data
public class ProcessableChanges {

	protected int itemId = 0;
	
	protected List<SourceFile> sourceFiles = new ArrayList<>();
	protected List<SourceFile> sourceFilesChanged = new ArrayList<>();

	protected List<Pair<String,VariableType>> typeDependencies = new ArrayList<>();
	protected List<Pair<String,VariableType>> typesModified = new ArrayList<>();

	protected Map<String,Object> objects = new HashMap<>();
	
	protected Map<String,String> attributesAdded = new HashMap<>();
	protected List<String> attributesOriginated = new ArrayList<>();
	protected List<String> attributeDependencies = new ArrayList<>();

	protected List<Pair<String,FolderWatcher>> folderWatchersAdded = new ArrayList<>();
	protected List<Pair<String,FileProcessor>> fileProcessorsAdded = new ArrayList<>();
	
	protected List<Component> componentsAdded = new ArrayList<>();

	public ProcessableChanges(int itemId) {
		this.itemId = itemId;
	}

}

