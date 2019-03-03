package net.sf.jaspercode.langsupport.java.types.impl;

import net.sf.jaspercode.api.Code;
import net.sf.jaspercode.api.CodeExecutionContext;
import net.sf.jaspercode.api.JasperException;
import net.sf.jaspercode.api.types.ListType;
import net.sf.jaspercode.api.types.VariableType;
import net.sf.jaspercode.langsupport.java.JavaCode;
import net.sf.jaspercode.langsupport.java.types.JavaVariableType;
import net.sf.jaspercode.langsupport.java.types.JavaVariableTypeBase;

public class JavaListType extends JavaVariableTypeBase implements ListType {

	private JavaVariableType elementType = null;
	
	public JavaListType() {
		super("List","java.util.List",null);
	}

	public JavaListType getListTypeWithElementTypName(VariableType elementType) {
		JavaListType ret = new JavaListType();
		if (elementType instanceof JavaVariableType) {
			ret.elementType = (JavaVariableType)elementType;
		}
		return ret;
	}

	@Override
	public JavaCode declare(String varName, String elementType, CodeExecutionContext execCtx) throws JasperException {
		JavaVariableType eltType = execCtx.getType(JavaVariableType.class, elementType);
		JavaCode ret = new JavaCode("List<"+eltType.getClassName()+"> "+varName+" = new java.util.ArrayList<>();\n",eltType.getImport(),getImport());
		return ret;
	}

	@Override
	public Code appendToList(String listVarName, String value, CodeExecutionContext execCtx) throws JasperException {
		return new JavaCode(listVarName+".add("+value+");\n");
	}

	@Override
	public String getName() {
		if (elementType!=null) {
			return "list/"+elementType.getName();
		} else {
			return "list";
		}
	}

	@Override
	public JavaCode instantiate(String name, String value, CodeExecutionContext execCtx) throws JasperException {
		throw new JasperException("Cannot instantiate a List without an element type");
	}

	@Override
	public String getImport() {
		return "java.util.List";
	}

	@Override
	public String getClassName() {
		StringBuilder b = new StringBuilder();
		b.append("List");
		if (elementType!=null) {
			if (elementType.getImport()!=null) {
				b.append('<').append(elementType.getImport()).append('>');
			} else {
				b.append('<').append(elementType.getClassName()).append('>');
			}
		}
		return b.toString();
	}

	@Override
	public JavaCode declare(String name, CodeExecutionContext execCtx) throws JasperException {
		if (elementType==null) {
			throw new JasperException("Cannot declare a list without an element type");
		}
		return this.declare(name, elementType.getName(), execCtx);
	}
	
	@Override
	public VariableType getElementType() {
		return elementType;
	}

}
