package net.sf.jaspercode.engine;

import java.util.HashSet;
import java.util.Set;

import net.sf.jaspercode.api.annotation.Processor;
import net.sf.jaspercode.api.config.BuildComponent;
import net.sf.jaspercode.api.config.Component;
import net.sf.jaspercode.api.BuildComponentProcessor;
import net.sf.jaspercode.api.ComponentProcessor;

public class EnginePatterns {
	private Set<ComponentPattern> patterns = new HashSet<>();
	private Set<BuildComponentPattern> buildPatterns = new HashSet<>();
	PluginManager pluginManager = null;

	public EnginePatterns(PluginManager pluginManager) {
		this.pluginManager = pluginManager;
	}

	@SuppressWarnings("unchecked")
	public void findPatterns() throws EngineInitException {
		try {
			Set<Class<ComponentProcessor>> processorClasses = pluginManager.getPluginSubclasses(ComponentProcessor.class);
			Set<Class<Component>> componentClasses = pluginManager.getPluginSubclasses(Component.class);
			Set<Class<BuildComponentProcessor>> buildProcessors = pluginManager.getPluginSubclasses(BuildComponentProcessor.class);

			for(Class<?> comp : componentClasses) {
				if (BuildComponent.class.isAssignableFrom(comp)) {
					Class<? extends BuildComponentProcessor> processorClass = null;
					Class<? extends BuildComponent> bcomp = (Class<? extends BuildComponent>)comp;
					for(Class<?> cl : buildProcessors) {
						Class<? extends BuildComponentProcessor> buildProcClass = (Class<? extends BuildComponentProcessor>)cl;
						BuildComponentProcessor inst = buildProcClass.newInstance();
						if (inst.getComponentClass()==comp) {
							processorClass = buildProcClass;
							break;
						}
					}
					BuildComponentPattern p = new BuildComponentPattern(bcomp,processorClass);
					buildPatterns.add(p);
				} else {
					Class<? extends Component> componentClass = (Class<? extends Component>)comp;
					ComponentPattern p = new ComponentPattern(componentClass);
					for(Class<? extends ComponentProcessor> pr : processorClasses) {
						Processor processor = pr.getAnnotation(Processor.class);
						if (processor==null) {
							throw new EngineInitException("All Jasper plugins that implement ComponentProcessor must have the annotation @Processor, to mark what component class they process");
						}
						if (processor.componentClass()==componentClass) {
							RegisteredProcessor proc = new RegisteredProcessor(pr);
							p.getProcessors().add(proc);
						}
					}
					patterns.add(p);
				}
				
			}
		} catch(IllegalAccessException e) {
			throw new EngineInitException("Exception while locating component patterns",e);
		} catch(InstantiationException e) {
			throw new EngineInitException("Exception while locating component patterns",e);
		}
	}

	// Never returns null for a component that has been read from a XML file.
	public ComponentPattern getPattern(Class<? extends Component> compClass) {
		for(ComponentPattern pattern : patterns) {
			if (pattern.getPatternClass()==compClass) {
				return pattern;
			}
		}
		return null;
	}

	public BuildComponentPattern getBuildPattern(Class<? extends BuildComponent> buildComponent) {
		for(BuildComponentPattern pattern : buildPatterns) {
			if (pattern.getComponentClass()==buildComponent) {
				return pattern;
			}
		}
		return null;
	}

}
