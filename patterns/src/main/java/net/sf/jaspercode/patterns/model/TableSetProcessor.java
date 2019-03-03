package net.sf.jaspercode.patterns.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import net.sf.jaspercode.api.ComponentProcessor;
import net.sf.jaspercode.api.JasperException;
import net.sf.jaspercode.api.ProcessorContext;
import net.sf.jaspercode.api.annotation.Plugin;
import net.sf.jaspercode.api.annotation.Processor;
import net.sf.jaspercode.api.config.Component;
import net.sf.jaspercode.patterns.xml.model.TableSetComponent;

/**
 * A table set in a MySQL database
 * @author DCS
 */
@Plugin
@Processor(componentClass = TableSetComponent.class)
public class TableSetProcessor implements ComponentProcessor {

	private TableSetComponent comp = null;
	private ProcessorContext ctx = null;
	
	@Override
	public void init(Component component, ProcessorContext ctx) {
		this.comp = (TableSetComponent)component;
		this.ctx = ctx;
	}

	@Override
	public void process() throws JasperException {
		TableSet schema = new TableSet();
		String id = comp.getId();
		String schemaName = comp.getDbSchema();
		List<TableInfo> tableInfos = null;
		Connection conn = null;

		schema.setDatabase(DatabaseType.MYSQL);
		ctx.getLog().info("Reading table set '"+id+"'");
		try {
			conn = ModelUtils.connectToDatabase(ctx);
			tableInfos = getTableInfos(conn,schemaName);
			for(TableInfo info : tableInfos) {
				populateTableInfo(conn,info);
			}
			schema.setTableInfos(tableInfos);
			ModelUtils.saveSchemaInfo(schema, id, ctx);
			ctx.setObject("Schema_"+id, schema);
		} finally {
			if (conn!=null) {
				try {
					conn.close();
				} catch(Exception e) { }
			}
		}
	}
	
	private void populateTableInfo(Connection conn,TableInfo tableInfo) throws JasperException {
		PreparedStatement stmt = null;
		ResultSet res = null;
		String tableName = tableInfo.getTableName();
		String schemaName = tableInfo.getSchema();

		try {
			// select table_schema,table_name,column_name,is_nullable,data_type,column_default,column_type,column_key from information_schema.columns where table_schema = 'webwizard' and table_name = 'User' order by ordinal_position;
			stmt = conn.prepareStatement(
					"select column_name,is_nullable,data_type,column_default,column_type,column_key,extra "
					+ "from information_schema.columns where table_schema = ? and table_name = ? "
					+ "order by ordinal_position");
			stmt.setString(1, schemaName);
			stmt.setString(2, tableName);
			res = stmt.executeQuery();
			while(res.next()) {
				String col = res.getString(1);
				String nullable = res.getString(2);
				String dataType = res.getString(3);
				String colType = res.getString(5);
				String colKey = res.getString(6);
				String extra = res.getString(7);
				String attrName = getAttributeName(col);
				String attrType = getAttributeType(attrName,dataType);

				boolean autoGenerate = false;
				if ((extra!=null) && (extra.equals("auto_increment"))) {
					autoGenerate = true;
				}
				
				ColumnInfo c = new ColumnInfo(col,colType,null,true,false,attrName,attrType,autoGenerate);
				if ((nullable!=null) && (nullable.equals("NO"))) {
					c.setNullable(false);
				}
				if ((colKey!=null) && (colKey.equals("PRI"))) {
					c.setPrimaryKey(true);
				}
				tableInfo.getColumns().add(c);
			}
		} catch(SQLException e) {
			throw new JasperException("Couldn't read table column names for table '"+tableName+"'",e);
		} finally {
			if (res!=null) {
				try {
					res.close();
				} catch(Exception e) { }
			}
			if (stmt!=null) {
				try {
					stmt.close();
				} catch(Exception e) { }
			}
		}
		
		try {
			stmt = conn.prepareStatement("select index_name, column_name, non_unique "
					+ "from information_schema.statistics "
					+ "where table_name = ? and table_schema = ? and index_name <> 'PRIMARY' "
					+ "order by index_name,seq_in_index");
			stmt.setString(1, tableName);
			stmt.setString(2, schemaName);
			res = stmt.executeQuery();
			IndexInfo ind = null;
			while(res.next()) {
				String indexName = res.getString(1);
				String col = res.getString(2);
				//Integer seq = res.getInt(3);
				Boolean nonUnique = res.getBoolean(3);
				if (ind==null) {
					ind = new IndexInfo(indexName,new ArrayList<>(),!nonUnique);
					tableInfo.getIndices().add(ind);
					ind.getColumns().add(col);
				} else if (ind.getName().equals(indexName)) {
					ind.getColumns().add(col);
				} else {
					ind = new IndexInfo(indexName,new ArrayList<>(),!nonUnique);
					tableInfo.getIndices().add(ind);
					ind.getColumns().add(col);
				}
			}
		} catch(SQLException e) {
			throw new JasperException("Couldn't read table column names for table '"+tableName+"'",e);
		} finally {
			if (res!=null) {
				try {
					res.close();
				} catch(Exception e) { }
			}
			if (stmt!=null) {
				try {
					stmt.close();
				} catch(Exception e) { }
			}
		}
	}
	
	private List<TableInfo> getTableInfos(Connection conn,String schemaName) throws JasperException {
		PreparedStatement stmt = null;
		ResultSet res = null;
		List<TableInfo> ret = new ArrayList<>();
		
		try {
			stmt = conn.prepareStatement("select table_schema, table_name, engine from information_schema.tables where table_schema = ?");
			stmt.setString(1, schemaName);
			res = stmt.executeQuery();
			while(res.next()) {
				TableInfo i = new TableInfo();
				String schema = res.getString(1);
				String name = res.getString(2);
				String engine = res.getString(3);
				String entityName = getEntityName(name);
				i.setEngine(engine);
				i.setTableName(name);
				i.setSchema(schema);
				ret.add(i);
				i.setEntityName(entityName);
			}
			if (ret.size()==0) {
				throw new JasperException("Found no tables associated with tableSet");
			}
		} catch(SQLException e) {
			throw new JasperException("Couldn't read table names from schema '"+schemaName+"'",e);
		} finally {
			if (res!=null) {
				try {
					res.close();
				} catch(Exception e) { }
			}
			if (stmt!=null) {
				try {
					stmt.close();
				} catch(Exception e) { }
			}
		}
		
		return ret;
	}

	private String getEntityName(String tableName) {
		return tableName;
	}
	
	private String getAttributeName(String columnName) {
		return columnName;
	}
	
	private String getAttributeType(String attribName,String colType) {
		String attributeType = ctx.getSystemAttribute(attribName);
		if (attributeType==null) {
			if (colType.indexOf("smallint")==0) return "integer";
			if (colType.indexOf("mediumint")==0) return "integer";
			if (colType.indexOf("bigint")==0) return "longint";
			if (colType.indexOf("int")==0) return "integer";
			else if (colType.equals("varchar")) return "string";
			else if (colType.equals("text")) return "string";
			else if (colType.equals("longtext")) return "string";
			return colType;
		} else {
			return attributeType;
		}
	}
	
}
