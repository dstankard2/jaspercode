package net.sf.jaspercode.patterns.model;

import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.MethodSource;

import net.sf.jaspercode.api.ComponentProcessor;
import net.sf.jaspercode.api.JasperUtils;
import net.sf.jaspercode.api.ProcessorContext;
import net.sf.jaspercode.api.annotation.Plugin;
import net.sf.jaspercode.api.annotation.Processor;
import net.sf.jaspercode.api.config.Component;
import net.sf.jaspercode.api.exception.JasperException;
import net.sf.jaspercode.langsupport.java.JavaClassSourceFile;
import net.sf.jaspercode.patterns.model.types.impl.EntityManagerType;
import net.sf.jaspercode.patterns.xml.model.ThreadLocalEntityManager;

@Plugin
@Processor(componentClass = ThreadLocalEntityManager.class)
public class ThreadLocalEntityManagerProcessor implements ComponentProcessor {

	private ThreadLocalEntityManager comp = null;
	private ProcessorContext ctx = null;

	@Override
	public void init(Component component, ProcessorContext ctx) {
		this.comp = (ThreadLocalEntityManager)component;
		this.ctx = ctx;
	}

	public void process() throws JasperException {
		ctx.setLanguageSupport("Java8");
		String pkg = comp.getFullPackage();
		String pu = null;
		String txRef = comp.getTxRef();
		String emTypeName = ctx.getSystemAttribute(txRef);
		
		if (emTypeName==null) {
			throw new JasperException("Reference '"+txRef+"' is not a valid txRef");
		}

		String className = comp.getName();
		JavaClassSourceFile src = new JavaClassSourceFile(ctx);
		JavaClassSource cl = src.getSrc();
		cl.addImport("javax.persistence.EntityManager");
		cl.addImport("javax.persistence.EntityManagerFactory");
		cl.addImport("javax.persistence.Persistence");
		EntityManagerType emType = JasperUtils.getType(EntityManagerType.class, emTypeName, ctx);
		pu = emType.getPersistenceUnitName();
		
		cl.setPackage(pkg);
		cl.setName(className);
		cl.addField().setPrivate().setName("factory").setLiteralInitializer("factoryInit();\n").setStatic(true).setFinal(true).setType("EntityManagerFactory");

		cl.addField().setPrivate().setName("currentEm").setLiteralInitializer("new ThreadLocal<>();").setStatic(true).setType("ThreadLocal<EntityManager>");

		MethodSource<JavaClassSource> factoryInit = cl.addMethod().setName("factoryInit").setPrivate().setStatic(true);
		factoryInit.setReturnType("EntityManagerFactory");
		factoryInit.setBody("EntityManagerFactory ret = null;\n"
				+ "try {\n"
				+ "ret = Persistence.createEntityManagerFactory(\""+pu+"\");\n"
						+ "} catch(Throwable e) {\ne.printStackTrace();\n}\n"
						+ "return ret;\n");
		
		StringBuilder b = new StringBuilder();
		b.append("EntityManager ret = null;\nret = currentEm.get();\nif (ret==null) {\n");
		b.append("ret = factory.createEntityManager();\nret.getTransaction().begin();\n");
		b.append("currentEm.set(ret);\n");
		b.append("}\nreturn ret;\n");
		cl.addMethod().setName("getEntityManager").setPublic().setStatic(true).setBody(b.toString()).setReturnType("EntityManager");

		b = new StringBuilder();
		b.append("EntityManager em = currentEm.get();\n");
		b.append("if (em!=null) {\ncurrentEm.remove();\n");
		b.append("if (em.getTransaction().isActive()) {\nif (em.getTransaction().getRollbackOnly()) {\n");
		b.append("em.getTransaction().rollback();\n} else {\n");
		b.append("em.getTransaction().commit();\n}\n}\nem.close();\n}\n");
		cl.addMethod().setName("releaseEntityManager").setPublic().setStatic(true).setBody(b.toString());

		ctx.addVariableType(new ThreadLocalTxLocatorType(pkg, className, ctx.getBuildContext(),emType));
		ctx.addSourceFile(src);
	}

}

