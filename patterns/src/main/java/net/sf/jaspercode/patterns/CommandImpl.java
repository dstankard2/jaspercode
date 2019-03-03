package net.sf.jaspercode.patterns;

import net.sf.jaspercode.api.Command;

public class CommandImpl implements Command {
	String commandString = null;
	boolean asynch = false;

	public CommandImpl(String commandString,boolean asynch) {
		this.commandString = commandString;
		this.asynch = asynch;
	}

	@Override
	public String getCommandString() {
		return commandString;
	}

	@Override
	public boolean asynch() {
		return asynch;
	}

}
