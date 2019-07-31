package net.sf.jaspercode.patterns.model;

import java.util.Arrays;
import java.util.List;

import org.dom4j.Element;
import org.jboss.forge.roaster.model.source.AnnotationSource;
import org.jboss.forge.roaster.model.source.FieldSource;
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
import net.sf.jaspercode.langsupport.java.JavaUtils;
import net.sf.jaspercode.langsupport.java.types.JavaVariableType;
import net.sf.jaspercode.langsupport.java.types.impl.JavaDataObjectType;
import net.sf.jaspercode.langsupport.java.types.impl.JavaEnumType;
import net.sf.jaspercode.patterns.files.XmlFile;
import net.sf.jaspercode.patterns.model.types.impl.EntityManagerType;
import net.sf.jaspercode.patterns.xml.model.PersistenceUnit;

@Plugin
@Processor(componentClass = PersistenceUnit.class)
public class PersistenceUnitProcessor implements ComponentProcessor {

	private static final List<String> jpaImplementations = Arrays.asList("hibernate");

	private PersistenceUnit persistenceUnit = null;
	private ProcessorContext ctx = null;
	
	@Override
	public void init(Component component, ProcessorContext ctx) {
		this.persistenceUnit = (PersistenceUnit)component;
		this.ctx = ctx;
	}

	@Override
	public void process() throws JasperException {
		String tableSetId = persistenceUnit.getTableSetId();
		XmlFile persistenceXml = null;
		Element root = null;
		Element pu = null;
		String txRef = persistenceUnit.getTxRef();
		String puName = persistenceUnit.getName();
		ctx.setLanguageSupport("Java8");

		TableSet tableSet = ModelUtils.getTableSet(tableSetId, ctx);

		ctx.getLog().info("Processing persistence unit '"+persistenceUnit.getName()+"'");

		persistenceXml = ModelUtils.getPersistenceXml(ctx);
		root = persistenceXml.getDocument().getRootElement();

		String jpaImplementation = ctx.getProperty("java.model.jpaImplementation");
		if (jpaImplementation==null) {
			throw new JasperException("Configuration property 'java.model.jpaImplementation' is required");
		} else if (!jpaImplementations.contains(jpaImplementation)) {
			throw new JasperException("Configuration property 'java.model.jpaImplementation' must be set to one of: "+jpaImplementations.toString());
		}

		pu = root.addElement("persistence-unit");
		pu.addAttribute("transaction-type", "RESOURCE_LOCAL");
		pu.addAttribute("name", puName);

		if (jpaImplementation.equals("hibernate")) pu.addElement("provider").setText("org.hibernate.jpa.HibernatePersistenceProvider");

		if (ctx.getProperty("model.jndiDataSource")!=null) {
			String name = ctx.getProperty("model.jndiDataSource");
			pu.addElement("non-jta-data-source").setText(name);
		}
		
		for(TableInfo ti : tableSet.getTableInfos()) {
			addEntity(persistenceUnit,ti,pu,ctx);
		}
		
		Element props = pu.addElement("properties");
		Element dialect = props.addElement("property");
		dialect.addAttribute("name", "hibernate.dialect");
		if (tableSet.getDatabase()==DatabaseType.MYSQL) {
			// Add MySQL dialect and dependencies
			dialect.addAttribute("value","org.hibernate.dialect.MySQLDialect");
			ctx.getBuildContext().addDependency("mysql-connector");
		}

		ctx.getBuildContext().addDependency("jpa");
		ctx.getBuildContext().addDependency(jpaImplementation);

		ModelUtils.enableJpa(ctx);
		EntityManagerType emType = new EntityManagerType(puName,tableSet,ctx.getBuildContext());
		ctx.addSystemAttribute(txRef, emType.getName());
		ctx.addVariableType(emType);
	}

	protected void addEntity(PersistenceUnit persistenceUnit,TableInfo tableInfo,Element puElt,ProcessorContext ctx) throws JasperException {
		String typeName = tableInfo.getEntityName();
		JavaClassSourceFile entityFile = new JavaClassSourceFile(ctx);
		String pkg = JavaUtils.getJavaPackage(persistenceUnit, ctx);
		JavaDataObjectType objType = new JavaDataObjectType(typeName,pkg+'.'+typeName,ctx.getBuildContext());
		
		entityFile.getSrc().setPackage(pkg);
		entityFile.getSrc().setName(typeName);
		entityFile.getSrc().addAnnotation("javax.persistence.Entity");
		entityFile.getSrc().addAnnotation("javax.persistence.Table")
				.setLiteralValue("name", "\""+tableInfo.getTableName()+"\"")
				.setLiteralValue("schema", "\""+tableInfo.getSchema()+"\"");
		entityFile.getSrc().addMethod().setConstructor(true).setPublic().setBody("");
		MethodSource<JavaClassSource> constructor = entityFile.getSrc().addMethod().setConstructor(true).setPublic();
		StringBuilder constructorCode = new StringBuilder();
		for(ColumnInfo col : tableInfo.getColumns()) {
			String enumType = null;
			String name = col.getAttributeName();
			String attrType = col.getAttributeType();
			String colName = col.getName();
			String colType = col.getType();
			ctx.addSystemAttribute(name, attrType);
			ctx.originateSystemAttribute(name);
			String colClass = null;

			objType.addProperty(name, colType);
			if (attrType.equals("string")) colClass = "String";
			else if (attrType.equals("integer")) colClass = "Integer";
			else if (attrType.equals("datetime")) colClass = "java.sql.Timestamp";
			else if (attrType.equals("date")) colClass = "java.util.Date";
			else if (attrType.equals("longint")) colClass = "Long";
			else if (attrType.equals("boolean")) colClass = "Boolean";
			else {
				// This may be an enumeration
				JavaVariableType t = JasperUtils.getType(JavaVariableType.class, attrType, ctx);
				if (t instanceof JavaEnumType) {
					colClass = t.getImport();
					if (colType.indexOf("int")>=0) {
						enumType = "javax.persistence.EnumType.ORDINAL";
					} else {
						enumType = "javax.persistence.EnumType.STRING";
					}
					//isEnum = true;
				}
				else throw new JasperException("Couldn't find entity property type for attribute type '"+name+"' - column type was '"+colType+"'");
			}
			boolean nullible = col.isNullable();
			entityFile.getSrc().addProperty(colClass, name);

			constructor.addParameter(colClass, name);
			constructorCode.append("this."+name+" = "+name+";\n");
			FieldSource<JavaClassSource> field = entityFile.getSrc().getField(name);
			AnnotationSource<JavaClassSource> an = field.addAnnotation();
			an.setName("javax.persistence.Column")
					.setLiteralValue("name", "\""+colName+"\"");
			if (!nullible) {
				an.setLiteralValue("nullable","false");
			}
			if (enumType!=null) {
				AnnotationSource<JavaClassSource> a2 = field.addAnnotation().setName("javax.persistence.Enumerated");
				a2.setLiteralValue(enumType);
			}
			if (col.isPrimaryKey()) {
				field.addAnnotation("javax.persistence.Id");
			}
			if (col.isAutoGenerate()) {
				field.addAnnotation("javax.persistence.GeneratedValue").setLiteralValue("strategy", "javax.persistence.GenerationType.AUTO");
			}
		}
		constructor.setBody(constructorCode.toString());
		ctx.addVariableType(objType);
		ctx.addSourceFile(entityFile);

		String attribName = JasperUtils.getLowerCamelName(typeName);
		String multiple = JasperUtils.getMultiple(attribName);

		ctx.addSystemAttribute(attribName, typeName);
		ctx.addSystemAttribute(multiple, "list/"+typeName);

		Element entityClass = puElt.addElement("class");
		entityClass.addText(pkg+'.'+typeName);
	}
}

