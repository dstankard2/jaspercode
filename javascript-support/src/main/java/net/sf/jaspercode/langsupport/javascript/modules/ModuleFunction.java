package net.sf.jaspercode.langsupport.javascript.modules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.jaspercode.api.types.ServiceOperation;
import net.sf.jaspercode.langsupport.javascript.JavascriptCode;

public class ModuleFunction {

	private JavascriptCode code = null;
	
	Map<String,String> paramTypes = new HashMap<>();
	private List<String> paramNames = new ArrayList<>();

	private String name = null;

	public void setParameters(ServiceOperation op) {
		for(String param : op.getParamNames()) {
			String type = op.getParamType(param);
			addParam(param,type);
		}
	}

	public void addParam(String name,String type) {
		paramNames.add(name);
		paramTypes.put(name, type);
	}
	
	public JavascriptCode getCode() {
		return code;
	}

	public void setCode(JavascriptCode code) {
		this.code = code;
	}

	public List<String> getParamNames() {
		return paramNames;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
}
