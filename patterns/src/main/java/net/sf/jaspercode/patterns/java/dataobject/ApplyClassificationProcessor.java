package net.sf.jaspercode.patterns.java.dataobject;

import java.util.List;

import net.sf.jaspercode.api.ComponentProcessor;
import net.sf.jaspercode.api.JasperUtils;
import net.sf.jaspercode.api.ProcessorContext;
import net.sf.jaspercode.api.annotation.Plugin;
import net.sf.jaspercode.api.annotation.Processor;
import net.sf.jaspercode.api.config.Component;
import net.sf.jaspercode.api.exception.JasperException;
import net.sf.jaspercode.langsupport.java.JavaClassSourceFile;
import net.sf.jaspercode.langsupport.java.JavaUtils;
import net.sf.jaspercode.langsupport.java.types.impl.JavaDataObjectType;
import net.sf.jaspercode.patterns.xml.java.dataobject.ApplyClassification;

@Plugin
@Processor(componentClass = ApplyClassification.class)
public class ApplyClassificationProcessor implements ComponentProcessor {
	private ApplyClassification comp = null;
	private ProcessorContext ctx = null;

	@Override
	public void init(Component component, ProcessorContext ctx) {
		this.comp = (ApplyClassification)component;
		this.ctx = ctx;
	}

	@Override
	public void process() throws JasperException {
		ctx.setLanguageSupport("Java8");
		String cl = comp.getClassificationName();
		List<String> dos = comp.getObject();
		boolean autoApply = comp.getAutoApply();

		if (autoApply) {
			ctx.getLog().warn("Classification auto-apply is not currently supported");
		}
		JavaDataObjectType clType = JasperUtils.getType(JavaDataObjectType.class, cl, ctx);
		for(String dataObj : dos) {
			JavaDataObjectType ty = JasperUtils.getType(JavaDataObjectType.class, dataObj, ctx);
			for(String attr : clType.getAttributeNames()) {
				if (!clType.getAttributeType(attr).equals(ty.getAttributeType(attr))) {
					throw new JasperException("Could not apply classification '"+cl+"' to data object '"+dataObj+"' - data object does not have attribute '"+attr+"'");
				}
			}
			ty.getSuperTypes().add(clType.getName());
			JavaClassSourceFile src = JavaUtils.getClassSourceFile(ty.getImport(), ctx);
			src.getSrc().addInterface(clType.getImport());
		}
	}

}

