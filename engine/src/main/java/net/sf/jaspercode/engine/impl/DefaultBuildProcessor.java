package net.sf.jaspercode.engine.impl;

import java.util.List;

import net.sf.jaspercode.api.BuildComponentProcessor;
import net.sf.jaspercode.api.BuildContext;
import net.sf.jaspercode.api.BuildProcessorContext;
import net.sf.jaspercode.api.Command;
import net.sf.jaspercode.api.config.BuildComponent;
import net.sf.jaspercode.engine.definitions.ApplicationFolderImpl;

public class DefaultBuildProcessor implements BuildComponentProcessor {

	BuildProcessorContext ctx = null;
	
	public DefaultBuildProcessor(BuildProcessorContext ctx) {
		this.ctx = ctx;
	}
	
	@Override
	public Class<? extends BuildComponent> getComponentClass() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void initialize(BuildComponent component, BuildProcessorContext ctx) {
		this.ctx = ctx;
	}

	@Override
	public BuildContext createBuildContext() {
		ApplicationFolderImpl root = (ApplicationFolderImpl)ctx.getFolder();
		root = (ApplicationFolderImpl)root.getResource("/");
		return new DefaultBuildContext(root);
	}

	@Override
	public void generateBuild() {
	}

	@Override
	public List<Command> build() {
		this.ctx.getLog().warn("Default Build Context does not perform a build");
		return null;
	}

}
