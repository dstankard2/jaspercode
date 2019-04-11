package net.sf.jaspercode.patterns.java.dataobject;

import java.util.List;

import org.jboss.forge.roaster.model.source.MethodSource;

import net.sf.jaspercode.api.AttribEntry;
import net.sf.jaspercode.api.ComponentProcessor;
import net.sf.jaspercode.api.JasperUtils;
import net.sf.jaspercode.api.ProcessorContext;
import net.sf.jaspercode.api.annotation.Plugin;
import net.sf.jaspercode.api.annotation.Processor;
import net.sf.jaspercode.api.config.Component;
import net.sf.jaspercode.api.exception.JasperException;
import net.sf.jaspercode.langsupport.java.JavaCode;
import net.sf.jaspercode.langsupport.java.JavaClassSourceFile;
import net.sf.jaspercode.langsupport.java.types.JavaVariableType;
import net.sf.jaspercode.langsupport.java.types.impl.JavaDataObjectType;
import net.sf.jaspercode.patterns.xml.java.dataobject.DataObject;

@Plugin
@Processor(componentClass = DataObject.class)
public class DataObjectProcessor implements ComponentProcessor {

	DataObject comp = null;
	ProcessorContext ctx = null;
	
	@Override
	public void init(Component component, ProcessorContext ctx) {
		comp = (DataObject)component;
		this.ctx = ctx;
	}

	@Override
	public void process() throws JasperException {
		ctx.setLanguageSupport("Java8");
		JavaClassSourceFile src = new JavaClassSourceFile(ctx);
		String name = comp.getName();
		String lowerCamel = JasperUtils.getLowerCamelName(name);
		String multiple = JasperUtils.getMultiple(lowerCamel);
		String pkg = comp.getJavaRootPackage()+'.'+comp.getPkg();
		String props = comp.getAttributes();
		List<AttribEntry> attribs = JasperUtils.readParametersAsList(props, ctx);
		JavaDataObjectType newType = new JavaDataObjectType(name,pkg+'.'+name,ctx.getBuildContext());
		if (name.equals(lowerCamel)) {
			throw new JasperException("Type name '"+name+"' is not a valid name for a data object");
		}

		src.getSrc().setName(name);
		src.getSrc().setPackage(pkg);

		src.getSrc().addMethod().setConstructor(true).setBody("").setPublic();

		MethodSource<?> constructor = src.getSrc().addMethod().setConstructor(true).setPublic();
		JavaCode constructorCode = new JavaCode();

		for(AttribEntry a : attribs) {
			String n = a.getName();
			JavaVariableType type = (JavaVariableType)a.getType();
			src.addImport(type);
			src.getSrc().addProperty(type.getClassName(), n);
			newType.addProperty(n, type.getName());
			constructorCode.appendCodeText("this."+n+" = "+n+";\n");
			constructor.addParameter(type.getClassName(), n);
		}

		if (comp.getExtend().trim().length()>0) {
			String ex = comp.getExtend();
			JavaDataObjectType exType = JasperUtils.getType(JavaDataObjectType.class, ex, ctx);
			for(String a : exType.getAttributeNames()) {
				String aTypeName = exType.getAttributeType(a);
				JavaVariableType aType = JasperUtils.getType(JavaVariableType.class, aTypeName, ctx);
				if (newType.getAttributeType(a)!=null) {
					if (!newType.getAttributeType(a).equals(aTypeName)) {
						throw new JasperException("Found inconsistent types for system attribute '"+a+"'");
					}
				} else {
					String upperCamel = JasperUtils.getUpperCamelName(a);
					constructor.addParameter(aType.getClassName(), a);
					src.addImport(aType);
					//constructor.addParameter(aType.getImport(), a);
					constructorCode.appendCodeText("super.set"+upperCamel+"("+a+");\n");
					newType.addProperty(a, aTypeName);
				}
			}
			newType.getSuperTypes().add(ex);
			if (exType.getIsInterface()) {
				src.getSrc().addInterface(exType.getImport());
			} else {
				src.getSrc().setSuperType(exType.getImport());
			}
		}
		
		ctx.addVariableType(newType);
		constructor.setBody(constructorCode.getCodeText());
		
		ctx.addSystemAttribute(lowerCamel, name);
		ctx.addSystemAttribute(lowerCamel+"List", "list/"+name);
		ctx.addSystemAttribute(multiple, "list/"+name);
		
		ctx.addSourceFile(src);
	}

}
