package net.sf.jaspercode.api.types;

import java.util.List;

import net.sf.jaspercode.api.Code;
import net.sf.jaspercode.api.CodeExecutionContext;
import net.sf.jaspercode.api.JasperException;

public interface DataObjectType extends VariableType {

    /**
     * Returns a string which represents the code needed to retrieve the specified attribute from 
     * the specified attribute holder, transforming it (if necessary) into the specified type.
     * This transformation is necessary in the case of attribute holders that hold generic 
     * types (such as Maps).
     * @param varName Name of the attribute holder instance we are retrieving the value from.
     * @param attribName Attribute to retrieve from the attribute holder.
     * @param targetType The type we want to interpret resulting value as.
     * @param execCtx Current code execution context.
     * @return A string containing the code to retrieve the specified attribute, as the specified type.
     * @throws IllegalArgumentException If the attribute cannot be interpreted as being of the targetType.
     * @throws ProcessingException If there is another issue.
     */
	public String getCodeToRetrieveAttribute(String varName,String attribName,String targetType,CodeExecutionContext execCtx) throws IllegalArgumentException,JasperException;

	/**
	 * 
	 * @param varName Name of the attribute holder instance we are trying to modify.
	 * @param attribName Name of the attribute to modify on the specified attribute holder.
	 * @param evaluatedValue Evaluated expression to set the specified attribute to.
	 * @param execCtx Current code execution context.
	 * @return Code to set the specified attribute to the given value.
	 */
	public String getCodeToSetAttribute(String varName,String attribName,String valueString,CodeExecutionContext execCtx) throws JasperException;
	
	/**
	 * Returns the type of the specified attribute.
	 * @param attrib The attribute to retrieve the type for.
	 * @return Type of the specified attribute.
	 */
	public String getAttributeType(String attrib) throws JasperException;

	public List<String> getAttributeNames();
	
	public Code instantiate(String varName);

	public List<String> getSuperTypes();

}
