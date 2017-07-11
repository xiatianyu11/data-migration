package com.my.miggration;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.log4j.Logger;


public abstract class DataSyncMainBase {

	private final Logger inforLogger = Logger.getLogger(DataSyncMainMultiple.class.getName());
	protected DataSyncDataSourceParameter dataSourceParameters;
	protected DataSyncSessionParameter sessionParameter;
	protected final String configFileName = "config.properties";
	protected String basePath;
	

	protected void syncData() {
		BasicDataSource sourceDataSource = null;
		BasicDataSource targetDataSource = null;
		try {
			// for source driver
			sourceDataSource = createSourceDataSource();
			// for target database
			targetDataSource = createTargetDataSource();
			
			List<Table> tableList = this.syncTable(sourceDataSource, targetDataSource);
			// call thread to sync the data from source to target
			ExecutorService scheduler = Executors.newFixedThreadPool(sessionParameter.getThreadNum());
			for(Table table : tableList){
				int selectCount = table.getSelectCount();
				long start = System.currentTimeMillis();
				if(selectCount > 0){
					final CountDownLatch beginCountDownLatch = new CountDownLatch(1);  
					final CountDownLatch endCountDownLatch = new CountDownLatch(selectCount + 1);  
					for (int i = 0; i <= selectCount; i++) {
						scheduler.execute(new DataSyncMiltiple(sourceDataSource, targetDataSource, sessionParameter, table, beginCountDownLatch, endCountDownLatch));
					}
					
					beginCountDownLatch.countDown();
					endCountDownLatch.await();
					
				}else{
					DataSyncSingle dataSync = new DataSyncSingle(sourceDataSource, targetDataSource, sessionParameter, table);
					dataSync.start();
				}
				
				long end = System.currentTimeMillis();
				long duration = end - start;
				inforLogger.info("Table " + table.getName() + " ,duration=" + duration + "ms");
			}
			scheduler.shutdown();
			scheduler.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
		} catch (Exception ex) {
			inforLogger.error(ex);
		} finally{
			try {
				if (null != sourceDataSource){sourceDataSource.close();}
				if (null != targetDataSource){targetDataSource.close();}
			} catch (Exception e) {
				inforLogger.error(e);
			}
		}
	}
	
	private BasicDataSource createSourceDataSource(){
		BasicDataSource sourceDataSource = new BasicDataSource();

		sourceDataSource.setInitialSize(dataSourceParameters.getSourceInitialSize());
		sourceDataSource.setMaxActive(dataSourceParameters.getSourceMaxActive());
		sourceDataSource.setDriverClassName(dataSourceParameters.getSourceDriverClassName());
		sourceDataSource.setUrl(dataSourceParameters.getSourceUrl());
		sourceDataSource.setUsername(dataSourceParameters.getSourceUser());
		sourceDataSource.setPassword(dataSourceParameters.getSourcePassword());
		sourceDataSource.setMaxWait(dataSourceParameters.getSourceMaxWait());
		sourceDataSource.setMaxIdle(dataSourceParameters.getSourceMaxIdle());
		sourceDataSource.setMinIdle(dataSourceParameters.getSourceMinIdle());
		
		return sourceDataSource;
	}
	
	private BasicDataSource createTargetDataSource(){
		BasicDataSource targetDataSource = new BasicDataSource();
		targetDataSource.setInitialSize(dataSourceParameters.getTargetInitialSize());
		targetDataSource.setMaxActive(dataSourceParameters.getTargetMaxActive());
		targetDataSource.setDriverClassName(dataSourceParameters.getTargetDriverClassName());
		targetDataSource.setUrl(dataSourceParameters.getTargetUrl());
		targetDataSource.setUsername(dataSourceParameters.getTargetUser());
		targetDataSource.setPassword(dataSourceParameters.getTargetPassword());
		targetDataSource.setMaxWait(dataSourceParameters.getTargetMaxWait());
		targetDataSource.setMaxIdle(dataSourceParameters.getTargetMaxIdle());
		targetDataSource.setMinIdle(dataSourceParameters.getTargetMinIdle());
		return targetDataSource;
	}

	protected boolean setparameters(String filePath) {
		dataSourceParameters = new DataSyncDataSourceParameter();
		sessionParameter = new DataSyncSessionParameter();
		Properties props = new Properties();
		try {
			InputStream in = new BufferedInputStream(new FileInputStream(filePath + "//" + configFileName));
			props.load(in);

			// for source database
			dataSourceParameters.setSourceInitialSize(Integer.parseInt(props.getProperty("source.dataSource.initialSize")));
			dataSourceParameters.setSourceMaxIdle(Integer.parseInt(props.getProperty("source.dataSource.maxIdle")));
			dataSourceParameters.setSourceMinIdle(Integer.parseInt(props.getProperty("source.dataSource.minIdle")));
			dataSourceParameters.setSourceMaxActive(Integer.parseInt(props.getProperty("source.dataSource.maxActive")));
			dataSourceParameters.setSourceMaxWait(Integer.parseInt(props.getProperty("source.dataSource.maxWait")));
			dataSourceParameters.setSourceDriverClassName(props.getProperty("source.jdbc.driverClassName"));
			dataSourceParameters.setSourceUrl(props.getProperty("source.jdbc.url"));
			dataSourceParameters.setSourceUser(props.getProperty("source.jdbc.username"));
			
			dataSourceParameters.setSourcePassword(props.getProperty("source.jdbc.password"));

			// for target database
			dataSourceParameters.setTargetInitialSize(Integer.parseInt(props.getProperty("target.dataSource.initialSize")));
			dataSourceParameters.setTargetMaxIdle(Integer.parseInt(props.getProperty("target.dataSource.maxIdle")));
			dataSourceParameters.setTargetMinIdle(Integer.parseInt(props.getProperty("target.dataSource.minIdle")));
			dataSourceParameters.setTargetMaxActive(Integer.parseInt(props.getProperty("target.dataSource.maxActive")));
			dataSourceParameters.setTargetMaxWait(Integer.parseInt(props.getProperty("target.dataSource.maxWait")));
			dataSourceParameters.setTargetDriverClassName(props.getProperty("target.jdbc.driverClassName"));
			dataSourceParameters.setTargetUrl(props.getProperty("target.jdbc.url"));
			dataSourceParameters.setTargetUser(props.getProperty("target.jdbc.username"));
			
			dataSourceParameters.setTargetPassword(props.getProperty("target.jdbc.password"));
			
			sessionParameter.setSourceCommand(props.getProperty("source.database.sessionCommand"));
			sessionParameter.setTargetSchema(props.getProperty("tartet.database.schema"));
			sessionParameter.setTargetCommitNum(Integer.parseInt(props.getProperty("target.database.commitNum")));
			sessionParameter.setErrorSelOut("error.select.out");
			sessionParameter.setThreadNum(Integer.parseInt(props.getProperty("target.database.threadNum")));
			return true;
		} catch (Exception ex) {
			inforLogger.error("Load configure parameter error: " + ex.getMessage());
			return false;
		}
	}
	
	protected abstract List<Table> syncTable(BasicDataSource sourceDataSource, BasicDataSource targetDataSource);

}
