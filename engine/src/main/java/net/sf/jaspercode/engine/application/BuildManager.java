package net.sf.jaspercode.engine.application;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.jaspercode.engine.definitions.ApplicationFolderImpl;
import net.sf.jaspercode.engine.processing.BuildComponentEntry;

public class BuildManager {

	private List<String> changesToCheck = new ArrayList<>();
	
	private List<String> cleanAlways = new ArrayList<>();
	private List<String> buildAlways = new ArrayList<>();
	private List<String> deployAlways = new ArrayList<>();
	
	private List<String> toClean = new ArrayList<>();
	private List<String> toBuild = new ArrayList<>();
	private List<String> toDeploy = new ArrayList<>();
	private List<String> toUndeploy = new ArrayList<>();
	
	private Map<String,Long> lastCommandChange = new HashMap<>();
	private ApplicationFolderImpl rootFolder = null;
	
	public BuildManager(ApplicationFolderImpl rootFolder) {
		this.rootFolder = rootFolder;
	}

	public void changeDetected(String path) {
		changesToCheck.add(path);
	}
	
	private void checkChanges() {
		for(String path : changesToCheck) {
			ApplicationFolderImpl folder = rootFolder.getResource(path).getFolder();
			BuildComponentEntry currentBuild = folder.getCurrentBuildComponent();
			String buildFolderPath = currentBuild.getFolder().getPath();
			if ((cleanAlways.contains(buildFolderPath)) && (!toClean.contains(buildFolderPath))) {
				toClean.add(buildFolderPath);
			}
			if ((buildAlways.contains(buildFolderPath)) && (!toBuild.contains(buildFolderPath))) {
				toBuild.add(buildFolderPath);
			}
			if ((deployAlways.contains(buildFolderPath)) && (!toDeploy.contains(buildFolderPath))) {
				toDeploy.add(buildFolderPath);
			}
		}
	}

	public long getLastCommandChange(String path) {
		return lastCommandChange.get(path);
	}

	public void scanStarted() {
		toClean.clear();
		toBuild.clear();
		toDeploy.clear();
	}
	public void clearStandingCommands(String path) {
		cleanAlways.remove(path);
		buildAlways.remove(path);
		deployAlways.remove(path);
	}
	public List<String> getStandingCommands(String path) {
		List<String> ret = new ArrayList<>();
		if (cleanAlways.contains(path)) ret.add("cleanAlways");
		if (buildAlways.contains(path)) ret.add("buildAlways");
		if (deployAlways.contains(path)) ret.add("deployAlways");
		return ret;
	}

	public void updateCommands(String path,List<String> commands) {
		if ((commands.contains("buildAlways")) && (!buildAlways.contains(path))) {
			buildAlways.add(path);
		}
		if ((commands.contains("deployAlways")) && (!deployAlways.contains(path))) {
			deployAlways.add(path);
		}
		if ((commands.contains("cleanAlways")) && (!cleanAlways.contains(path))) {
			cleanAlways.add(path);
		}

		if ((!commands.contains("buildAlways")) && (buildAlways.contains(path))) {
			buildAlways.remove(path);
		}
		if ((!commands.contains("deployAlways")) && (deployAlways.contains(path))) {
			deployAlways.remove(path);
		}
		if ((!commands.contains("cleanAlways")) && (cleanAlways.contains(path))) {
			cleanAlways.remove(path);
		}
	}

	private void runClean(String path) {
		ApplicationFolderImpl folder = (ApplicationFolderImpl)rootFolder.getResource(path);
		BuildComponentEntry build = folder.getBuildComponent();

		if (build!=null) {
			build.clean();
		}
	}
	
	private void runBuild(String path) {
		ApplicationFolderImpl folder = (ApplicationFolderImpl)rootFolder.getResource(path);
		BuildComponentEntry build = folder.getBuildComponent();

		if (build!=null) {
			build.build();
		}
	}
	
	private void runDeploy(String path) {
		ApplicationFolderImpl folder = (ApplicationFolderImpl)rootFolder.getResource(path);
		BuildComponentEntry build = folder.getBuildComponent();

		if (build!=null) {
			build.deploy();
		}
	}
	
	private void runUndeploy(String path) {
		ApplicationFolderImpl folder = (ApplicationFolderImpl)rootFolder.getResource(path);
		BuildComponentEntry build = folder.getBuildComponent();

		if (build!=null) {
			build.undeploy();
		}
	}
	
	public void checkCommands() {
		this.checkChanges();
		
		for(String s : toUndeploy) {
			runUndeploy(s);
		}
		for(String s : toClean) {
			runClean(s);
		}
		for(String s : toBuild) {
			runBuild(s);
		}
		for(String s : toDeploy) {
			runDeploy(s);
		}
	}

}

