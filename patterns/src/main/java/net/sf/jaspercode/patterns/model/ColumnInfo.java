package net.sf.jaspercode.patterns.model;

public class ColumnInfo {

	private String name;
	private String type;
	private String fieldSize = null;
	private boolean nullable = false;
	private boolean primaryKey = false;
	private String attributeName;
	private String attributeType;
	private boolean autoGenerate = false;

	public ColumnInfo(String name, String type, String fieldSize, boolean nullable, boolean primaryKey, String attributeName,
			String attributeType,boolean autoGenerate) {
		super();
		this.name = name;
		this.type = type;
		this.fieldSize = fieldSize;
		this.nullable = nullable;
		this.primaryKey = primaryKey;
		this.attributeName = attributeName;
		this.attributeType = attributeType;
		this.autoGenerate = autoGenerate;
	}

	public ColumnInfo() {
	}

	public String getAttributeName() {
		return attributeName;
	}

	public void setAttributeName(String attributeName) {
		this.attributeName = attributeName;
	}

	public String getAttributeType() {
		return attributeType;
	}

	public void setAttributeType(String attributeType) {
		this.attributeType = attributeType;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public boolean isNullable() {
		return nullable;
	}
	public void setNullable(boolean nullable) {
		this.nullable = nullable;
	}
	public boolean isPrimaryKey() {
		return primaryKey;
	}
	public void setPrimaryKey(boolean primaryKey) {
		this.primaryKey = primaryKey;
	}

	public String getFieldSize() {
		return fieldSize;
	}

	public void setFieldSize(String fieldSize) {
		this.fieldSize = fieldSize;
	}

	public boolean isAutoGenerate() {
		return autoGenerate;
	}

	public void setAutoGenerate(boolean autoGenerate) {
		this.autoGenerate = autoGenerate;
	}

}

