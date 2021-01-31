package net.sf.jaspercode.patterns.model;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.jboss.forge.roaster.model.source.JavaClassSource;

import net.sf.jaspercode.api.ComponentProcessor;
import net.sf.jaspercode.api.JasperUtils;
import net.sf.jaspercode.api.ProcessorContext;
import net.sf.jaspercode.api.annotation.Processor;
import net.sf.jaspercode.api.config.Component;
import net.sf.jaspercode.api.exception.JasperException;
import net.sf.jaspercode.langsupport.java.JavaClassSourceFile;
import net.sf.jaspercode.langsupport.java.JavaUtils;
import net.sf.jaspercode.langsupport.java.types.JavaVariableType;
import net.sf.jaspercode.langsupport.java.types.impl.JavaDataObjectType;
import net.sf.jaspercode.patterns.xml.model.SqlQuery;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectBody;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;

//@Plugin
@Processor(componentClass = SqlQuery.class)
public class SqlQueryProcessor implements ComponentProcessor {
	private SqlQuery comp = null;
	private ProcessorContext ctx = null;
	
	@Override
	public void init(Component component, ProcessorContext ctx) {
		this.comp = (SqlQuery)component;
		this.ctx = ctx;
	}

	@Override
	public void process() throws JasperException {
		ctx.setLanguageSupport("Java8");
		if (comp.getQuery().trim().length()==0) {
			throw new JasperException("No query string found for sqlQuery component");
		}

		CCJSqlParserManager pm = new CCJSqlParserManager();
		Statement stmt = null;
		try {
			stmt = pm.parse(new StringReader(comp.getQuery()));
		} catch(JSQLParserException e) {
			throw new JasperException("Couldn't parse SQL query", e);
		}

		EntityManagerLocator loc = 
				JasperUtils.getType(EntityManagerLocator.class, comp.getEmLocator(), ctx);
		JavaClassSourceFile src = null;
		String serviceName = comp.getService();
		String ruleName = comp.getRuleName();
		
		String pkg = JavaUtils.getJavaPackage(comp, ctx);
		src = JavaUtils.getClassSourceFile(pkg+'.'+serviceName, ctx);
		JavaClassSource cl = src.getSrc();

		if (stmt instanceof Select) {
			Select sel = (Select)stmt;
			handleSelect(sel,cl,pkg);
		} else {
			throw new JasperException("NativeQuery component only supports select statements for now");
		}
		
		ctx.getLog().warn("TODO: Process SqlQuery component");
	}

	protected void handleSelect(Select sel, JavaClassSource cl,String pkg) throws JasperException {
		String dataObjName = comp.getResultType();
		JavaDataObjectType dataType = new JavaDataObjectType(dataObjName,pkg+'.'+dataObjName,ctx.getBuildContext());
		JavaClassSourceFile dataObjSrc = new JavaClassSourceFile(ctx);
		ctx.addVariableType(dataType);
		dataObjSrc.getSrc().setPackage(pkg);
		dataObjSrc.getSrc().setName(dataObjName);
		ctx.addSourceFile(dataObjSrc);
		SelectBody body = sel.getSelectBody();
		List<String> attributes = new ArrayList<>();

		if (body instanceof PlainSelect) {
			PlainSelect ps = (PlainSelect)body;
			List<SelectItem> selectItems = ps.getSelectItems();
			for(SelectItem item : selectItems) {
				String name = null;
				String typeName = null;
				if (item instanceof SelectExpressionItem) {
					SelectExpressionItem i = (SelectExpressionItem)item;
					if (i.getAlias()!=null) {
						name = i.getAlias().getName();
					} else if (i.getExpression() instanceof Column) {
						Column c = (Column)i.getExpression();
						name = c.getColumnName();
					}
				}
				if (name==null) {
					throw new JasperException("Couldn't find property name from select item '"+item.toString()+"'");
				}
				typeName = ctx.getSystemAttribute(name);
				if (typeName==null) {
					throw new JasperException("Found no system attribute named '"+name+"'");
				}
				attributes.add(name);
			}
			Expression whereEx = ps.getWhere();
		} else {
			throw new JasperException("The select statement is not specified - it is not a PlainSelect");
		}

		for(String name : attributes) {
			String typeName = ctx.getSystemAttribute(name);
			JavaVariableType type = JasperUtils.getType(JavaVariableType.class, typeName, ctx);
			if (type.getImport()!=null) {
				dataObjSrc.addImport(type);
			}
			dataObjSrc.getSrc().addProperty(type.getClassName(), name);
			dataType.addProperty(name, typeName);
		}

		System.out.println("Hi");
	}
	
}
