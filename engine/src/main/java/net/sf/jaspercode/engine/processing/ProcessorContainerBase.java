package net.sf.jaspercode.engine.processing;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.sf.jaspercode.api.JasperException;
import net.sf.jaspercode.api.resources.ResourceWatcher;
import net.sf.jaspercode.api.types.VariableType;
import net.sf.jaspercode.engine.definitions.ApplicationFolderImpl;
import net.sf.jaspercode.engine.definitions.ComponentFile;
import net.sf.jaspercode.engine.impl.ApplicationContextImpl;
import net.sf.jaspercode.engine.impl.ProcessorLogImpl;

public abstract class ProcessorContainerBase implements Comparable<ProcessorContainerBase>,Processable {

	private Set<ResourceWatcherEntry> resourceWatchers = new HashSet<>();
	
	// This component's dependencies
	protected Set<String> attributes = new HashSet<>();
	protected Set<String> types = new HashSet<>();
	protected Set<String> objects = new HashSet<>();
	protected Set<String> resources = new HashSet<>();
	// End of dependencies
	
	// Objects originated by this component
	protected Set<String> attributesOriginated = new HashSet<>();
	protected Set<String> variableTypesOriginated = new HashSet<>();
	protected Set<String> objectsOriginated = new HashSet<>();
	protected Set<String> sourceFilesOriginated = new HashSet<>();
	
	ComponentContainer mgr = null;
	protected ApplicationFolderImpl folder = null;
	protected ProcessorLogImpl log = null;
	protected ComponentFile originator = null;
	protected ApplicationContextImpl applicationContext = null;

	public ProcessorContainerBase(ComponentFile originator,ComponentContainer mgr,ApplicationContextImpl applicationContext) {
		this.folder = originator.getFolder();
		this.originator = originator;
		this.mgr = mgr;
		this.applicationContext = applicationContext;
	}
	
	// Compares priority of components, for sorting
	@Override
	public int compareTo(ProcessorContainerBase o) {
		if (o==null) return -1;

		int p = this.getPriority();
		int op = o.getPriority();
		if (p>op) return 1;
		else if (p==op) return 0;
		else return -1;
	}

	public void originateAttribute(String name) {
		attributesOriginated.add(name);
	}
	public void originateVariableType(String name) {
		variableTypesOriginated.add(name);
	}
	public void originateObject(String name) {
		objectsOriginated.add(name);
	}
	public void originateSourceFile(String path) {
		sourceFilesOriginated.add(path);
	}

	public void dependOnObject(String key) {
		objects.add(key);
	}
	public void dependOnVariableType(String name, VariableType type) throws JasperException {
		types.add(name);
		this.folder.getBuildContext(mgr).addDependency(type.getBuildContext());
	}
	public void dependOnResource(String path) {
		resources.add(path);
	}
	public void dependOnSystemAttribute(String name) {
		attributes.add(name);
	}
	public boolean isDependentOnType(String name) {
		if (types.contains(name)) return true;
		return false;
	}
	public boolean isDependantOnAttribute(String name) {
		if (attributes.contains(name)) return true;
		return false;
	}
	public Set<String> getAttributes() {
		return attributes;
	}
	public Set<String> getTypes() {
		return types;
	}
	public Map<String,String> getConfiguration() {
		return this.folder.getProperties();
	}
	
	public ApplicationFolderImpl getFolder() {
		return folder;
	}

	public ProcessorLogImpl getLog() {
		if (this.log==null) {
			this.log = new ProcessorLogImpl(getProcessorName(),folder.getLogLevel());
		}
		return this.log;
	}

	public ComponentFile getComponentFile() {
		return originator;
	}

	public void addResourceWatcher(ResourceWatcher watcher, String path) {
		ResourceWatcherEntry e = new ResourceWatcherEntry(watcher,path,mgr,originator,applicationContext);
		this.resourceWatchers.add(e);
	}
	
	public Set<ResourceWatcherEntry> getResourceWatchers() {
		return this.resourceWatchers;
	}

	public abstract String getProcessorName();

	public abstract void process() throws JasperException;
	
}

