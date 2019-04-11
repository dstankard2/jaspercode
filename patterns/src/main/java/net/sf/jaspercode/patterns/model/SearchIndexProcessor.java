package net.sf.jaspercode.patterns.model;

import java.util.List;

import org.jboss.forge.roaster.model.source.JavaClassSource;

import net.sf.jaspercode.api.AttribEntry;
import net.sf.jaspercode.api.ComponentProcessor;
import net.sf.jaspercode.api.JasperUtils;
import net.sf.jaspercode.api.ProcessorContext;
import net.sf.jaspercode.api.annotation.Plugin;
import net.sf.jaspercode.api.annotation.Processor;
import net.sf.jaspercode.api.config.Component;
import net.sf.jaspercode.api.exception.JasperException;
import net.sf.jaspercode.api.types.ServiceOperation;
import net.sf.jaspercode.langsupport.java.JavaClassSourceFile;
import net.sf.jaspercode.langsupport.java.JavaCode;
import net.sf.jaspercode.langsupport.java.JavaUtils;
import net.sf.jaspercode.langsupport.java.types.ServiceLocator;
import net.sf.jaspercode.langsupport.java.types.impl.JavaServiceType;
import net.sf.jaspercode.patterns.xml.model.SearchIndex;

@Plugin
@Processor(componentClass = SearchIndex.class)
public class SearchIndexProcessor implements ComponentProcessor {
	private SearchIndex comp = null;
	private ProcessorContext ctx = null;
	
	@Override
	public void init(Component component, ProcessorContext ctx) {
		this.comp = (SearchIndex)component;
		this.ctx = ctx;
	}

	@Override
	public void process() throws JasperException {
		ctx.setLanguageSupport("Java8");
		ServiceLocator loc = JasperUtils.getTypeForSystemAttribute(ServiceLocator.class, comp.getJpaDaoFactoryRef(), ctx);
		String entity = comp.getEntity();
		String serviceName = entity+"Dao";
		
		if (!loc.getAvailableServices().contains(serviceName)) {
			throw new JasperException("Couldn't find DAO for entity '"+entity+"'");
		}
		
		JavaServiceType daoType = JasperUtils.getType(JavaServiceType.class, serviceName, ctx);
		JavaClassSourceFile src = JavaUtils.getClassSourceFile(daoType.getImport(), ctx, false);
		String name = comp.getName();
		Boolean multiple = comp.getMultiple();
		String paramString = comp.getParams();
		String query = comp.getQueryString();

		ServiceOperation op = new ServiceOperation(name);
		JavaCode code = new JavaCode();
		if (multiple==Boolean.TRUE) {
			op.returnType("list/"+entity);
		} else {
			op.returnType(entity);
		}
		List<AttribEntry> attribs = JasperUtils.readParametersAsList(paramString, ctx);
		String resultStr = (multiple) ? "getResultList()" : "getSingleResult()";
		code.appendCodeText("return getEntityManager().createQuery(\""+query+"\","+entity+".class)");
		
		for(AttribEntry attrib : attribs) {
			op.addParam(attrib.getName(), attrib.getType().getName());
			code.appendCodeText(".setParameter(\""+attrib.getName()+"\","+attrib.getName()+")");
		}
		code.appendCodeText("."+resultStr+";\n");
		JavaClassSource cl = src.getSrc();
		JavaUtils.addServiceOperation(op, code, cl, ctx);
	}
	
}

