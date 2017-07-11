package com.my.miggration;

public class DataSyncSessionParameter {
   
	private int targetCommitNum;
	private int threadNum;
	private String sourceCommand;
	private String targetSchema;
	private String errorSelOut;
	
	
	public int getTargetCommitNum() {
		return targetCommitNum;
	}
	public void setTargetCommitNum(int targetCommitNum) {
		this.targetCommitNum = targetCommitNum;
	}
	public String getSourceCommand() {
		return sourceCommand;
	}
	public void setSourceCommand(String sourceCommand) {
		this.sourceCommand = sourceCommand;
	}
	public String getTargetSchema() {
		return targetSchema;
	}
	public void setTargetSchema(String targetSchema) {
		this.targetSchema = targetSchema;
	}
	public String getErrorSelOut() {
		return errorSelOut;
	}
	public void setErrorSelOut(String errorSelOut) {
		this.errorSelOut = errorSelOut;
	}
	public int getThreadNum() {
		return threadNum;
	}
	public void setThreadNum(int threadNum) {
		this.threadNum = threadNum;
	}
	
	
}
