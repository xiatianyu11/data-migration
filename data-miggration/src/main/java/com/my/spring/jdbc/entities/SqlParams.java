package com.my.spring.jdbc.entities;


public class SqlParams{
	private String sql;
	private Object[] params;
	public SqlParams(String sql, Object[] params) {
		super();
		this.sql = sql;
		this.params = params;
	}
	public String getSql() {
		return sql;
	}
	public void setSql(String sql) {
		this.sql = sql;
	}
	public Object[] getParams() {
		return params;
	}
	public void setParams(Object[] params) {
		this.params = params;
	}
	
	
	
}