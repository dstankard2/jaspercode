package net.sf.jaspercode.patterns.web;

import java.util.ArrayList;
import java.util.List;

import net.sf.jaspercode.api.Command;
import net.sf.jaspercode.api.RuntimePlatform;
import net.sf.jaspercode.patterns.CommandImpl;

public class NginxRuntimePlatform implements RuntimePlatform {
	List<Command> deploy = new ArrayList<>();
	List<Command> undeploy = new ArrayList<>();
	
	public NginxRuntimePlatform(String nginxCommand, String cfg) {
		deploy.add(new CommandImpl(nginxCommand+" -t",false));
		//deploy.add(new CommandImpl(nginxCommand + " -p . -c " + cfg, true));
	}

	@Override
	public List<Command> deploy() {
		return deploy;
	}

	@Override
	public List<Command> undeploy() {
		return undeploy;
	}

}
