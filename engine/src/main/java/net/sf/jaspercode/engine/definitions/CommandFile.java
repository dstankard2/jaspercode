package net.sf.jaspercode.engine.definitions;

import java.io.File;

// Represents commands.txt in a directory with a build component
public class CommandFile implements WatchedResource {
	ApplicationFolderImpl folder;
	File commandFile;
	
	public CommandFile(ApplicationFolderImpl folder, File commandFile) {
		this.folder = folder;
		this.commandFile = commandFile;
	}

	@Override
	public String getName() {
		return "commands.txt";
	}

	@Override
	public String getPath() {
		return folder.getPath()+"/commands.txt";
	}

	@Override
	public long getLastModified() {
		return commandFile.lastModified();
	}

	@Override
	public ApplicationFolderImpl getFolder() {
		return folder;
	}

}
