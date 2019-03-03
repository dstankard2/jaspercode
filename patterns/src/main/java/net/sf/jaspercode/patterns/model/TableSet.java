package net.sf.jaspercode.patterns.model;

import java.util.ArrayList;
import java.util.List;

public class TableSet {

	private DatabaseType database = null;
	
	private List<TableInfo> tableInfos = new ArrayList<>();

	public DatabaseType getDatabase() {
		return database;
	}

	public void setDatabase(DatabaseType database) {
		this.database = database;
	}

	public List<TableInfo> getTableInfos() {
		return tableInfos;
	}

	public void setTableInfos(List<TableInfo> tableInfos) {
		this.tableInfos = tableInfos;
	}
	
}
