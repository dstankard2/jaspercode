package net.sf.jaspercode.api.langsupport;

import java.util.List;

import net.sf.jaspercode.api.SourceFile;
import net.sf.jaspercode.api.types.VariableType;

public interface LanguageSupport {

	public String getLanguageName();

	public List<VariableType> getBaseVariableTypes();

	public Class<? extends SourceFile> baseSourceFile();

	public Class<? extends VariableType> baseVariableType();

}
