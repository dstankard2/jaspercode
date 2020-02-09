package net.sf.jaspercode.langsupport.javascript.types;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.jaspercode.api.CodeExecutionContext;
import net.sf.jaspercode.api.ProcessorContext;

/**
 * Represents a callable function on a Javascript-based object.
 * 
 * Even though, in Javascript, a function is an object, for the sake of 
 * Javascribe design patterns we treat it as only a function that can be 
 * invoked.
 * 
 * For an object that is instantiated, use an AttributeHolder or 
 * JavascriptServiceObjectType that extend JavascriptVariableType.
 * @author DCS
 *
 */
public class JavascriptFunctionType extends JavascriptType {
	List<String> paramNames = new ArrayList<String>();
	HashMap<String,String> paramTypes = new HashMap<String,String>();
	String obj = null;
	String bindRef = null;
	String name = null;
	String returnType = null;

	public String getReturnType() {
		return returnType;
	}
	public void setReturnType(String returnType) {
		this.returnType = returnType;
	}
	public String getName() {
		return name;
	}
	public JavascriptFunctionType(String name,ProcessorContext ctx) {
		super(name);
		this.name = name;
	}
	
	public void setObj(String obj) {
		this.obj = obj;
	}
	public String getObj() {
		return obj;
	}

	public void addParam(String name,String type) {
		paramNames.add(name);
		paramTypes.put(name, type);
	}
	public List<String> getParamNames() {
		return paramNames;
	}
	public String getParamType(String paramName) {
		return paramTypes.get(paramName);
	}
	
	public String invoke(String resultVar,String ref,Map<String,String> paramRefs,CodeExecutionContext execCtx) {
		if (paramRefs==null) {
			paramRefs = new HashMap<String,String>();
		}
		StringBuilder b = new StringBuilder();
		if (resultVar!=null) b.append(resultVar+" = ");
		if (ref!=null) b.append(ref+'.');
		b.append(this.getName()+'(');
		boolean first = true;
		for(String p : this.getParamNames()) {
			String paramValue = paramRefs.get(p);
			if (paramValue==null) {
				if (execCtx.getVariableType(p)!=null) paramValue = p;
				else paramValue = "undefined";
			}
			if (first) first = false;
			else b.append(',');
			b.append(paramValue);
		}
		b.append(");\n");
		return b.toString();
	}
	
}

