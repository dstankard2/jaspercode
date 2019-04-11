package net.sf.jaspercode.langsupport.javascript.modules;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class StandardModuleSource implements ModuleSource {

	private String name = null;

	public StandardModuleSource(String name) {
		this.name = name;
	}

	private Map<String,String> properties = new HashMap<>();

	private Map<String,ModuleFunction> functions = new HashMap<>();

	private Map<String,ModuleFunction> internalFunctions = new HashMap<>();

	private StringBuilder initCode = new StringBuilder();
	
	public void addProperty(String name,String type) {
		properties.put(name, type);
	}

	public void addInternalFunction(ModuleFunction fn) {
		internalFunctions.put(fn.getName(), fn);
	}
	
	public void addFunction(ModuleFunction fn) {
		functions.put(fn.getName(), fn);
	}
	
	public StringBuilder getInitCode() {
		return initCode;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public StringBuilder getCode() {
		StringBuilder ret = new StringBuilder();
		StringBuilder objSource = new StringBuilder();
		boolean first = true;
		
		for(String name : properties.keySet()) {
			ret.append("var _"+name+";\n");
			if (first) first = false;
			else objSource.append(",\n");
			objSource.append(name+" : _"+name);
		}
		for(String name : internalFunctions.keySet()) {
			ModuleFunction fn = internalFunctions.get(name);
			ret.append(fnSource(fn));
		}
		for(String name : functions.keySet()) {
			ModuleFunction fn = functions.get(name);

			ret.append(fnSource(fn));
			if (first) first = false;
			else objSource.append(",\n");
			objSource.append(name+" : _"+name);
		}
		
		ret.append(initCode);
		ret.append("var _obj = {\n");
		ret.append(objSource);
		ret.append("};\n");
		
		ret.append("return _obj;\n");
		return ret;
	}
	
	private String fnSource(ModuleFunction fn) {
		StringBuilder b = new StringBuilder();
		String name = fn.getName();
		
		b.append("function _"+name+"(");
		boolean firstParam = true;
		for(String p : fn.getParamNames()) {
			if (firstParam) firstParam = false;
			else b.append(',');
			b.append(p);
		}
		b.append(") {\n");
		b.append(fn.getCode().getCodeText());
		b.append("\n}\n");

		return b.toString();
	}

	public StandardModuleSource copy() {
		StandardModuleSource ret = new StandardModuleSource(name);
		
		ret.initCode = new StringBuilder(initCode.toString());
		ret.properties = copy(properties);
		ret.functions = copy(functions);
		ret.internalFunctions = copy(internalFunctions);
		
		return ret;
	}

	private <Y extends Object> Map<String,Y> copy(Map<String,Y> orig) {
		Map<String,Y> ret = new HashMap<>();
		
		for(Entry<String,Y> entry : orig.entrySet()) {
			ret.put(entry.getKey(), entry.getValue());
		}
		
		return ret;
	}
}

