package net.sf.jaspercode.engine.processing;

import java.lang.reflect.Method;
import java.util.Map;

import net.sf.jaspercode.api.JasperException;
import net.sf.jaspercode.api.annotation.ConfigProperty;
import net.sf.jaspercode.api.config.Component;
import net.sf.jaspercode.engine.ComponentPattern;
import net.sf.jaspercode.engine.definitions.ComponentFile;
import net.sf.jaspercode.engine.impl.ApplicationContextImpl;
import net.sf.jaspercode.engine.impl.ProcessorContextImpl;

/**
 * Processes a component and manages information about a component including:
 * * processors
 * * componentFile
 * * Dependencies on types, objects, attributes, sourceFiles
 * @author DCS
 */
public class ComponentEntry extends ProcessorContainerBase {

	private Component component;
	private PatternInstance patternInstance = null;
	private ProcessorContextImpl processorContext = null;
	private String name = null;

	@Override
	public int getPriority() {
		int priority = 0;
		if (component!=null) {
			priority = component.getPriority();
		}
		return priority;
	}

	public ComponentEntry(ComponentContainer mgr,Component component, ComponentPattern pattern,ComponentFile componentFile,ApplicationContextImpl applicationContext) {
		super(componentFile,mgr,applicationContext);
		this.component = component;
		this.processorContext = new ProcessorContextImpl(this,mgr,applicationContext);
		if (pattern!=null) {
			this.patternInstance = new PatternInstance(processorContext,component,pattern.getProcessors());
		}
		populateName();
	}
	
	@Override
	public String getProcessorName() {
		return component.getComponentName();
	}

	protected void populateName() {
		this.name = component.getComponentName();
	}

	protected void populateConfigProperties() throws JasperException {
		Class<?> cl = component.getClass();
		Map<String,String> configuration = folder.getProperties();
		
		for (Method m : cl.getMethods()) {
			if (m.isAnnotationPresent(ConfigProperty.class)) {
				if (m.getParameters().length!=1) {
					throw new JasperException("Couldn't invoke ConfigProperty setter "+m.toString());
				}
				ConfigProperty p = m.getAnnotation(ConfigProperty.class);
				String name = p.name();
				Object value = configuration.get(name);
				if ((m.getParameters()[0].getType()==Integer.class) && (value!=null)) {
					try {
						value = Integer.parseInt(value.toString());
					} catch(NumberFormatException e) {
						throw new JasperException("Couldn't read config property '"+name+"' - value '"+value+"' was not a number");
					}
				}
				boolean required = p.required();
				if ((value==null) && (required)) {
					throw new JasperException("Component is missing required configuration '"+name+"'");
				}
				else if (value==null) {
					this.getLog().warn("Could not find optional configuration parameter '"+name+"'");
				}
				if (value!=null) {
					try {
					m.invoke(component, value);
					} catch(Exception e) {
						throw new JasperException("Couldn't set configuration property '"+name+"'",e);
					}
				}
			}
		}
	}

	public Map<String,String> getConfiguration() {
		return folder.getProperties();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ProcessorContextImpl getProcessorContext() {
		return processorContext;
	}

	public void setProcessorContext(ProcessorContextImpl processorContext) {
		this.processorContext = processorContext;
	}

	public Component getComponent() {
		return component;
	}
	
	public void setComponent(Component component) {
		this.component = component;
	}
	
	@Override
	public void process() throws JasperException {
		getLog().notifyStartProcessing();
		populateConfigProperties();
		if (patternInstance!=null) {
			patternInstance.process();
			this.getComponentFile().getProcessedComponents().add(this);
		} else {
			getLog().notifyNoProcessor();
		}
		getLog().notifyFinishProcessing();
	}
	
}
