package net.sf.jaspercode.engine.processing;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

import net.sf.jaspercode.api.ApplicationContext;
import net.sf.jaspercode.api.annotation.ConfigProperty;
import net.sf.jaspercode.api.resources.ResourceWatcher;
import net.sf.jaspercode.engine.application.ProcessingContext;
import net.sf.jaspercode.engine.definitions.ComponentFile;
import net.sf.jaspercode.engine.exception.PreprocessingException;

/**
 * This represents a resource watcher that needs to be executed for a watched resource path.
 * @author DCS
 */
public class ResourceWatcherEntry extends ProcessableBase {

	private ResourceWatcher resourceWatcher = null;
	private ResourceWatcherRecord record = null;

	public ResourceWatcherEntry(ApplicationContext applicationContext, ComponentFile componentFile,ProcessingContext processingContext, int id, ResourceWatcher resourceWatcher,ResourceWatcherRecord record) {
		super(applicationContext, componentFile, processingContext, id, resourceWatcher.getPath(), new HashMap<String,String>());
		this.resourceWatcher = resourceWatcher;
		resourceWatcher.init(processorContext);
		this.record = record;
		this.log = new ProcessorLog(getName());
	}

	public String getPath() {
		return resourceWatcher.getPath();
	}

	@Override
	public int getPriority() {
		return resourceWatcher.getPriority();
	}

	public void preprocess() throws PreprocessingException {
		Class<?> compClass = resourceWatcher.getClass();
		Method[] methods = compClass.getMethods();
		
		this.state = ProcessingState.PREPROCESSING;
		for(Method method : methods) {
			ConfigProperty prop = method.getDeclaredAnnotation(ConfigProperty.class);
			if (prop!=null) {
				try {
					Object value = super.handleConfigProperty(prop, method.getParameterTypes());
					method.invoke(resourceWatcher, value);
				} catch(InvocationTargetException | IllegalAccessException e) {
					throw new PreprocessingException("Couldn't invoke ConfigProperty annotation on "+compClass.getCanonicalName(), e);
				}
			}
		}
		this.state = ProcessingState.PREPROCESSED;
	}

	@Override
	public boolean process() {
		boolean ret = true;
		
		this.state = ProcessingState.PROCESSING;
		try {
			resourceWatcher.process();
			this.state = ProcessingState.COMPLETE;
			record.setLastRun(System.currentTimeMillis());
		} catch(Exception e) {
			this.state = ProcessingState.ERROR;
			e.printStackTrace();
		}
		
		return ret;
	}

	@Override
	public String getName() {
		return "ResourceWatcher["+getPath()+"]";
	}

}

