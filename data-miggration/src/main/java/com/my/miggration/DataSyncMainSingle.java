package com.my.miggration;

import java.util.List;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class DataSyncMainSingle extends DataSyncMainBase{
	private static Logger infoLog = Logger.getLogger(DataSyncMainMultiple.class);
	
	static final String configFileName = "config.properties";
	static String acceptTableName = "users";
	
	public DataSyncMainSingle(String basePath){
		this.basePath = basePath;
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		try {
			acceptTableName = args[1];
			PropertyConfigurator.configure(System.getProperty("catalina.home") + "/conf/log4j.properties");
			String confFilePath = args[0];
			DataSyncMainBase dataSyncMain = new DataSyncMainSingle(args[0]);
			if (dataSyncMain.setparameters(confFilePath)) {
				// start to call thread to sync the data
				dataSyncMain.syncData();
			}
		} catch (Exception e) {
			infoLog.error(e);
			System.exit(1);
		}
		
	}
	
	protected List<Table> syncTable(BasicDataSource sourceDataSource, BasicDataSource targetDataSource){
		TableSyncBase tableSync = new TableSyncSingle(sourceDataSource, targetDataSource, sessionParameter, acceptTableName);
		List<Table> tableList = tableSync.getTableList();
		tableSync.truncateData();
		return tableList;
	}
	
}
