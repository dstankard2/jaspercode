package net.sf.jaspercode.langsupport.javascript.modules;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import net.sf.jaspercode.api.exception.JasperException;
import net.sf.jaspercode.langsupport.javascript.JavascriptUtils;
import net.sf.jaspercode.langsupport.javascript.types.ExportedModuleType;

public class StandardModuleSource implements ModuleSource {

	private String name = null;
	private ExportedModuleType exportType = null;
	
	public StandardModuleSource(String name) {
		this.name = name;
	}

	private Map<String,String> properties = new HashMap<>();

	private Map<String,ModuleFunction> functions = new HashMap<>();

	private Map<String,ModuleFunction> internalFunctions = new HashMap<>();

	private StringBuilder initCode = new StringBuilder();
	
	public ExportedModuleType getExportType() {
		return exportType;
	}

	public void setExportType(ExportedModuleType exportType) {
		this.exportType = exportType;
	}

	public void addProperty(String name,String type) {
		properties.put(name, type);
	}
	
	public String getProperty(String name) {
		return properties.get(name);
	}

	public void addInternalFunction(ModuleFunction fn) throws JasperException {
		if (getExportType()==ExportedModuleType.CONST) {
			throw new JasperException("A constant cannot have an internal function.  You should add the module function to the module source file.");
		}
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
	public String getSource() {
		StringBuilder ret = new StringBuilder(); // Return value
		StringBuilder objSource = new StringBuilder(); // Object that is to be exported
		boolean first = true;

		for(String name : properties.keySet()) {
			ret.append("let "+getName()+"_"+name+";\n");
			if (first) first = false;
			else objSource.append(",\n");
			objSource.append(name+" : "+getName()+"_"+name);
		}
		internalFunctions.keySet().forEach(key-> {
			ModuleFunction fn = internalFunctions.get(name);
			ret.append(JavascriptUtils.fnSource(fn, null));
		});
		for(Entry<String,ModuleFunction> entry : functions.entrySet()) {
			ModuleFunction fn = entry.getValue();
			ret.append(JavascriptUtils.fnSource(fn, getName()+"_"));
			if (first) first = false;
			else objSource.append(",\n");
			objSource.append(fn.getName()+" : "+getName()+"_"+fn.getName());
		}

		if (getExportType()==ExportedModuleType.CONST) {
			ret.append(initCode);
			ret.append("export const "+getName()+" = {\n");
			ret.append(objSource.toString());
			ret.append("\n};\n");
		} else if (getExportType()==ExportedModuleType.CONSTRUCTOR) {
			ret.append("export function "+getName()+"() {\n");
			ret.append(initCode);
			ret.append("\n};\n");
		}
		return ret.toString();
	}
	
	public StandardModuleSource copy() {
		StandardModuleSource ret = new StandardModuleSource(name);
		
		ret.initCode = new StringBuilder(initCode.toString());
		ret.properties = copy(properties);
		ret.functions = copy(functions);
		ret.internalFunctions = copy(internalFunctions);
		ret.exportType = exportType;
		
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

