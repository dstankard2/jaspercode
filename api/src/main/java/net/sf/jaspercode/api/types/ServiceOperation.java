package net.sf.jaspercode.api.types;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServiceOperation {
	private String name;
	private List<String> paramNames = new ArrayList<>();
	private Map<String,String> paramTypes = new HashMap<>();
	private String returnType;

	public ServiceOperation(String name) {
		this.name = name;
	}

	public ServiceOperation addParam(String name,String type) {
		paramNames.add(name);
		paramTypes.put(name, type);
		return this;
	}

	public ServiceOperation returnType(String type) {
		this.returnType = type;
		return this;
	}

	public List<String> getParamNames() {
		return paramNames;
	}

	public String getParamType(String name) {
		return paramTypes.get(name);
	}

	public String getReturnType() {
		return returnType;
	}

	public String getName() {
		return name;
	}

}

