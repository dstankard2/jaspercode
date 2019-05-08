package net.sf.jaspercode.plugins.monitor;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import net.sf.jaspercode.api.annotation.Plugin;
import net.sf.jaspercode.api.exception.JasperException;
import net.sf.jaspercode.api.plugin.ApplicationPlugin;
import net.sf.jaspercode.api.plugin.PluginContext;
import net.sf.jaspercode.api.snapshot.ApplicationSnapshot;
import net.sf.jaspercode.api.snapshot.ItemSnapshot;
import net.sf.jaspercode.api.snapshot.SourceFileSnapshot;
import net.sf.jaspercode.api.snapshot.SystemAttributeSnapshot;

@Plugin
public class MonitorPlugin implements ApplicationPlugin {
	private String applicationName = null;
	private PluginContext ctx = null;
	
	public MonitorPlugin() {
	}

	@Override
	public String getPluginName() {
		return "monitor";
	}

	@Override
	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}

	@Override
	public void setPluginContext(PluginContext ctx) {
		this.ctx = ctx;
	}

	@Override
	public void scanStart() {
		
	}

	private ItemSnapshot find(int id,ApplicationSnapshot snapshot) {
		for(ItemSnapshot item : snapshot.getItems()) {
			if (item.getId()==id) return item;
		}
		return null;
	}

	private void outputsourceFiles(FileWriter writer, ApplicationSnapshot snapshot) throws IOException {
		writer.write("Source Files:\n");
		for(SourceFileSnapshot src : snapshot.getSourceFiles()) {
			writer.write("+- "+src.getPath()+"\n");
		}
	}

	private void outputComponents(FileWriter writer, ApplicationSnapshot snapshot) throws IOException {
		writer.write("Items:\n");
	}

	private void outputSystemAttributes(FileWriter writer, ApplicationSnapshot snapshot) throws IOException {
		writer.write("System Attributes\n");
		for(SystemAttributeSnapshot a : snapshot.getSystemAttributes()) {
			writer.write("+- "+a.getName()+" = "+a.getType()+"\n");
			if (a.getOriginators().size()>0) {
				writer.write("|  +- Originators:\n");
				for(Integer i : a.getOriginators()) {
					ItemSnapshot s = find(i, snapshot);
					if (s!=null) {
						writer.write("|     +- "+s.getName()+"\n");
					}
				}
			}
			if (a.getDependants().size()>0) {
				writer.write("|  +- Dependants:\n");
				for(Integer i : a.getDependants()) {
					ItemSnapshot s = find(i, snapshot);
					if (s!=null) {
						writer.write("|     +- "+s.getName()+"\n");
					}
				}
			}
		}
	}
	
	@Override
	public void scanComplete(ApplicationSnapshot snapshot) throws JasperException {
		String location = ctx.getEngineProperty("applicationPlugin.monitor.output", null);
		boolean showAttributes = ctx.getBooleanEngineProperty("applicationPlugin.monitor.showAttributes", false);
		boolean showSourceFiles = ctx.getBooleanEngineProperty("applicationPlugin.monitor.showSourceFiles", false);
		boolean showItems = ctx.getBooleanEngineProperty("applicationPlugin.monitor.showItems", false);
		
		if (location==null) {
			throw new JasperException("Monitor plugin requires engine property 'applicationPlugin.monitor.output' to be location of output file");
		}
		File file = new File(location);
		if (!file.exists()) {
			file.getParentFile().mkdirs();
			try {
			file.createNewFile();
			} catch(IOException e) {
				throw new JasperException("Couldn't create file '"+location+"'", e);
			}
		} else {
			file.delete();
		}
		
		try (FileWriter writer = new FileWriter(file)) {
			if (showSourceFiles) {
				outputsourceFiles(writer, snapshot);
			}
			if (showAttributes) {
				outputSystemAttributes(writer,snapshot);
			}
			writer.flush();
		} catch(IOException e) {
			throw new JasperException("Monitor plugin couldn't write file '"+location+"'",e);
		}
	}

}

