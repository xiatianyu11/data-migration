package com.my.miggration;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

public class TableSyncBase {
	private final Logger inforLogger = Logger.getLogger(this.getClass().getName());
	private final BasicDataSource sourceDataSource;
	private final BasicDataSource targetDataSource;
	private final String schemaPattern;
	private final int commitNum;
	private final List<Table> tableList = new ArrayList<Table>();

	public TableSyncBase(BasicDataSource sourceDataSource, BasicDataSource targetDataSource, DataSyncSessionParameter sessionParameter) {
		super();
		this.sourceDataSource = sourceDataSource;
		this.targetDataSource = targetDataSource;
		this.schemaPattern = sessionParameter.getTargetSchema();
		this.commitNum = sessionParameter.getTargetCommitNum();
		this.init();
	}
	
	public void init(){
		Connection conn = null;
		ResultSet rs = null;
		Connection sourceConn = null;
		try {
			sourceConn = sourceDataSource.getConnection();
			conn = targetDataSource.getConnection();
			DatabaseMetaData targetMetaData = conn.getMetaData();
			rs = targetMetaData.getTables(null, this.schemaPattern, "%", new String[] { "TABLE" });
			while(rs.next()){
				String tableName = rs.getString(3);
				this.buildTableList(tableName, sourceConn,  targetMetaData);
			}
		} catch (Exception e) {
			inforLogger.error(e);
		}finally{
			try {
				if(rs != null){ rs.close(); }
				if(conn != null){ conn.close(); }
				if(sourceConn != null){ sourceConn.close(); }
			} catch (SQLException e) {
				inforLogger.error(e);
			}
		}
	}
	
	public List<Table> getTableList(){
		return this.tableList;
	}
	
	
	public void truncateData(){
		Connection conn = null;
		PreparedStatement stmt = null;
		try {
			conn = targetDataSource.getConnection();
			conn.setAutoCommit(false);
			ListIterator<Table> listIter = this.tableList.listIterator(this.tableList.size());
			while(listIter.hasPrevious()){
				Table previous = listIter.previous();
				String sql = "delete from " + previous.getName();
				inforLogger.info(sql);
				stmt = conn.prepareStatement(sql);
				stmt.execute();
			}
			conn.commit();
		} catch (Exception e) {
			inforLogger.error(e.getMessage());
		}finally{
			try {
				if(stmt != null){ stmt.close(); }
				if(conn != null){ conn.close(); }
			} catch (SQLException e) {
				inforLogger.error(e.getMessage());
			}
		}
		
	}
	
	
	protected void buildTableList(String tableName, Connection sourceConn, DatabaseMetaData targetMetaData){
		inforLogger.info(tableName);
		ResultSet rsPk = null;
		ResultSet rsFk = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String selSql = "select count(*) from " + tableName;
		try {
			
			rsPk = targetMetaData.getPrimaryKeys(null, schemaPattern, tableName);
			String pk = "";
			while(rsPk.next()){
				pk = rsPk.getString(4);
				break;
			}
			rsFk = targetMetaData.getImportedKeys(null, this.schemaPattern, tableName);
			while(rsFk.next()){
				this.buildTableList(rsFk.getString(3), sourceConn, targetMetaData);
			}
			pstmt = sourceConn.prepareStatement(selSql);
			rs = pstmt.executeQuery();
			int count = 0;
			if(rs.next()){
				count = rs.getInt(1);
			}
			Table table = new Table(tableName, pk, count, this.commitNum);
			if(!tableList.contains(table) && StringUtils.isNotBlank(pk)){
				tableList.add(table);
			}
		} catch(Exception e){
			inforLogger.error("Oracle database is not exist table : " + tableName, e);
		}finally {
			try {
				if(rsPk != null){ rsPk.close(); }
				if(rsFk != null){ rsPk.close(); }
			} catch (SQLException e) {
				inforLogger.error(tableName + ": " + e.getMessage());
			}
		}
	}
	
	
	
	
	

}
