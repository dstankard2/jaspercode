package net.sf.jaspercode.patterns.model;

import java.util.ArrayList;
import java.util.List;

public class TableInfo {

	private List<ColumnInfo> columns = new ArrayList<>();
	
	private String tableName;
	
	private String entityName;
	
	private String schema = null;
	
	private String engine = null;
	
	private List<IndexInfo> indices = new ArrayList<>();

	public TableInfo() {
	}

	public TableInfo(List<ColumnInfo> columns, String tableName, String entityName, String schema, String engine,
			List<IndexInfo> indices) {
		super();
		this.columns = columns;
		this.tableName = tableName;
		this.entityName = entityName;
		this.schema = schema;
		this.engine = engine;
		this.indices = indices;
	}

	public List<ColumnInfo> getColumns() {
		return columns;
	}

	public void setColumns(List<ColumnInfo> columns) {
		this.columns = columns;
	}

	public String getEngine() {
		return engine;
	}

	public void setEngine(String engine) {
		this.engine = engine;
	}

	public List<IndexInfo> getIndices() {
		return indices;
	}

	public void setIndices(List<IndexInfo> indices) {
		this.indices = indices;
	}

	public String getSchema() {
		return schema;
	}

	public void setSchema(String schema) {
		this.schema = schema;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getEntityName() {
		return entityName;
	}

	public void setEntityName(String entityName) {
		this.entityName = entityName;
	}
	
}
