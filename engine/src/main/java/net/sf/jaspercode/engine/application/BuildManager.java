package net.sf.jaspercode.engine.application;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.jaspercode.engine.definitions.ApplicationFolderImpl;
import net.sf.jaspercode.engine.processing.BuildComponentEntry;

public class BuildManager {

	List<BuildComponentEntry> buildAlways = new ArrayList<>();
	List<BuildComponentEntry> deployAlways = new ArrayList<>();
	
	Map<String,BuildComponentEntry> buildComps = new HashMap<>();

	public void buildRemoved(BuildComponentEntry entry) {
		
	}

	public void updateBuildComponent(ApplicationFolderImpl folder) {
		buildComps.put(folder.getPath(), folder.getBuildComponent());
	}

	public void buildAlways(ApplicationFolderImpl folder) {
	}

	public void deployAlways(ApplicationFolderImpl folder) {
	}

	public void performBuilds() {
	}
	
	public void performDeploys() {
	}

}

