package com.my.miggration;

import java.sql.Connection;
import java.sql.DatabaseMetaData;

import org.apache.commons.dbcp.BasicDataSource;

public class TableSyncSingle extends TableSyncBase{
	private String tableName;
	public TableSyncSingle(BasicDataSource sourceDataSource, BasicDataSource targetDataSource, DataSyncSessionParameter sessionParameter, String tableName) {
		super(sourceDataSource, targetDataSource, sessionParameter);
		this.tableName = tableName;
	}
	
	protected void buildTableList(String tableName, Connection sourceConn, DatabaseMetaData targetMetaData){
		if(!tableName.equalsIgnoreCase(this.tableName)){
			return;
		}
		super.buildTableList(tableName, sourceConn, targetMetaData);
	}

}
