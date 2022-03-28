package net.sf.jaspercode.api;

import java.util.List;

import net.sf.jaspercode.api.config.BuildComponent;
import net.sf.jaspercode.api.exception.JasperException;

public interface BuildComponentProcessor {

	void setBuildProcessorContext(BuildProcessorContext ctx) throws JasperException;
	void setBuildComponent(BuildComponent buildComponent) throws JasperException;
	void initialize() throws JasperException;
	BuildContext createBuildContext();
	void generateBuild() throws JasperException;
	List<Command> build();
	List<Command> clean();
	Class<? extends BuildComponent> getComponentClass();

}
