package net.sf.jaspercode.patterns.maven;

import java.util.List;

import net.sf.jaspercode.api.ComponentProcessor;
import net.sf.jaspercode.api.ProcessorContext;
import net.sf.jaspercode.api.annotation.Plugin;
import net.sf.jaspercode.api.annotation.Processor;
import net.sf.jaspercode.api.config.Component;
import net.sf.jaspercode.api.exception.JasperException;
import net.sf.jaspercode.api.resources.ApplicationFile;
import net.sf.jaspercode.api.resources.ApplicationFolder;
import net.sf.jaspercode.api.resources.ApplicationResource;
import net.sf.jaspercode.patterns.xml.maven.SqlExecution;

@Plugin
@Processor(componentClass = SqlExecution.class)
public class SqlExecutionProcessor implements ComponentProcessor {

	private SqlExecution comp = null;
	private ProcessorContext ctx = null;
	
	@Override
	public void init(Component component, ProcessorContext ctx) {
		this.comp = (SqlExecution)component;
		this.ctx = ctx;
	}

	@Override
	public void process() throws JasperException {
		String id = comp.getId();
		String sqlDir = comp.getResource();
		MavenBuildContext mctx = MavenUtils.getMavenBuildContext(ctx);
		PluginConfig pl = mctx.getPlugin("org.codehaus.mojo:sql-maven-plugin");
		
		if (pl==null) {
			pl = new PluginConfig("org.codehaus.mojo:sql-maven-plugin:1.5");
			mctx.addPlugin(pl);
			// TODO: Remove hard-coded dependency on MySQL
			pl.getDependencies().add("mysql:mysql-connector-java:5.1.6");
			pl.getConfiguration().addProperty("driver", "com.mysql.jdbc.Driver");
			pl.getConfiguration().addProperty("onError", "continue");
			pl.getConfiguration().addProperty("url", comp.getJdbcUrl());
			pl.getConfiguration().addProperty("username", comp.getUsername());
			pl.getConfiguration().addProperty("password", comp.getPassword());
		}
		
		if (sqlDir.trim().length()==0) {
			throw new JasperException("Sql pattern requires XML attribute 'resource'");
		}
		
		ExecutionConfig exec = new ExecutionConfig();
		pl.getExecutions().add(exec);
		exec.setId(id);
		exec.getGoals().add("execute");
		exec.setPhase("package");
		mctx.addBuildPhase("package");
		exec.getConfiguration().addProperty("autocommit", "true");
		PropertyWithValueList files = exec.getConfiguration().addPropertyValueList("srcFiles", "srcFile");
		ApplicationResource res = ctx.getResource(sqlDir);
		if (res instanceof ApplicationFolder) {
			// Recursively search this folder and subfolder for SQL files
			ApplicationFolder folder = (ApplicationFolder)res;
			addFolder(folder,files,mctx,ctx);
		} else if (res instanceof ApplicationFile) {
			ApplicationFile file = (ApplicationFile)res;
			if (file.getName().endsWith(".sql")) {
				addFile(file.getPath(),files,mctx,ctx);
			}
		}
	}
	
	protected void addFolder(ApplicationFolder folder,PropertyWithValueList files,MavenBuildContext bctx,ProcessorContext ctx) {
		List<String> names = folder.getContentNames();
		//ctx.dependOnResource(folder.getPath());
		for(String name : names) {
			ApplicationResource res = folder.getResource(name);
			if (res instanceof ApplicationFile) {
				ApplicationFile file = (ApplicationFile)res;
				addFile(file.getPath(), files, bctx,ctx);
			} else if (res instanceof ApplicationFolder) {
				ApplicationFolder f = (ApplicationFolder)res;
				addFolder(f, files,bctx,ctx);
			}
		}
	}
	
	protected void addFile(String path,PropertyWithValueList files, MavenBuildContext bctx,ProcessorContext ctx) {
		if (path.endsWith(".sql")) {
			//ctx.dependOnResource(path);
			String buildBase = bctx.getApplicationFolderPath();
			String resourcePath = path.substring(buildBase.length());
			files.addValue(resourcePath);
		}
	}

}
