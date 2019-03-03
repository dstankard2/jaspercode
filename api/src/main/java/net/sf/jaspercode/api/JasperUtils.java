package net.sf.jaspercode.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.sf.jaspercode.api.types.DataObjectType;
import net.sf.jaspercode.api.types.ListType;
import net.sf.jaspercode.api.types.ServiceOperation;
import net.sf.jaspercode.api.types.ServiceType;
import net.sf.jaspercode.api.types.VariableType;

/**
 * General utilities for use by pattern processors
 * @author DCS
 *
 */
public class JasperUtils {

	/**
	 * Returns the "natural" attribute name for the given type.  This is 
	 * generally the lower camel variation of the type name. If the type 
	 * name is "list/*" then the natural name is the lower camel name of 
	 * the list element type with "List" appended.
	 * @param typeName Name of an attribute type to get the attribute name 
	 * for.
	 * @return Natural attribute name.
	 */
	public static String getNaturalAttributeName(String typeName) {
		String ret = null;
		
		if (typeName.startsWith("list/")) {
			ret = getLowerCamelName(typeName.substring(5))+"List";
		} else {
			ret = getLowerCamelName(typeName);
		}
		
		return ret;
	}

	/**
	 * Assuming that the type name if a variable type name (generally 
	 * upper camel), returns the lower camel variation.
	 * @param typeName
	 * @return Lower camel variation of the typeName.
	 */
	public static String getLowerCamelName(String typeName) {
		return Character.toLowerCase(typeName.charAt(0))+typeName.substring(1);
	}
	
	public static boolean isLowerCamel(String str) {
		return getLowerCamelName(str).equals(str);
	}
	
	/**
	 * Assuming that the attributeName is a variable name (generally 
	 * lower camel), returns the upper camel variation.
	 * @param attributeName 
	 * @return Upper camel version of the name.
	 */
	public static String getUpperCamelName(String attributeName) {
		return Character.toUpperCase(attributeName.charAt(0))+attributeName.substring(1);
	}
	
	public static boolean isUpperCamelName(String str) {
		return getUpperCamelName(str).equals(str);
	}
	
	public static String getMultiple(String name) {
		String ret = null;
		if (name.endsWith("y")) {
			ret = name.substring(0, name.length()-2)+"ies";
		} else {
			ret = name + 's';
		}
		return ret;
	}
	
	public static List<AttribEntry> readParametersAsList(String paramString,ProcessorContext ctx) throws JasperException {
		List<AttribEntry> ret = new ArrayList<>();
		
		paramString = paramString.trim();
		if (paramString.length()==0) return ret;
		
		String[] parts = paramString.split(",");
		for(String part : parts) {
			part = part.trim();
			int index = part.indexOf(':');

			if (index>0) {
				String name = part.substring(0, index).trim();
				String typeName = part.substring(index+1).trim();
				VariableType type = ctx.getVariableType(typeName);
				if (type==null) {
					throw new JasperException("Found no variable type named '"+typeName+"'");
				}
				if (typeName.startsWith("list/")) {
					ListType listType = (ListType)type;
					String elementTypeName = typeName.substring(5);
					VariableType elementType = JasperUtils.getType(VariableType.class, elementTypeName, ctx);
					type = listType.getListTypeWithElementTypName(elementType);
				}
				ctx.addSystemAttribute(name, typeName);
				ctx.originateSystemAttribute(name);
				ret.add(new AttribEntry(name,type,true));
			} else if (index==0) {
				throw new JasperException("Found invalid attribute name '"+part+"'");
			} else {
				String typeName = ctx.getSystemAttribute(part.trim());
				if (typeName==null) {
					throw new JasperException("Couldn't find type for attribute '"+part+"'");
				}
				VariableType type = JasperUtils.getType(VariableType.class, typeName, ctx);
				//VariableType type = ctx.getVariableType(typeName);
				if ((type instanceof ListType) && (typeName.startsWith("list/"))) {
					String elementTypeName = typeName.substring(5);
					VariableType elementType = JasperUtils.getType(VariableType.class, elementTypeName, ctx);
					ListType listType = (ListType)type;
					type = listType.getListTypeWithElementTypName(elementType);
				}
				ret.add(new AttribEntry(part,type,false));
			}
		}
		
		return ret;
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends VariableType> T getType(Class<T> cl, String name,ProcessorContext ctx) throws JasperException {
		T ret = null;
		VariableType type = null;
		
		if (ctx==null) {
			throw new JasperException("Tried to find type '"+name+"' but got null ProcessorContext");
		}
		type = ctx.getVariableType(name);
		if (type==null) {
			throw new JasperException("Could not find type '"+name+"' for current language");
		}
		if (cl.isAssignableFrom(type.getClass())) {
			ret = (T)type;
		} else {
			throw new JasperException("Variable Type '"+name+"' was not of type '"+cl.getCanonicalName()+"'");
		}
		if ((ret instanceof ListType) && (name.startsWith("list/"))) {
			String elementTypeName = name.substring(5);
			ListType listType = (ListType)ret;
			VariableType elementType = getType(VariableType.class,elementTypeName,ctx);
			ret = (T)listType.getListTypeWithElementTypName(elementType);
		}
		
		return ret;
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends VariableType> T getTypeForSystemAttribute(Class<T> cl, String name,ProcessorContext ctx) throws JasperException {
		T ret = null;
		VariableType type = null;
		String typeName = ctx.getSystemAttribute(name);
		
		if (typeName==null) throw new JasperException("Could not find system attribute '"+name+"'");
		
		type = ctx.getVariableType(typeName);
		if (type==null) throw new JasperException("Couldn't find type '"+typeName+"'");
		if (cl.isAssignableFrom(type.getClass())) {
			ret = (T)type;
		} else {
			throw new JasperException("Variable Type '"+name+"' was not of type '"+cl.getCanonicalName()+"'");
		}
		
		return ret;
	}
	
	public static String getObjectName(String ref) throws JasperException {
		String ret = null;
		int i = ref.indexOf('.');
		if (i<=0) {
			throw new JasperException("Couldn't extract object name from reference '"+ref+"'");
		}
		ret = ref.substring(0, i);
		return ret;
	}

	public static String getRuleName(String ref) throws JasperException {
		String ret = null;
		int i = ref.indexOf('.');
		if (i<=0) {
			throw new JasperException("Couldn't extract rule name from reference '"+ref+"'");
		}
		ret = ref.substring(i+1);
		return ret;
	}

	public static HashMap<String,String> readParametersAsMap(String paramString,ProcessorContext ctx) throws JasperException {
		HashMap<String,String> ret = new HashMap<>();
		String params[] = paramString.split(",");
		if (paramString.trim().length()==0) return ret;
		
		for(String p : params) {
			int i = p.indexOf("=");
			String name = p.substring(0, i);
			String value = p.substring(i+1);
			ret.put(name, value);
		}
		
		return ret;
	}
	
	public static List<ServiceOperation> findRuleFromTypeAndRef(String ruleRef,ProcessorContext ctx) throws JasperException {
		List<ServiceOperation> ret = new ArrayList<>();
		int i = ruleRef.indexOf('.');
		
		if (i<0) {
			throw new JasperException("Reference '"+ruleRef+"' does not refer to a service and a rule");
		}
		String ref = ruleRef.substring(0, i);
		String ruleName = ruleRef.substring(i+1);
		
		ServiceType type = JasperUtils.getType(ServiceType.class, ref, ctx);
		for(ServiceOperation op : type.getOperations()) {
			if (op.getName().equals(ruleName)) {
				ret.add(op);
			}
		}
		
		return ret;
	}
	
	public static List<ServiceOperation> findRuleFromRef(String ruleRef,ProcessorContext ctx) throws JasperException {
		List<ServiceOperation> ret = new ArrayList<>();
		int i = ruleRef.indexOf('.');
		
		if (i<1) {
			throw new JasperException("Reference '"+ruleRef+"' does not refer to a service and a rule");
		}
		String ref = ruleRef.substring(0, i);
		String ruleName = ruleRef.substring(i+1);
		
		ServiceType type = JasperUtils.getTypeForSystemAttribute(ServiceType.class, ref, ctx);
		for(ServiceOperation op : type.getOperations()) {
			if (op.getName().equals(ruleName)) {
				ret.add(op);
			}
		}
		
		return ret;
	}
	
	public static String getTypeForRef(String ref,ProcessorContext ctx) throws JasperException {
		String ret = null;
		String parts[] = ref.split("\\.");
		
		if (parts.length==1) {
			ret = ctx.getSystemAttribute(parts[0]);
		} else {
			DataObjectType parentType = null;
			String parentTypeName = null;
			for(String part : parts) {
				if (ret==null) {
					ret = part;
					parentTypeName = ctx.getSystemAttribute(part);
					parentType = JasperUtils.getType(DataObjectType.class, parentTypeName, ctx);
					//parentType = execCtx.getType(DataObjectType.class, parentTypeName);
				} else {
					if (parentType.getAttributeType(part)==null) {
						throw new JasperException("Couldn't evaluate reference '"+ref+"' - type '"+parentTypeName+"' didn't have an attribute called '"+part+"'");
					}
					ret = parentType.getAttributeType(part);
				}
			}
		}
		
		return ret;
	}

	public static String evaluateReference(String ref,CodeExecutionContext execCtx) throws JasperException {
		String ret = null;
		
		String parts[] = ref.split("\\.");
		if (parts.length==1) {
			ret = ref;
		} else {
			DataObjectType parentType = null;
			String parentTypeName = null;
			for(String part : parts) {
				if (ret==null) {
					ret = part;
					parentTypeName = execCtx.getVariableType(part);
					parentType = execCtx.getType(DataObjectType.class, parentTypeName);
				} else {
					if (parentType.getAttributeType(part)==null) {
						throw new JasperException("Couldn't evaluate reference '"+ref+"' - type '"+parentTypeName+"' didn't have an attribute called '"+part+"'");
					}
					ret = parentType.getCodeToRetrieveAttribute(ret, part, null, execCtx);
				}
			}
		}
		
		return ret;
	}
	
	/**
	 * Does the data object type have the super
	 * @param type
	 * @param superTypeName
	 * @param ctx
	 * @return
	 * @throws JasperException
	 */
	public static boolean isSubclass(DataObjectType type,String superTypeName,ProcessorContext ctx) throws JasperException {
		boolean ret = false;
		
		while(type.getSuperTypes().size()>0) {
			List<String> typeNames = type.getSuperTypes();
			if (typeNames.contains(superTypeName)) {
				return true;
			}
			for(String typeName : typeNames) {
				DataObjectType s = JasperUtils.getType(DataObjectType.class, typeName, ctx);
				if (isSubclass(s, superTypeName,ctx)) {
					return true;
				}
			}
		}
		
		return ret;
	}
	
}
