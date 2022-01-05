package net.sf.jaspercode.eng;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class EngineProperties {

	private Map<String,String> userOptions = null;

	protected List<String> getValueList(String option) {
		String val = userOptions.get(option);
		if ((val==null) || (val.trim().length()==0)) {
			return new ArrayList<>();
		}
		String[] vals = val.split(",");
		return Arrays.asList(vals);
	}
	
	public boolean getBoolean(String option,boolean defaultValue) {
		boolean ret = defaultValue;
		
		String val = userOptions.get(option);
		if (val!=null) {
			if ((val.equalsIgnoreCase("true")) || (val.equalsIgnoreCase("T")) || (val.equalsIgnoreCase("Y"))) {
				ret = true;
			} else {
				ret = false;
			}
		}
		
		return ret;
	}
	
	public String getProperty(String option, String defaultValue) {
		String ret = defaultValue;
		String val = userOptions.get(option);
		if (val!=null) {
			ret = val;
		}
		return ret;
	}

	public EngineProperties(Map<String,String> userOptions) {
		this.userOptions = userOptions;
	}

	public Map<String, String> getUserOptions() {
		return userOptions;
	}

	public List<String> getCommands() {
		return getValueList("commands");
	}

	public boolean getRunOnce() {
		return userOptions.get("once")!=null;
	}

	public boolean getSingleApp() {
		return userOptions.get("singleAppMode")!=null;
	}

	public String getApplicationDir() {
		return userOptions.get("applicationDir");
	}

	public String getOutputDir() {
		return userOptions.get("outputDir");
	}
	
	public boolean getDebug() {
		return getBoolean("engineDebug", false);
	}

}

