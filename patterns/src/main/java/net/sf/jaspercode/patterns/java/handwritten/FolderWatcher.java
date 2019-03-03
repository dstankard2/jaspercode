package net.sf.jaspercode.patterns.java.handwritten;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.sf.jaspercode.api.JasperException;
import net.sf.jaspercode.api.ProcessorContext;
import net.sf.jaspercode.api.resources.ApplicationFile;
import net.sf.jaspercode.api.resources.ApplicationFolder;
import net.sf.jaspercode.api.resources.ApplicationResource;
import net.sf.jaspercode.api.resources.ResourceWatcher;

public class FolderWatcher implements ResourceWatcher {
	private ProcessorContext ctx = null;
	private Set<String> watchedFiles = new HashSet<>();
	private String path = null;

	public FolderWatcher(String folderPath) {
		this.path = folderPath;
	}
	
	@Override
	public void init(ProcessorContext ctx) {
		this.ctx = ctx;
	}

	@Override
	public void process() throws JasperException {
		// TODO Auto-generated method stub
		ApplicationFolder folder = (ApplicationFolder)ctx.getResource(path);
		List<String> names = folder.getContentNames();
		for(String name : names) {
			ApplicationResource res = folder.getResource(name);
			checkResource(res);
		}
	}

	private void checkResource(ApplicationResource res) throws JasperException {
		String name = res.getName();
		if (name.endsWith(".java")) {
			if (res instanceof ApplicationFolder) {
				throw new JasperException("HandwrittenCode pattern found a *.java file which is a folder");
			}
			ApplicationFile file = (ApplicationFile)res;
			String path = file.getPath();
			if (!watchedFiles.contains(path)) {
				FileWatcher fileWatcher = new FileWatcher(path);
				watchedFiles.add(path);
				ctx.addResourceWatcher(fileWatcher, path);
			}
		} else if (res instanceof ApplicationFolder) {
			ApplicationFolder folder = (ApplicationFolder)res;
			for(String n : folder.getContentNames()) {
				ApplicationResource r = folder.getResource(n);
				checkResource(r);
			}
		}
	}
	
	@Override
	public int getPriority() {
		return 0;
	}

}
