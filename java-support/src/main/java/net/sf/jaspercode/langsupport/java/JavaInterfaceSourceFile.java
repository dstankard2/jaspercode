package net.sf.jaspercode.langsupport.java;

import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.JavaInterfaceSource;

import net.sf.jaspercode.api.ProcessorContext;
import net.sf.jaspercode.api.SourceFile;
import net.sf.jaspercode.api.exception.JasperException;

public class JavaInterfaceSourceFile extends JavaSourceFile<JavaInterfaceSource> {

	public JavaInterfaceSourceFile(ProcessorContext ctx) throws JasperException {
		super(JavaInterfaceSource.class, ctx);
	}

	public JavaInterfaceSourceFile(ProcessorContext ctx, JavaInterfaceSource copy) throws JasperException {
		super(JavaInterfaceSource.class, ctx);
		this.src = copy;
	}

	public SourceFile copy() {
		StringBuilder src = this.getSource();
		JavaInterfaceSource copy = Roaster.parse(JavaInterfaceSource.class, src.toString());
		try {
			return new JavaInterfaceSourceFile(ctx, copy);
		} catch(JasperException e) {
			// should be impossible
		}
		return null;
	}

}

