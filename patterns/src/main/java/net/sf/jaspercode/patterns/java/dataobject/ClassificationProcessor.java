package net.sf.jaspercode.patterns.java.dataobject;

import org.jboss.forge.roaster.model.source.JavaInterfaceSource;

import net.sf.jaspercode.api.ComponentProcessor;
import net.sf.jaspercode.api.JasperUtils;
import net.sf.jaspercode.api.ProcessorContext;
import net.sf.jaspercode.api.annotation.Plugin;
import net.sf.jaspercode.api.annotation.Processor;
import net.sf.jaspercode.api.config.Component;
import net.sf.jaspercode.api.exception.JasperException;
import net.sf.jaspercode.langsupport.java.JavaInterfaceSourceFile;
import net.sf.jaspercode.langsupport.java.JavaUtils;
import net.sf.jaspercode.langsupport.java.types.JavaVariableType;
import net.sf.jaspercode.langsupport.java.types.impl.JavaDataObjectType;
import net.sf.jaspercode.patterns.xml.java.dataobject.Classification;

@Plugin
@Processor(componentClass = Classification.class)
public class ClassificationProcessor implements ComponentProcessor {
	Classification comp = null;
	ProcessorContext ctx = null;
	
	@Override
	public void init(Component component, ProcessorContext ctx) {
		this.comp = (Classification)component;
		this.ctx = ctx;
	}

	@Override
	public void process() throws JasperException {
		ctx.setLanguageSupport("Java8");
		JavaInterfaceSourceFile file = new JavaInterfaceSourceFile(ctx);
		String pkg = JavaUtils.getJavaPackage(comp, ctx);
		String className = comp.getName();
		JavaInterfaceSource src = file.getSrc();
		src.setName(className);
		src.setPackage(pkg);
		JavaDataObjectType type = new JavaDataObjectType(className, pkg+'.'+className, ctx.getBuildContext());

		type.setIsInterface(true);

		String lowerCamel = JasperUtils.getLowerCamelName(className);
		String multi = JasperUtils.getMultiple(lowerCamel);
		
		ctx.addSystemAttribute(lowerCamel, className);
		ctx.addSystemAttribute(multi, "list/"+className);
		ctx.addSystemAttribute(lowerCamel+"List", "list/"+className);
		
		if (comp.getExtend().trim().length()>0) {
			String exs[] = comp.getExtend().split(",");
			for(String ex : exs) {
				JavaDataObjectType exType = JasperUtils.getType(JavaDataObjectType.class, ex, ctx);
				if (!exType.getIsInterface()) {
					throw new JasperException("A classification may only extend another classification");
				}
				ctx.dependOnVariableType(exType);
				src.addInterface(exType.getImport());
				for(String n : exType.getAttributeNames()) {
					if (type.getAttributeType(n)==null) {
						String t = exType.getAttributeType(n);
						type.addProperty(n, t);
					}
				}
			}
		}
		
		if (comp.getAttributes().trim().length()==0) {
			throw new JasperException("Classification must define one or more attributes");
		}
		String[] attribs = comp.getAttributes().split(",");
		for(String attr : attribs) {
			String typeName = null;
			if (attr.indexOf(':')>0) {
				int i = attr.indexOf(':');
				typeName = attr.substring(i+1);
				attr = attr.substring(0, i);
				String existingType = ctx.getSystemAttribute(attr);
				if ((existingType!=null) && (!existingType.equals(typeName))) {
					throw new JasperException("Found inconsistent types for system attribute '"+attr+"'");
				}
				ctx.addSystemAttribute(attr, typeName);
			} else {
				typeName = ctx.getSystemAttribute(attr);
				if (typeName==null) {
					throw new JasperException("Couldn't find type for classification attribute '"+attr+"'");
				}
				ctx.dependOnSystemAttribute(attr);
			}
			JavaVariableType attrType = JasperUtils.getType(JavaVariableType.class, typeName, ctx);
			String propertyClass = null;
			if (typeName.startsWith("list")) {
				propertyClass = JavaUtils.getClassDisplayForList(typeName, ctx);
			} else {
				propertyClass = attrType.getClassName();
			}
			ctx.dependOnVariableType(attrType);
			String upperCamel = JasperUtils.getUpperCamelName(attr);
			file.addImport(attrType);
			src.addMethod().setName("set"+upperCamel).setPublic().addParameter(propertyClass, attr);
			src.addMethod().setName("get"+upperCamel).setPublic().setReturnType(propertyClass);
			file.addImport(attrType);
		}
		
		ctx.addSourceFile(file);
		ctx.addVariableType(type);
	}

	
}
