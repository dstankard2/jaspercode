package net.sf.jaspercode.langsupport.java;

import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.JavaEnumSource;

import net.sf.jaspercode.api.BuildContext;
import net.sf.jaspercode.api.JasperException;
import net.sf.jaspercode.api.ProcessorContext;
import net.sf.jaspercode.api.SourceFile;

public class JavaEnumSourceFile implements SourceFile {

	private BuildContext buildCtx = null;
	private JavaEnumSource src = null;

	public JavaEnumSourceFile(ProcessorContext ctx) {
		this.buildCtx = ctx.getBuildContext();
		src = Roaster.create(JavaEnumSource.class);
	}
	
	public JavaEnumSource getJavaEnumSource() {
		return src;
	}

	@Override
	public StringBuilder getSource() throws JasperException {
		StringBuilder b = new StringBuilder();
		b.append(src.toString());
		return b;
	}

	@Override
	public String getPath() {
		String base = buildCtx.getOutputRootPath("java");
		if (base==null) base = "";
		String dir = src.getPackage();
		String filename = src.getName()+".java";
		return base + '/' + dir.replace('.', '/')+'/'+filename;
	}

}
