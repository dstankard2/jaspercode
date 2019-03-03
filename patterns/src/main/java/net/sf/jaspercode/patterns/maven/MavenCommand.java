package net.sf.jaspercode.patterns.maven;

import java.util.ArrayList;
import java.util.List;

import net.sf.jaspercode.api.Command;

public class MavenCommand implements Command {

	private List<String> goals = new ArrayList<>();

	public boolean asynch() {
		return false;
	}

	public void addPhase(String goal) {
		if (!goals.contains(goal)) {
			goals.add(goal);
		}
	}
	
	public MavenCommand() {
	}

	@Override
	public String getCommandString() {
		if (goals.size()==0) return null;

		StringBuilder b = new StringBuilder();
		b.append("mvn.cmd");
		for(String s : goals) {
			b.append(' ').append(s.trim());
		}
		return b.toString();
	}

}
