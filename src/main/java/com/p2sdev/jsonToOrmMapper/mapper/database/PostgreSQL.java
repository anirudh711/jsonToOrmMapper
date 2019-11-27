package com.p2sdev.jsonToOrmMapper.mapper.database;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.p2sdev.jsonToOrmMapper.convert.JSONTypes;
import com.p2sdev.jsonToOrmMapper.convert.Table;

/**
 * Class to generate Create and insert statements.
 * By default all classes will have id as their primary key
 * 
 * @author p2sdev.com
 * @version 2019-11-27
 *
 */
public class PostgreSQL implements Database {

	public enum ColumnType {
		VARCHAR_40("varchar(40)"), VARCHAR_100("varchar(100)"), INTEGER("integer"), DATE("date"),
		TIME("time"), BOOLEAN("bool"), FLOAT8("float8");
		private String value;
		private ColumnType(String value) {
			this.value = value;
		}
		
		public String getValue() {
			return value;
		}
	}

	public enum ColumnConstraint {
		PRIMARY_KEY("PRIMARY KEY"), IDENTITY("GENERATED BY DEFAULT AS IDENTITY"), NOT_NULL("NOT NULL"), 
		NULL("NULL"), UNIQUE("UNIQUE"), REFERENCES("REFERENCES");
		private String value;
		private ColumnConstraint(String value) {
			this.value = value;
		}
		
		public String getValue() {
			return value;
		}
	}
	
	public enum TableConstraint {
		PRIMARY_KEY("PRIMARY KEY"), FOREIGN_KEY("FOREIGN KEY"), REFERENCES("REFERENCES"), 
		CONSTRAINT("CONSTRAINT"), DELETE_CASCADE("ON DELETE CASCADE");
		private String value;
		private TableConstraint(String value) {
			this.value = value;
		}
		
		public String getValue() {
			return value;
		}
	}

	@Override
	public String getSQL(List<Table> tables) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getSQLCreate(List<Table> tables) {

		Map<String, StringBuilder> tablesSQLMap = new HashMap<>();
		
		for(Table table : tables) {
			StringBuilder tableDefinition = new StringBuilder("");
			tableDefinition.append(getCreateStatementOpen(table.getTableName()))
				.append(getColumns(table))
				.append(getCreateStatementClose());
			tablesSQLMap.put(table.getTableName(), tableDefinition);
		}
		
		StringBuilder sql = new StringBuilder();
		// setting tables constraints
		for(Table table : tables) {
			setRelationships(tablesSQLMap, table);
			sql.append(tablesSQLMap.get(table.getTableName()).toString())
				.append("\n");
		}
		
		return sql.toString();
	}
	
	@Override
	public String getSQLInsert(List<Table> tables) {
		// TODO Auto-generated method stub
		return null;
	}
	
	/**
	 * Explicitly called by getSQLCreate before it returns to set
	 * tables constraints
	 * @param sql
	 */
	private void setRelationships(Map<String, StringBuilder> tablesSQLMap, 
			Table table) {
		for(String tableName : table.getRelationships().keySet()) {
			StringBuilder createStatement = tablesSQLMap.get(tableName);
			int length;
			StringBuilder constraint;
			
			switch(table.getRelationships().get(tableName)) {
				case MANYTOMANY:
					
					break;
				case ONETOMANY:
					constraint = new StringBuilder();
					constraint
						.append(",\n\t"+table.getTableName()+"_id ")
						.append(ColumnType.INTEGER.getValue())
						.append(" "+ColumnConstraint.NOT_NULL.getValue()+", \n\t");
					
					constraint
						.append(TableConstraint.FOREIGN_KEY.getValue())
						.append(" ("+tableName+"_id)")
						.append(" "+TableConstraint.REFERENCES)
						.append(" "+table.getTableName()+"("+table.getTableName()+"_id)")
						.append(" "+TableConstraint.DELETE_CASCADE.getValue());
					
					length = createStatement.length();
					createStatement.replace(length-6, length-5, constraint.toString());
					
					break;
				case ONETOONE:
					constraint = new StringBuilder();
					constraint
						.append(",\n\t"+table.getTableName()+"_id ")
						.append(ColumnType.INTEGER.getValue())
						.append(" "+ColumnConstraint.NOT_NULL.getValue()+", \n\t");
					
					constraint.append(TableConstraint.CONSTRAINT.getValue()+" ")
						.append("fk_"+table.getTableName()+"_id"+" ")
						.append(TableConstraint.FOREIGN_KEY.getValue())
						.append(" ("+table.getTableName()+"_id)")
						.append(" "+TableConstraint.REFERENCES)
						.append(" "+table.getTableName()+"("+table.getTableName()+"_id)")
						.append(" "+TableConstraint.DELETE_CASCADE.getValue());
					
					length = createStatement.length();
					createStatement.replace(length-6, length-5, constraint.toString());
					
					break;
				default:
					break;
			}
			
		}
		
	}
	
	private String getCreateStatementOpen(String t_name) {
		StringBuilder opening = new StringBuilder("CREATE TABLE ");
		opening.append(t_name).append(" (\n\t");
		return opening.toString();
	}
	
	private String getCreateStatementClose() {
		return ");\n";
	}
	
	private String primaryKey(String t_name) {
		StringBuilder pkeyColumn = new StringBuilder(t_name);
		pkeyColumn.append("_id")
			.append(" ")
			.append(ColumnType.INTEGER.getValue())
			.append(" ")
			.append(ColumnConstraint.PRIMARY_KEY.getValue())
			.append(" ")
			.append(ColumnConstraint.IDENTITY.getValue())
			.append(", \n\t");
		
		return pkeyColumn.toString();
	}
	
	private String getColumns(Table table) {
		
		StringBuilder columns = new StringBuilder("");
		columns.append(primaryKey(table.getTableName()));
		Map<String, JSONTypes> columnDef = table.getTableColumnsDef();
		
		for(String key : columnDef.keySet()) {
			
			// the column looks like this: "[column_name] [column_type] [column_constraint],\n"
			
			switch(columnDef.get(key)) {
				case STRING:
					columns.append(key).append(" ")
					.append(ColumnType.VARCHAR_100.getValue())
					.append(",\n\t");
					break;
				case INTEGER:
					columns.append(key).append(" ")
					.append(ColumnType.INTEGER.getValue())
					.append(",\n\t");
					break;
				case BOOLEAN:
					columns.append(key).append(" ")
					.append(ColumnType.BOOLEAN.getValue())
					.append(",\n\t");
					break;
				case CHARACTER:
					break;
				case DATE:
					columns.append(key).append(" ")
					.append(ColumnType.DATE.getValue())
					.append(",\n\t");
					break;
				case JSONARRAY:
					// use setRelationships() after all tables are created
					break;
				case JSONOBJECT:
					// use setRelationships() after all tables are created
					break;
				default:
					break;
			}
			
		}
		
		// removing last comma
		int length = columns.length();
		columns.replace(length-3, length-2, "");
		columns.append("\n");
		return columns.toString();
	}

}

