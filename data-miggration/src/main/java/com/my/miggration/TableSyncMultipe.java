package com.my.miggration;

import java.sql.Connection;
import java.sql.DatabaseMetaData;

import org.apache.commons.dbcp.BasicDataSource;

public class TableSyncMultipe extends TableSyncBase{
	
	public TableSyncMultipe(BasicDataSource sourceDataSource, BasicDataSource targetDataSource, DataSyncSessionParameter sessionParameter) {
		super(sourceDataSource, targetDataSource, sessionParameter);
	}

	protected void buildTableList(String tableName, Connection sourceConn, DatabaseMetaData targetMetaData){
		super.buildTableList(tableName, sourceConn, targetMetaData);
	}

}
