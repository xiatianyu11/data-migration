package com.my.miggration;

import java.util.List;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.lang3.StringUtils;

public class DataSyncSingle extends DataSyncBase {

	public DataSyncSingle(BasicDataSource sourceDataSource, BasicDataSource targetDataSource, DataSyncSessionParameter sessionParameter, Table table) {
		super(sourceDataSource, targetDataSource, sessionParameter, table);
	}
	
	public void start(){
		this.run();
	}

	protected String buildSelectSql(){
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT * FROM ")
		  .append(this.table.getName());
		return sb.toString();
	}
	
	protected String buildLogSelectSql(){
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT * FROM ")
		  .append(this.table.getName());
		return sb.toString();
	}
	
	protected String buildInsertSql(List<String> colomnNameList){
		StringBuilder sb = new StringBuilder();
		sb.append("INSERT INTO ")
		  .append(this.table.getName())
		  .append("(")
		  .append(StringUtils.join(colomnNameList, ","))
		  .append(")")
		  .append(" VALUES(");
		for(String columnName : colomnNameList){
			sb.append("?,");
		}
		sb.replace(sb.lastIndexOf(","), sb.length(), ")");
		return sb.toString();
	}
	
	protected int getThreadIndex(){
		return 0;
	}

}
