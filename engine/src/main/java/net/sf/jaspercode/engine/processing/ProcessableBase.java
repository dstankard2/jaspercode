
package net.sf.jaspercode.engine.processing;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.tuple.Pair;

import net.sf.jaspercode.api.BuildContext;
import net.sf.jaspercode.api.SourceFile;
import net.sf.jaspercode.api.config.Component;
import net.sf.jaspercode.api.exception.JasperException;
import net.sf.jaspercode.api.plugin.ProcessorLogMessage;
import net.sf.jaspercode.api.resources.FileProcessor;
import net.sf.jaspercode.api.resources.FolderWatcher;
import net.sf.jaspercode.api.types.VariableType;
import net.sf.jaspercode.engine.application.JasperResources;
import net.sf.jaspercode.engine.application.ProcessingContext;
import net.sf.jaspercode.engine.definitions.ApplicationFolderImpl;
import net.sf.jaspercode.engine.definitions.ComponentFile;

public abstract class ProcessableBase extends ProcessableContextBase implements Processable {

	protected int originatorId = 0;
	protected int id = 0;
	protected ProcessingContext ctx = null;
	protected ProcessorLog log = null;
	protected ApplicationFolderImpl folder = null;
	protected Map<String,String> configs = null;
	//protected List<Pair<String,VariableType>> typesAdded = new ArrayList<>();
	protected ProcessingState state = ProcessingState.TO_PROCESS;
	protected ComponentFile originatorFile = null;

	protected ProcessableBase(int id, ProcessingContext ctx,ComponentFile originatorFile,
			Map<String,String> configs,int originatorId,JasperResources jasperResources) {
		super(jasperResources);
		this.originatorId = originatorId;
		this.id = id;
		this.ctx = ctx;
		this.originatorFile = originatorFile;
		this.folder = originatorFile.getFolder();
		this.configs = configs;
	}

	@Override
	public ProcessorLog getLog() {
		return log;
	}

	// Compares priority of components, for sorting
	@Override
	public int compareTo(Processable o) {
		if (o==null) return -1;

		int p = this.getPriority();
		int op = o.getPriority();
		if (p>op) return 1;
		else if (p==op) return 0;
		else return -1;
	}
	
	@Override
	public void clearLogMessages() {
		if (this.log!=null) {
			this.log.getMessages(true);
		}
	}
	@Override
	public int getOriginatorId() {
		return originatorId;
	}

	@Override
	public void addVariableType(String lang, VariableType variableType) {
		for(Pair<String,VariableType> p : typesOriginated) {
			if ((p.getValue()==variableType) && (p.getLeft().equals(lang))) {
				return;
			}
		}
		this.typesOriginated.add(Pair.of(lang, variableType));
	}

	@Override
	public String getConfigurationProperty(String name) {
		return configs.get(name);
	}

	@Override
	public ProcessingState getState() {
		return this.state;
	}

	@Override
	public abstract int getPriority();

	@Override
	public List<ProcessorLogMessage> getMessages() {
		return log.getMessages(false);
	}

	@Override
	public abstract String getName();

	//@Override
	//public abstract boolean preprocess();

	@Override
	public abstract boolean process();

	@Override
	public boolean commitChanges() {
		boolean ret = true;

		// Commit components added
		for(Component comp : componentsAdded) {
			try {
				ctx.addComponent(id, comp, originatorFile);
			} catch(JasperException e) {
				state = ProcessingState.ERROR;
				return false;
			}
		}
		componentsAdded.clear();

		// Commit object changes
		for(Entry<String,Object> entry : objects.entrySet()) {
			ctx.originateObject(id, entry.getKey());
			ctx.setObject(id, entry.getKey(), entry.getValue());
		}
		objects.clear();

		// Commit system attribute changes
		for(Entry<String,String> entry : this.attributesAdded.entrySet()) {
			ctx.originateSystemAttribute(id, entry.getKey(), entry.getValue());
		}
		for(String s : attributeDependencies) {
			ctx.dependOnSystemAttribute(id, s);
		}
		
		// Commit added variable types
		for(Pair<String,VariableType> e : this.typesOriginated) {
			ctx.originateType(id, e.getKey(), e.getValue());
		}
		typesOriginated.clear();
		
		for(Pair<String,VariableType> e : this.typeDependencies) {
			ctx.dependOnType(id, e.getKey(), e.getRight().getName());
		}
		typeDependencies.clear();

		for(Pair<String,FolderWatcher> entry : this.folderWatchersAdded) {
			ctx.addFolderWatcher(id, originatorFile, entry.getKey(), entry.getRight());
		}
		folderWatchersAdded.clear();
		
		for(Pair<String,FileProcessor> proc : this.fileProcessorsAdded) {
			ctx.addFileProcessor(id, originatorFile, proc.getKey(), proc.getRight());
		}
		fileProcessorsAdded.clear();
		
		for(SourceFile src : this.sourceFiles) {
			ctx.saveSourceFile(id, src);
		}
		
		return ret;
	}

	@Override
	public SourceFile getSourceFile(String path) {
		for(SourceFile src : sourceFiles) {
			if (src.getPath().equals(path)) {
				return src;
			}
		}
		SourceFile src = ctx.getSourceFile(path);
		if (src!=null) {
			sourceFiles.add(src);
		}
		return src;
	}

	@Override
	public Object getObject(String objectName) {
		Object ob = ctx.getObject(id, objectName);
		this.objects.put(objectName, ob);
		return ob;
	}

	@Override
	public String getSystemAttribute(String name) {
		return ctx.getSystemAttribute(name);
	}

	// TODO: Implement
	@Override
	public void dependOnType(String lang, String name, BuildContext buildCtx) {
		VariableType t = getVariableType(lang,name);
		if (t.getBuildContext()!=buildCtx) {
			buildCtx.addDependency(t.getBuildContext());
		}
		this.typeDependencies.add(Pair.of(lang, t));
	}

	@Override
	public VariableType getVariableType(String language, String typeName) {
		for(Pair<String,VariableType> t : this.typesOriginated) {
			if ((t.getKey().equals(language)) && (t.getValue().getName().equals(typeName))) {
				return t.getRight();
			}
		}
		return ctx.getVariableType(language, typeName);
	}

}

