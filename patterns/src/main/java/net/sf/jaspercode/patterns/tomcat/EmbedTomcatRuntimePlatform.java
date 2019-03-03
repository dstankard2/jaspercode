package net.sf.jaspercode.patterns.tomcat;

import java.util.ArrayList;
import java.util.List;

import net.sf.jaspercode.api.Command;
import net.sf.jaspercode.patterns.CommandImpl;
import net.sf.jaspercode.patterns.web.JavaWebappRuntimePlatform;

/**
 * Runtime platform implementation for an Embed Tomcat jar.  The jar should have a Main Class defined.
 * This runtime platform is deployed by invoking "java -jar <jarFile>"
 * @author DCS
 *
 */
public class EmbedTomcatRuntimePlatform extends JavaWebappRuntimePlatform {

	private String jarLocation = null;
	List<DataSourceInfo> dataSources = new ArrayList<>();
	private Integer debugPort = null;

	public EmbedTomcatRuntimePlatform(String jarLocation, Integer debugPort) {
		this.jarLocation = jarLocation;
		this.debugPort = debugPort;
	}

	@Override
	public List<Command> deploy() {
		List<Command> ret = new ArrayList<>();
		StringBuilder cmdString = new StringBuilder();

		cmdString.append("java ");
		if (debugPort!=null) {
			cmdString.append("-Xdebug -Xrunjdwp:transport=dt_socket,address=")
			.append(debugPort)
			.append(",server=y,suspend=y ");
		}
		cmdString.append("-jar ").append(jarLocation);
		ret.add(new CommandImpl(cmdString.toString(), true));

		return ret;
	}

	@Override
	public List<Command> undeploy() {
		return new ArrayList<Command>();
	}

	public List<DataSourceInfo> getDataSources() {
		return dataSources;
	}

	public void setDataSources(List<DataSourceInfo> dataSources) {
		this.dataSources = dataSources;
	}

}
