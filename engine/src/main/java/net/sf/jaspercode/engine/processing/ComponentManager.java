package net.sf.jaspercode.engine.processing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import net.sf.jaspercode.api.JasperException;
import net.sf.jaspercode.api.config.BuildComponent;
import net.sf.jaspercode.api.config.Component;
import net.sf.jaspercode.api.config.ComponentSet;
import net.sf.jaspercode.engine.BuildComponentPattern;
import net.sf.jaspercode.engine.ComponentPattern;
import net.sf.jaspercode.engine.EnginePatterns;
import net.sf.jaspercode.engine.definitions.ComponentFile;
import net.sf.jaspercode.engine.impl.ApplicationContextImpl;

/**
 * Creates and manages ComponentEntry objects
 * @author DCS
 */
public class ComponentManager {
	private ComponentContainer componentContainer = null;
	private List<ComponentFile> componentFiles = new ArrayList<>();
	private List<ComponentFile> newComponentFiles = new ArrayList<>();
	//private List<ComponentEntry> addedComps = new ArrayList<>();
	//private List<ComponentEntry> allComponents = new ArrayList<>();
	private EnginePatterns enginePatterns = null;
	private List<BuildComponentEntry> buildComponentsToProcess = new ArrayList<>();
	private List<ProcessorContainerBase> toProcess = new ArrayList<>();
	private ApplicationContextImpl applicationContext = null;

	public ComponentManager(ComponentContainer applicationData,EnginePatterns patterns,ApplicationContextImpl applicationContext) {
		this.componentContainer = applicationData;
		this.enginePatterns = patterns;
		this.applicationContext = applicationContext;
	}
	
	public void removeComponentFile(String path) {
		ComponentFile toRemove = null;
		for(ComponentFile f : componentFiles) {
			if (f.getPath().equals(path)) {
				toRemove = f;
				break;
			}
		}
		componentFiles.remove(toRemove);
		System.out.println("TODO: Remove components in file '"+path+"'");
	}
	
	public void removeComponent(Component comp) {
		
	}
	
	public void addComponentFile(ComponentFile componentFile) {
		newComponentFiles.add(componentFile);
	}
	
	public void processComponentFilesAdded() throws EngineRuntimeException,JasperException {
		
		for(ComponentFile f : newComponentFiles) {
			ComponentSet s = f.getComponentSet();
			for(Component comp : s.getComponent()) {
				if (comp instanceof BuildComponent) {
					BuildComponent bcomp = (BuildComponent)comp;
					BuildComponentPattern pattern = enginePatterns.getBuildPattern(bcomp.getClass());
					BuildComponentEntry e = new BuildComponentEntry(f,bcomp,componentContainer,pattern,applicationContext);
					buildComponentsToProcess.add(e);
				} else {
					ComponentPattern pattern = enginePatterns.getPattern(comp.getClass());
					ComponentEntry e = new ComponentEntry(componentContainer,comp,pattern,f,applicationContext);
					toProcess.add(e);
				}
			}
		}
		processComponentsAdded();
	}

	protected void processComponentsAdded() throws JasperException {
		List<BuildComponentEntry> buildCompsInProcessing = new ArrayList<>();

		while(buildComponentsToProcess.size()>0) {
			BuildComponentEntry e = buildComponentsToProcess.get(0);
			buildCompsInProcessing.add(e);
			buildComponentsToProcess.remove(0);
			e.initialize();
		}
		Collections.sort(toProcess);
		while(toProcess.size()>0) {
			ProcessorContainerBase e = toProcess.get(0);
			toProcess.remove(e);
			int previousSize = toProcess.size();
			if (e.getPriority()>=0) {
				e.process();
				e.getComponentFile().getProcessedComponents().add(e);
				if (toProcess.size()!=previousSize) {
					Collections.sort(toProcess);
				}
			}
			Set<ResourceWatcherEntry> watchers = e.getResourceWatchers();
			if (watchers.size()>0) {
				toProcess.addAll(watchers);
				Collections.sort(toProcess);
			}
		}
		
		for(BuildComponentEntry e : buildCompsInProcessing) {
			e.process();
		}
		
	}

	public void addComponent(Component componentBase,ComponentFile originator) {
		
		if (componentBase instanceof BuildComponent) {
			throw new RuntimeException("You cannot dynamically add a build component - it must be in a component file");
		}
		
		ComponentEntry entry = new ComponentEntry(componentContainer,componentBase,enginePatterns.getPattern(componentBase.getClass()),originator,applicationContext);
		toProcess.add(entry);
	}
	
}
