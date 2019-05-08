package net.sf.jaspercode.engine.processing;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

import net.sf.jaspercode.api.ApplicationContext;
import net.sf.jaspercode.api.annotation.ConfigProperty;
import net.sf.jaspercode.api.resources.ApplicationFile;
import net.sf.jaspercode.api.resources.FolderWatcher;
import net.sf.jaspercode.engine.application.ProcessingContext;
import net.sf.jaspercode.engine.definitions.ComponentFile;
import net.sf.jaspercode.engine.exception.PreprocessingException;

/**
 * This represents a resource watcher that needs to be executed for a watched resource path.
 * @author DCS
 */
public class FolderWatcherEntry extends ProcessableBase {

	private FolderWatcher folderWatcher = null;
	private FolderWatcherRecord record = null;
	private String path = null;
	private ApplicationFile applicationFile = null;

	public FolderWatcherEntry(String path, ApplicationContext applicationContext, ComponentFile componentFile,ProcessingContext processingContext, int id, FolderWatcher folderWatcher,FolderWatcherRecord record, ApplicationFile applicationFile) {
		super(applicationContext, componentFile, processingContext, id, path, new HashMap<String,String>());
		this.folderWatcher = folderWatcher;
		this.applicationFile = applicationFile;
		this.record = record;
		this.path = path;
		this.log = new ProcessorLog(getName());
		this.processorContext = new ProcessorContextImpl(componentFile.getFolder(), this, log);
		this.folderWatcher.init(processorContext);
	}

	public String getPath() {
		return path;
	}

	@Override
	public int getPriority() {
		return folderWatcher.getPriority();
	}

	public void preprocess() throws PreprocessingException {
		Class<?> compClass = folderWatcher.getClass();
		Method[] methods = compClass.getMethods();
		
		this.state = ProcessingState.PREPROCESSING;
		for(Method method : methods) {
			ConfigProperty prop = method.getDeclaredAnnotation(ConfigProperty.class);
			if (prop!=null) {
				try {
					Object value = super.handleConfigProperty(prop, method.getParameterTypes());
					method.invoke(folderWatcher, value);
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
			folderWatcher.process(applicationFile);
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
		return "FolderWatcher["+getPath()+"] for file '"+applicationFile.getPath()+"'";
	}

}

