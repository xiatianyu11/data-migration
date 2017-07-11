package com.my.miggration;

import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

public abstract class DataSyncBase {
	
	private final Logger inforLogger = Logger.getLogger(this.getClass().getName());
	private Logger failselectLogger = null;

	protected final Table table;
	private final BasicDataSource sourceDataSource;
	private final BasicDataSource targetDataSource;
	private final DataSyncSessionParameter sessionParameter;
	private CountDownLatch beginCountDownLatch;
	private CountDownLatch endCountDownLatch;
	
	
	public DataSyncBase(BasicDataSource sourceDataSource, BasicDataSource targetDataSource, DataSyncSessionParameter sessionParameter, Table table) {
		super();
		this.sourceDataSource = sourceDataSource;
		this.targetDataSource = targetDataSource;
		this.sessionParameter = sessionParameter;
		this.table = table;
		if(StringUtils.isNotEmpty(sessionParameter.getErrorSelOut())){
			failselectLogger = Logger.getLogger(sessionParameter.getErrorSelOut());
		}
		
	}
	

	public DataSyncBase(BasicDataSource sourceDataSource, BasicDataSource targetDataSource, DataSyncSessionParameter sessionParameter, Table table, CountDownLatch beginCountDownLatch, CountDownLatch endCountDownLatch) {
		super();
		this.sourceDataSource = sourceDataSource;
		this.targetDataSource = targetDataSource;
		this.sessionParameter = sessionParameter;
		this.table = table;
		this.beginCountDownLatch = beginCountDownLatch;
		this.endCountDownLatch = endCountDownLatch;
		if(StringUtils.isNotEmpty(sessionParameter.getErrorSelOut())){
			failselectLogger = Logger.getLogger(sessionParameter.getErrorSelOut());
		}
	}

	public void run() {
		// start
		Connection sourceConn = null;
		Connection targetConn = null;
		PreparedStatement targetPstmt = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String exeSql = "";
		try {
			if(beginCountDownLatch != null){ beginCountDownLatch.await(); }
			if(this.getThreadIndex() < 0){ return; }
			sourceConn = this.sourceDataSource.getConnection();
			targetConn = this.targetDataSource.getConnection();
			targetConn.setAutoCommit(false);
			// get the source select data SQL.
			exeSql = this.buildSelectSql();
			String sessionCommand = sessionParameter.getSourceCommand();
			String[] command = null;
			if (null != sessionCommand) {
				command = sessionCommand.split(";");
				// load the session command
				for (int i = 0; i < command.length; i++) {
					pstmt = sourceConn.prepareStatement(command[i]);
					pstmt.execute();
				}
				
			}
            
			pstmt = sourceConn.prepareStatement(exeSql);
			//pstmt.setInt(1, threadIndex);
			this.setThreadPrameter(pstmt);
			rs = pstmt.executeQuery();
			// start to insert the data
			

			int cnt = 0;
			while (rs.next()) {
				ResultSetMetaData rsmd = rs.getMetaData();
				List<String> columnNameList = new ArrayList<String>();
				List<Field> columnValueList = new ArrayList<Field>();
				for (int i = 1; i <= rsmd.getColumnCount(); i++) {
					
					columnNameList.add(rsmd.getColumnName(i));
					
					if (rsmd.getColumnType(i) == Types.VARCHAR || rsmd.getColumnType(i) == Types.CHAR) {
						columnValueList.add(new Field(FieldType.STRING, rs.getString(i)));
					} else if (rsmd.getColumnType(i) == Types.INTEGER || rsmd.getColumnType(i) == Types.TINYINT) {
						columnValueList.add(new Field(FieldType.INT,rs.getInt(i)));
					} else if (rsmd.getColumnType(i) == Types.DATE) {
						columnValueList.add(new Field(FieldType.DATE, rs.getDate(i)));
					} else if (rsmd.getColumnType(i) == Types.DOUBLE) {
						columnValueList.add(new Field(FieldType.DOUBLE,rs.getDouble(i)));
					} else if (rsmd.getColumnType(i) == Types.CLOB) {
						columnValueList.add(new Field(FieldType.CLOB, rs.getClob(i)));
					}else if (rsmd.getColumnType(i) == Types.BLOB) {
						columnValueList.add(new Field(FieldType.BLOB, rs.getBlob(i)));
					} else if (rsmd.getColumnType(i) == Types.TIMESTAMP) {
						columnValueList.add(new Field(FieldType.DATE, rs.getDate(i)));
					} else if (rsmd.getColumnType(i) == Types.NUMERIC
							|| rsmd.getColumnType(i) == Types.BIGINT) {
						columnValueList.add(new Field(FieldType.LONG, rs.getLong(i)));
					}
				}
				String insertSql = this.buildInsertSql(columnNameList);
				targetPstmt = targetConn.prepareStatement(insertSql);
				for(int i = 0; i < columnValueList.size(); i++){
					Field columnValue = columnValueList.get(i);
					switch(columnValue.getType()){
						case STRING: targetPstmt.setString(i + 1, (String)columnValue.getValue());break;
						case INT: targetPstmt.setInt(i + 1, (Integer)columnValue.getValue());break;
						case DATE: targetPstmt.setDate(i + 1, (Date)columnValue.getValue());break;
						case DOUBLE: targetPstmt.setDouble(i + 1, (Double)columnValue.getValue());break;
						case CLOB: targetPstmt.setClob(i + 1, (Clob)columnValue.getValue());break;
						case BLOB: targetPstmt.setBlob(i + 1, (Blob)columnValue.getValue());break;
						case LONG: targetPstmt.setLong(i + 1, (Long)columnValue.getValue());break;
					}
				}
				targetPstmt.execute();
			}
			targetConn.commit();
			inforLogger.info("Success sql: " + this.buildLogSelectSql());
		} catch (Exception e) {
			this.errorSelLog(this.buildLogSelectSql());
			inforLogger.error("Fail sql: " + this.buildLogSelectSql() + "\r\n" + e);
		} finally {
			
			try {
				if (rs != null){rs.close(); }
				if (pstmt != null){pstmt.close();}
				if (sourceConn != null){sourceConn.close();}
				if (targetPstmt != null){targetPstmt.close(); }
				if (targetConn != null){targetConn.close();}
			} catch (Exception e) {
				inforLogger.error(e);
			}
			
			if(endCountDownLatch != null)endCountDownLatch.countDown();
		}
	}
	
	
	protected String buildLogInsertSql(List<String> colomnNameList, List<Field> columnValueList){
		StringBuilder sb = new StringBuilder();
		sb.append("INSERT INTO ")
		  .append(this.table.getName())
		  .append("(")
		  .append(StringUtils.join(colomnNameList, ","))
		  .append(")")
		  .append(" VALUES(");
		for(Field columnValue : columnValueList){
			switch(columnValue.getType()){
				case STRING: sb.append("'").append(columnValue.getValue()).append("'").append(",");break;
				case INT: sb.append(columnValue.getValue()).append(",");break;
				case DATE: sb.append("'").append(columnValue.getValue()).append("'").append(",");break;
				case DOUBLE: sb.append("'").append(columnValue.getValue()).append("'").append(",");break;
				case CLOB: sb.append("'").append(columnValue.getValue()).append("'").append(",");break;
				case BLOB: sb.append("'").append(columnValue.getValue()).append("'").append(",");break;
				case LONG: sb.append(columnValue.getValue()).append(",");break;
			}
		}
		sb.replace(sb.lastIndexOf(","), sb.length(), ")");
		return sb.toString();
	}
	
	public void errorSelLog(String msg){
		if(failselectLogger != null){
			failselectLogger.error(msg);
		}
 	}
	
	protected abstract String buildSelectSql();
	
	protected abstract String buildInsertSql(List<String> colomnNameList);
	
	protected abstract String buildLogSelectSql();
	
	protected void setThreadPrameter(PreparedStatement pstmt)throws Exception{}
	
	protected abstract int getThreadIndex();
	
}
