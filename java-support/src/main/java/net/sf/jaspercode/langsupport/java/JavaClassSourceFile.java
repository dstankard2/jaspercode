package net.sf.jaspercode.langsupport.java;

import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.JavaClassSource;

import net.sf.jaspercode.api.ProcessorContext;
import net.sf.jaspercode.api.SourceFile;
import net.sf.jaspercode.api.exception.JasperException;

public class JavaClassSourceFile extends JavaSourceFile<JavaClassSource> {

	public JavaClassSourceFile(ProcessorContext ctx) throws JasperException {
		super(JavaClassSource.class, ctx);
	}

	public JavaClassSourceFile(ProcessorContext ctx, JavaClassSource copy) throws JasperException {
		super(JavaClassSource.class, ctx);
		this.src = copy;
	}

	public SourceFile copy() {
		StringBuilder src = this.getSource();
		JavaClassSource copy = Roaster.parse(JavaClassSource.class, src.toString());
		try {
			return new JavaClassSourceFile(ctx, copy);
		} catch(JasperException e) {
			// should be impossible
		}
		return null;
	}

}

