package net.sf.jaspercode.langsupport.java;

import java.util.ArrayList;
import java.util.List;

import net.sf.jaspercode.api.SourceFile;
import net.sf.jaspercode.api.annotation.Plugin;
import net.sf.jaspercode.api.langsupport.LanguageSupport;
import net.sf.jaspercode.api.types.VariableType;
import net.sf.jaspercode.langsupport.java.types.JavaVariableType;
import net.sf.jaspercode.langsupport.java.types.impl.BooleanJavaType;
import net.sf.jaspercode.langsupport.java.types.impl.DateJavaType;
import net.sf.jaspercode.langsupport.java.types.impl.IntegerJavaType;
import net.sf.jaspercode.langsupport.java.types.impl.JavaListType;
import net.sf.jaspercode.langsupport.java.types.impl.LongJavaType;
import net.sf.jaspercode.langsupport.java.types.impl.StringJavaType;
import net.sf.jaspercode.langsupport.java.types.impl.TimestampJavaType;

@Plugin
public class JavaLanguageSupport implements LanguageSupport {

	@Override
	public String getLanguageName() {
		return "Java8";
	}

	@Override
	public List<VariableType> getBaseVariableTypes() {
		List<VariableType> ret = new ArrayList<>();
		ret.add(new IntegerJavaType());
		ret.add(new LongJavaType());
		ret.add(new StringJavaType());
		ret.add(new TimestampJavaType());
		ret.add(new DateJavaType());
		ret.add(new JavaListType());
		ret.add(new BooleanJavaType());
		return ret;
	}

	@Override
	public Class<? extends SourceFile> baseSourceFile() {
		return JavaClassSourceFile.class;
	}

	@Override
	public Class<? extends VariableType> baseVariableType() {
		return JavaVariableType.class;
	}

}
