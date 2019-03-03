package net.sf.jaspercode.langsupport.java;

import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.JavaClassSource;

import net.sf.jaspercode.api.BuildContext;
import net.sf.jaspercode.api.JasperException;
import net.sf.jaspercode.api.ProcessorContext;
import net.sf.jaspercode.api.SourceFile;
import net.sf.jaspercode.langsupport.java.types.JavaVariableType;
import net.sf.jaspercode.langsupport.java.types.impl.JavaListType;

public class JavaClassSourceFile implements SourceFile {

	protected BuildContext buildCtx = null;
	JavaClassSource cl = null;
	
	public JavaClassSourceFile(BuildContext buildCtx) {
		this.buildCtx = buildCtx;
		cl = Roaster.create(JavaClassSource.class);
	}

	public JavaClassSourceFile(ProcessorContext ctx) {
		this(ctx.getBuildContext());
	}
	
	public JavaClassSource getJavaClassSource() {
		return cl;
	}
	
	@Override
	public StringBuilder getSource() throws JasperException {
		StringBuilder b = new StringBuilder();
		b.append(cl.toString());
		return b;
	}

	@Override
	public String getPath() {
		String base = buildCtx.getOutputRootPath("java");
		if (base==null) base = "";
		String dir = cl.getPackage();
		String filename = cl.getName()+".java";
		return base + '/' + dir.replace('.', '/')+'/'+filename;
	}

	public void addImport(JavaVariableType type) {
		if (type.getImport()!=null) {
			this.getJavaClassSource().addImport(type.getImport());
		}
		if (type instanceof JavaListType) {
			JavaListType listType = (JavaListType)type;
			if (listType.getElementType()!=null) {
				JavaVariableType eltType = (JavaVariableType)listType.getElementType();
				if (eltType.getImport()!=null) {
					this.getJavaClassSource().addImport(eltType.getImport());
				}
			}
		}
	}
	
	public void addImports(JavaCode code) {
		for(String s : code.getImports()) {
			getJavaClassSource().addImport(s);
		}
	}

}
