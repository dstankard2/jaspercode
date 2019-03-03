package net.sf.jaspercode.langsupport.javascript;

import java.util.Arrays;
import java.util.List;

import net.sf.jaspercode.api.SourceFile;
import net.sf.jaspercode.api.annotation.Plugin;
import net.sf.jaspercode.api.langsupport.LanguageSupport;
import net.sf.jaspercode.api.types.VariableType;
import net.sf.jaspercode.langsupport.javascript.types.ArrayType;
import net.sf.jaspercode.langsupport.javascript.types.BooleanType;
import net.sf.jaspercode.langsupport.javascript.types.DOMElementType;
import net.sf.jaspercode.langsupport.javascript.types.DOMEventType;
import net.sf.jaspercode.langsupport.javascript.types.DoubleType;
import net.sf.jaspercode.langsupport.javascript.types.IntegerType;
import net.sf.jaspercode.langsupport.javascript.types.JavascriptType;
import net.sf.jaspercode.langsupport.javascript.types.LongintType;
import net.sf.jaspercode.langsupport.javascript.types.NodeListType;
import net.sf.jaspercode.langsupport.javascript.types.NodeType;
import net.sf.jaspercode.langsupport.javascript.types.ObjectType;
import net.sf.jaspercode.langsupport.javascript.types.PromiseType;
import net.sf.jaspercode.langsupport.javascript.types.StringType;

@Plugin
public class JavascriptLanguageSupport implements LanguageSupport {

	@Override
	public String getLanguageName() {
		return "Javascript";
	}

	@Override
	public List<VariableType> getBaseVariableTypes() {
		return Arrays.asList(
				new ArrayType(), new DOMElementType(), new DOMEventType(), new DoubleType(), 
				new IntegerType(), new NodeListType(), new NodeType(), new ObjectType(), new StringType(),
				PromiseType.noResultPromise("Promise"), new LongintType(), new BooleanType()
		);
	}

	@Override
	public Class<? extends SourceFile> baseSourceFile() {
		return JavascriptSourceFile.class;
	}

	@Override
	public Class<? extends VariableType> baseVariableType() {
		return JavascriptType.class;
	}

}
