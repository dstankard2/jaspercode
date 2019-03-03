package net.sf.jaspercode.patterns.maven;

import java.util.ArrayList;
import java.util.List;

public class PluginConfig {

	private String artifact = null;

	private List<String> dependencies = new ArrayList<>();
	
	private PropertySet configuration = new PropertySet("configuration");
	
	private List<ExecutionConfig> executions = new ArrayList<>();

	public PluginConfig(String artifact) {
		this.artifact = artifact;
	}
	
	public String getArtifact() {
		return artifact;
	}

	public void setArtifact(String artifact) {
		this.artifact = artifact;
	}

	public PropertySet getConfiguration() {
		return configuration;
	}

	public void setConfiguration(PropertySet configuration) {
		this.configuration = configuration;
	}

	public List<ExecutionConfig> getExecutions() {
		return executions;
	}

	public void setExecutions(List<ExecutionConfig> executions) {
		this.executions = executions;
	}

	public List<String> getDependencies() {
		return dependencies;
	}

	public void setDependencies(List<String> dependencies) {
		this.dependencies = dependencies;
	}

}

