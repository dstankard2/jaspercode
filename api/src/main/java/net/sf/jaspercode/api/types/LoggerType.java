package net.sf.jaspercode.api.types;

import com.sun.org.apache.bcel.internal.classfile.Code;

public interface LoggerType extends VariableType {

	Code error(String valueString);
	Code warn(String valueString);
	Code info(String valueString);
	Code debug(String valueString);

}
