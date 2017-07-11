package com.my.miggration;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.lang3.StringUtils;

public class DataSyncMiltiple extends DataSyncBase implements Runnable{
	
	private final int threadIndex;
	private final int selectCount;

	public DataSyncMiltiple(BasicDataSource sourceDataSource, BasicDataSource targetDataSource, DataSyncSessionParameter sessionParameter, Table table, CountDownLatch beginCountDownLatch, CountDownLatch endCountDownLatch ) {
		super(sourceDataSource, targetDataSource, sessionParameter, table, beginCountDownLatch, endCountDownLatch);
		this.threadIndex = table.getCurrentNum();
		this.selectCount = table.getSelectCount();
	}
	
	
	protected String buildSelectSql(){
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT * FROM ")
		  .append(this.table.getName())
		  .append(" where ORA_HASH(")
		  .append(this.table.getHashKey())
		  .append(",")
		  .append(selectCount)
		  .append(")=?");
		return sb.toString();
	}
	
	protected String buildLogSelectSql(){
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT * FROM ")
		  .append(this.table.getName())
		  .append(" where ORA_HASH(")
		  .append(this.table.getHashKey())
		  .append(",")
		  .append(selectCount)
		  .append(")=")
		  .append(this.threadIndex);
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
	
	protected void setThreadPrameter(PreparedStatement pstmt)throws Exception{
		pstmt.setInt(1, threadIndex);
	}
	
	protected int getThreadIndex(){
		return this.threadIndex;
	}

}
