package net.sf.jaspercode.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.sf.jaspercode.api.types.VariableType;

public class CodeExecutionContext {

	private List<String> dependencyNames = new ArrayList<>();
	
	private HashMap<String,String> variables = new HashMap<>();
	private ProcessorContext ctx = null;
	
	public List<String> getDependencyNames() {
		return dependencyNames;
	}
	
	public List<String> getVariableNames() {
		List<String> ret = new ArrayList<String>();
		
		for(String k : variables.keySet()) {
			ret.add(k);
		}
		
		return ret;
	}

	/**
	 * Creates a new context with the same variables declared as the specified 
	 * parent.  Adding variables to the new context will not affect the parent 
	 * context.  This is handy for emulating new variable scopes from "{" in 
	 * some languages.
	 * @param parent
	 * @param types
	 */
	public CodeExecutionContext(CodeExecutionContext parent,ProcessorContext ctx) {
		if (parent!=null) {
			this.ctx = parent.ctx;
			variables.putAll(parent.variables);
			dependencyNames.addAll(parent.dependencyNames);
		} else {
			this.ctx = ctx;
		}
	}
	
	public CodeExecutionContext(CodeExecutionContext parent) {
		this(parent,null);
	}
	
	public CodeExecutionContext(ProcessorContext ctx) {
		this(null,ctx);
	}
	
	/**
	 * Adds a variable with the given name and the given type.
	 * @param name
	 * @param type
	 */
	public void addVariable(String name,String type) {
		variables.put(name,type);
	}

	public <T extends VariableType> T getType(Class<T> clazz,String name) throws JasperException {
		return JasperUtils.getType(clazz, name, ctx);
	}
	
	public String getVariableType(String var) {
		return variables.get(var);
	}
	
	/**
	 * Returns the variable type for the given variable.
	 * @param var
	 * @return Type of the specified variable, or null.
	 */
	public VariableType getTypeForVariable(String var) throws JasperException {
		String type = variables.get(var);
		VariableType ret = null;

		if (type!=null) {
			ret = ctx.getVariableType(type);
			if (ret==null) {
				throw new JasperException("Couldn't find type '"+type+"' for variable '"+var+"'");
			}
			return ctx.getVariableType(type);
		}

		return ret;
	}
	
}

