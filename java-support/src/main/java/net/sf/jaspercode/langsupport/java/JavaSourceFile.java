package net.sf.jaspercode.langsupport.java;

import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.JavaSource;

import net.sf.jaspercode.api.ProcessorContext;
import net.sf.jaspercode.api.SourceFile;
import net.sf.jaspercode.api.exception.JasperException;
import net.sf.jaspercode.langsupport.java.types.JavaVariableType;
import net.sf.jaspercode.langsupport.java.types.impl.JavaListType;

public abstract class JavaSourceFile<T extends JavaSource<?>> implements SourceFile {

	protected ProcessorContext ctx = null;
	protected T src = null;
	protected Class<T> cl = null;
	protected String basePath = null;

	public T getSrc() {
		return src;
	}

	public JavaSourceFile(Class<T> cl,ProcessorContext ctx) throws JasperException {
		this.cl = cl;
		this.ctx = ctx;
		//this.buildCtx = ctx.getBuildContext();
		this.src = Roaster.create(cl);
		this.basePath = ctx.getBuildContext().getOutputRootPath("java");
	}

	@Override
	public StringBuilder getSource() {
		StringBuilder b = new StringBuilder();
		b.append(src.toString());
		return b;
	}

	@Override
	public String getPath() {
		String base = basePath;
		if (basePath==null) base = "";
		String dir = src.getPackage();
		String filename = src.getName()+".java";
		return base + '/' + dir.replace('.', '/')+'/'+filename;
	}

	@Override
	public abstract SourceFile copy();

	public void addImport(JavaVariableType type) {
		if (type.getImport()!=null) {
			this.src.addImport(type.getImport());
		}
		if (type instanceof JavaListType) {
			JavaListType listType = (JavaListType)type;
			if (listType.getElementType()!=null) {
				JavaVariableType eltType = (JavaVariableType)listType.getElementType();
				if (eltType.getImport()!=null) {
					this.src.addImport(eltType.getImport());
				}
			}
		}
	}
	
	public void addImports(JavaCode code) {
		for(String s : code.getImports()) {
			src.addImport(s);
		}
	}

}

