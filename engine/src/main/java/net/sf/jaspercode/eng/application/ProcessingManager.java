package net.sf.jaspercode.eng.application;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.jaspercode.api.SourceFile;
import net.sf.jaspercode.api.config.BuildComponent;
import net.sf.jaspercode.api.config.Component;
import net.sf.jaspercode.api.exception.JasperException;
import net.sf.jaspercode.api.langsupport.LanguageSupport;
import net.sf.jaspercode.api.resources.FileProcessor;
import net.sf.jaspercode.api.resources.FolderWatcher;
import net.sf.jaspercode.api.types.VariableType;
import net.sf.jaspercode.eng.BuildComponentPattern;
import net.sf.jaspercode.eng.ComponentPattern;
import net.sf.jaspercode.eng.EngineLanguages;
import net.sf.jaspercode.eng.JasperResources;
import net.sf.jaspercode.eng.exception.EngineException;
import net.sf.jaspercode.eng.files.ComponentFile;
import net.sf.jaspercode.eng.files.UserFile;
import net.sf.jaspercode.eng.processing.BuildComponentItem;
import net.sf.jaspercode.eng.processing.ComponentItem;
import net.sf.jaspercode.eng.processing.FileProcessable;
import net.sf.jaspercode.eng.processing.FolderWatcherProcessable;
import net.sf.jaspercode.eng.processing.ProcessableBase;
import net.sf.jaspercode.eng.processing.ProcessableContext;
import net.sf.jaspercode.eng.processing.ProcessorLog;
import net.sf.jaspercode.eng.EnginePatterns;

public class ProcessingManager implements ProcessableContext {

	private JasperResources jasperResources;
	private ProcessingContext ctx;
	private EnginePatterns patterns;
	private List<ProcessableBase> toProcess = new ArrayList<>();
	private EngineLanguages languages;
	private ProcessorLog appLog;

	private List<BuildComponentItem> buildsToInit = new ArrayList<>();
	private List<BuildComponentItem> buildsToProcess = new ArrayList<>();
	
	private Map<String,UserFile> userFiles = new HashMap<>();
	private Map<String,SourceFile> sourceFiles = new HashMap<>();
	private Map<String,String> attributes = new HashMap<>();
	private Map<String,Object> objects = new HashMap<>();
	private Map<String,Map<String,VariableType>> types = new HashMap<>();

	public ProcessingManager(ProcessingContext ctx, JasperResources jasperResources, EnginePatterns patterns, EngineLanguages languages, ProcessorLog appLog) {
		this.ctx = ctx;
		this.jasperResources = jasperResources;
		this.patterns = patterns;
		this.languages = languages;
		this.appLog = appLog;
	}

	public void process(Map<String,String> systemAttributes, List<UserFile> userFiles, List<ComponentFile> componentFiles) {
		// Clear out existing data
		this.userFiles.clear();
		this.sourceFiles.clear();
		this.objects.clear();
		this.types.clear();
		this.toProcess.clear();
		this.buildsToInit.clear();
		this.buildsToProcess.clear();
		
		this.attributes = systemAttributes;
		
		userFiles.forEach(uf -> this.userFiles.put(uf.getPath(), uf));
		componentFiles.forEach(cf -> {
			cf.getComponentSet().getComponent().forEach(comp -> addComponent(cf, comp));
		});
		
		boolean result = runProcessing();
		
		if (result) {
			this.userFiles.values().forEach(uf -> ctx.writeUserFile(uf));
			this.sourceFiles.values().forEach(sf -> ctx.writeSourceFile(sf));
		}
	}

	private void outputLog(ProcessorLog log) {
		log.outputToSystem();
		log.getMessages(true);
	}

	private boolean runProcessing() {
		// Initialize builds
		while(buildsToInit.size()>0) {
			BuildComponentItem b = buildsToInit.get(0);
			try {
				appLog.info("Initializing build "+b.getName());
				appLog.outputToSystem();
				b.init();
				outputLog(b.getLog());
				buildsToInit.remove(0);
				buildsToProcess.add(b);
			} catch(EngineException e) {
				outputLog(b.getLog());
				return false;
			}
		}
		// run toProcess
		while(toProcess.size()>0) {
			Collections.sort(toProcess);
			ProcessableBase proc = toProcess.get(0);
			toProcess.remove(0);
			appLog.info("Processing item "+proc.getName());
			appLog.outputToSystem();
			boolean result = proc.process();
			outputLog(proc.getLog());
			if (!result) {
				return false;
			}
		}
		
		// process builds
		while(buildsToProcess.size()>0) {
			BuildComponentItem b = buildsToProcess.get(0);
			buildsToProcess.remove(0);
			try {
				appLog.info("Processing build "+b.getName());
				appLog.outputToSystem();
				b.process();
				outputLog(b.getLog());
			} catch(EngineException e) {
				outputLog(b.getLog());
				return false;
			}
		}
		
		return true;
	}

	@Override
	public String getSystemAttribute(String name) {
		return attributes.get(name);
	}

	@Override
	public VariableType getType(String lang, String name) {
		VariableType ret = null;
		
		Map<String,VariableType> typesForLang = types.get(lang);
		if (typesForLang!=null) {
			ret = typesForLang.get(name);
		}

		return ret;
	}

	@Override
	public UserFile getUserFile(String path) {
		return userFiles.get(path);
	}

	@Override
	public SourceFile getSourceFile(String path) {
		return sourceFiles.get(path);
	}

	@Override
	public Object getObject(String name) {
		return objects.get(name);
	}

	@Override
	public void addSystemAttribute(String name, String type) throws JasperException {
		attributes.put(name, type);
	}

	@Override
	public void addVariableType(String lang, VariableType variableType) {
		if (types.get(lang)==null) {
			Map<String,VariableType> typesForLang = new HashMap<>();
			types.put(lang, typesForLang);
			LanguageSupport supp = languages.getLanguageSupport(lang);
			supp.getBaseVariableTypes().forEach(t -> typesForLang.put(t.getName(), t));
		}
		types.get(lang).put(variableType.getName(), variableType);
	}

	@Override
	public void setObject(String name, Object value) {
		objects.put(name, value);
	}

	@Override
	public void addSourceFile(SourceFile src) {
		sourceFiles.put(src.getPath(), src);
	}

	@Override
	public void addComponent(ComponentFile originatorFile, Component comp) {
		if (comp instanceof BuildComponent) {
			BuildComponent bc = (BuildComponent)comp;
			BuildComponentPattern pattern = patterns.getBuildPattern(bc.getClass());
			BuildComponentItem item = new BuildComponentItem(bc, pattern, jasperResources, this, originatorFile);
			buildsToInit.add(item);
		} else {
			ComponentPattern pattern = patterns.getPattern(comp.getClass());
			ComponentItem item = new ComponentItem(comp, this, originatorFile, jasperResources, pattern);
			toProcess.add(item);
		}
	}

	@Override
	public void addFolderWatcher(ComponentFile componentFile, String folderPath, FolderWatcher folderWatcher) {
		this.userFiles.entrySet().forEach(entry -> {
			if (entry.getKey().startsWith(folderPath)) {
				UserFile userFile = userFiles.get(entry.getKey());
				FolderWatcherProcessable proc = new FolderWatcherProcessable(this, componentFile, jasperResources, userFile, folderWatcher);
				toProcess.add(proc);
			}
		});
	}

	@Override
	public void addFileProcessor(ComponentFile componentFile, String filePath, FileProcessor fileProcessor) {
		this.userFiles.entrySet().forEach(entry -> {
			String path = entry.getKey();
			if (path.equals(filePath)) {
				UserFile userFile = entry.getValue();
				FileProcessable proc = new FileProcessable(this, componentFile, jasperResources, fileProcessor, userFile);
				toProcess.add(proc);
			}
		});
	}

}

