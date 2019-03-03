package net.sf.jaspercode.engine.definitions;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import net.sf.jaspercode.api.config.ComponentSet;
import net.sf.jaspercode.api.config.Property;
import net.sf.jaspercode.engine.processing.ProcessorContainerBase;

public class ComponentFile implements WatchedResource {

	public ComponentFile(ComponentSet componentSet,File file,ApplicationFolderImpl folder) {
		this.folder = folder;
		if (file!=null) {
			this.fileName = file.getName();
		} else {
			this.fileName = "/none/no_file.xml";
		}
		this.componentSet = componentSet;
	}

	private ComponentSet componentSet = null;
	private String fileName;
	private List<Property> properties = new ArrayList<>();
	private long lastModified = Long.MIN_VALUE;
	private ApplicationFolderImpl folder = null;
	private List<ProcessorContainerBase> processedComponents = new ArrayList<>();
	private ProcessorContainerBase failedComponent = null;
	private List<ProcessorContainerBase> unprocessedComponents = new ArrayList<>();

	

	public ComponentSet getComponentSet() {
		return componentSet;
	}
	public void setComponentSet(ComponentSet componentSet) {
		this.componentSet = componentSet;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public List<Property> getProperties() {
		return properties;
	}
	public void setProperties(List<Property> properties) {
		this.properties = properties;
	}
	public ApplicationFolderImpl getFolder() {
		return folder;
	}
	public void setFolder(ApplicationFolderImpl folder) {
		this.folder = folder;
	}
	public List<ProcessorContainerBase> getProcessedComponents() {
		return processedComponents;
	}
	public void setProcessedComponents(List<ProcessorContainerBase> processedComponents) {
		this.processedComponents = processedComponents;
	}
	public ProcessorContainerBase getFailedComponent() {
		return failedComponent;
	}
	public void setFailedComponent(ProcessorContainerBase failedComponent) {
		this.failedComponent = failedComponent;
	}
	public List<ProcessorContainerBase> getUnprocessedComponents() {
		return unprocessedComponents;
	}
	public void setUnprocessedComponents(List<ProcessorContainerBase> unprocessedComponents) {
		this.unprocessedComponents = unprocessedComponents;
	}



	@Override
	public String getName() {
		return getFileName();
	}

	@Override
	public String getPath() {
		return folder.getPath()+'/'+getFileName();
	}

	@Override
	public long getLastModified() {
		return lastModified;
	}

}

