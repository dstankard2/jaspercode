package net.sf.jaspercode.api;

import java.util.List;

import net.sf.jaspercode.api.config.BuildComponent;
import net.sf.jaspercode.api.exception.JasperException;

public interface BuildComponentProcessor {

	public Class<? extends BuildComponent> getComponentClass();
	public void initialize(BuildComponent component,BuildProcessorContext ctx) throws JasperException;
	public BuildContext createBuildContext();
	public void generateBuild() throws JasperException;
	public List<Command> build();

}
