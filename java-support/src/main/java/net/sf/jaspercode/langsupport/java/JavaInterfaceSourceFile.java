package net.sf.jaspercode.langsupport.java;

import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.JavaInterfaceSource;

import net.sf.jaspercode.api.BuildContext;
import net.sf.jaspercode.api.JasperException;
import net.sf.jaspercode.api.ProcessorContext;
import net.sf.jaspercode.api.SourceFile;

public class JavaInterfaceSourceFile implements SourceFile {

	protected BuildContext buildCtx = null;
	JavaInterfaceSource cl = null;
	
	public JavaInterfaceSourceFile(BuildContext buildCtx) {
		this.buildCtx = buildCtx;
		cl = Roaster.create(JavaInterfaceSource.class);
	}

	public JavaInterfaceSourceFile(ProcessorContext ctx) {
		this(ctx.getBuildContext());
	}
	
	public JavaInterfaceSource getJavaClassSource() {
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

	public void addImport(String im) {
		if (im==null) return;
		this.getJavaClassSource().addImport(im);
	}

}
