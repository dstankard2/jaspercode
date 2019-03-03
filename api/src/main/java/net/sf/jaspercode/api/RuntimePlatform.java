package net.sf.jaspercode.api;

import java.util.List;

public interface RuntimePlatform {

	public List<Command> deploy();
	public List<Command> undeploy();
	
}

