package net.sf.jaspercode.patterns.java.dataobject;

import org.jboss.forge.roaster.model.source.JavaEnumSource;

import net.sf.jaspercode.api.ComponentProcessor;
import net.sf.jaspercode.api.JasperException;
import net.sf.jaspercode.api.ProcessorContext;
import net.sf.jaspercode.api.annotation.Plugin;
import net.sf.jaspercode.api.annotation.Processor;
import net.sf.jaspercode.api.config.Component;
import net.sf.jaspercode.langsupport.java.JavaEnumSourceFile;
import net.sf.jaspercode.langsupport.java.JavaUtils;
import net.sf.jaspercode.langsupport.java.types.impl.JavaEnumType;
import net.sf.jaspercode.patterns.xml.java.dataobject.OrdinalEnum;

@Plugin
@Processor(componentClass = OrdinalEnum.class)
public class OrdinalEnumProcessor implements ComponentProcessor {

	OrdinalEnum comp = null;
	ProcessorContext ctx = null;
	
	@Override
	public void init(Component component, ProcessorContext ctx) {
		this.comp = (OrdinalEnum)component;
		this.ctx = ctx;
	}

	@Override
	public void process() throws JasperException {
		String ref = comp.getRef();
		ctx.setLanguageSupport("Java8");
		JavaUtils.getJavaPackage(comp, ctx);
		String className = comp.getName();
		String pkg = JavaUtils.getJavaPackage(comp, ctx);
		JavaEnumSourceFile file = new JavaEnumSourceFile(ctx);
		file.getJavaEnumSource().setPackage(pkg);
		file.getJavaEnumSource().setName(className);
		ctx.addSourceFile(file);
		JavaEnumSource e = file.getJavaEnumSource();

		for(String value : comp.getValue()) {
			e.addEnumConstant(value);
		}

		JavaEnumType en = new JavaEnumType(className,pkg,ctx.getBuildContext());
		ctx.addVariableType(en);

		if (ref.trim().length()>0) {
			if (ctx.getSystemAttribute(ref)!=null) {
				throw new JasperException("ref '"+ref+"' is already a system attribute");
			}
		}
		ctx.addSystemAttribute(ref, en.getName());
	}

}
