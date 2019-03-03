package net.sf.jaspercode.patterns.maven;

import java.util.ArrayList;
import java.util.List;

public class ExecutionConfig {

	private String phase = "";
	
	private String id = "";
	
	private List<String> goals = new ArrayList<>();
	
	private PropertySet configuration = new PropertySet("configuration");

	public String getPhase() {
		return phase;
	}

	public void setPhase(String phase) {
		this.phase = phase;
	}

	public List<String> getGoals() {
		return goals;
	}

	public void setGoals(List<String> goals) {
		this.goals = goals;
	}

	public PropertySet getConfiguration() {
		return configuration;
	}

	public void setConfiguration(PropertySet configuration) {
		this.configuration = configuration;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

}
