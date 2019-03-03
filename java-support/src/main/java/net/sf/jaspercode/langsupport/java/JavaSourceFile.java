package net.sf.jaspercode.langsupport.java;

import org.jboss.forge.roaster.model.JavaClass;

import net.sf.jaspercode.api.BuildContext;
import net.sf.jaspercode.api.JasperException;
import net.sf.jaspercode.api.ProcessorContext;
import net.sf.jaspercode.api.SourceFile;

public class JavaSourceFile<T extends JavaClass> implements SourceFile {

	private BuildContext buildCtx = null;
	private T srcClass = null;
	private Class<T> cl = null;

	public T getJavaClass() {
		return srcClass;
	}

	public JavaSourceFile(Class<T> cl,ProcessorContext ctx) {
		this.cl = cl;
		this.buildCtx = ctx.getBuildContext();
	}

	@Override
	public StringBuilder getSource() throws JasperException {
		StringBuilder b = new StringBuilder();
		b.append(srcClass.toString());
		return b;
	}

	@Override
	public String getPath() {
		String base = buildCtx.getOutputRootPath("java");
		if (base==null) base = "";
		String dir = srcClass.getPackage();
		String filename = srcClass.getName()+".java";
		return base + '/' + dir.replace('.', '/')+'/'+filename;
	}

}

