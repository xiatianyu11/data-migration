package com.my.spring.jdbc.dao;

import java.lang.reflect.ParameterizedType;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

import com.my.common.annotations.Table;
import com.my.spring.jdbc.entities.BaseEntity;
import com.my.spring.jdbc.entities.SqlParams;

public abstract class JdbcDaoImpl<T extends BaseEntity> extends JdbcDaoSupport {
	
	public int getCount(){
		String SQL = this.buildCountSql();
		int rowCount = getJdbcTemplate().queryForInt( SQL );
		return rowCount;
	}
	
	public T getById(Object id){
		String SQL = this.buildByIdSql();
		T obj = getJdbcTemplate().queryForObject(
		   SQL, new Object[]{id}, this.getMapper());
		return obj;
	}
	
	public List<T> getByAll(){
		String SQL = this.buildAllSql();
		List<T> list = getJdbcTemplate().query(
		   SQL,  getMapper());
		return list;
	}
	
	public void insert(T obj){
		try {
			SqlParams sqlParams = obj.insertSqlParams();
			getJdbcTemplate().update(sqlParams.getSql(), sqlParams.getParams());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void update(T obj){
		try {
			SqlParams sqlParams = obj.updateSqlParams();
			getJdbcTemplate().update(sqlParams.getSql(), sqlParams.getParams());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void deleteById(Object id){
		String SQL = this.buildDeleteSql();
		getJdbcTemplate().update(SQL, new Object[]{id});
	}
	
	
	
	protected abstract RowMapper<T> getMapper();
	
	@SuppressWarnings({  "unchecked" })
	private Class<T> getGenericClass(){
		Class<T> entityClass =  (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
		return entityClass;
	}
	
	private String getTableName(){
		Class<T> entityClass =  this.getGenericClass();
		Table table = entityClass.getAnnotation(Table.class);
		if(StringUtils.isEmpty(table.name())){
			return entityClass.getSimpleName();
		}
		return table.name();
	}
	
	private String getPrimaryKey(){
		Class<T> entityClass =  this.getGenericClass();
		Table table = entityClass.getAnnotation(Table.class);
		return table.key();
	}
	
	private String buildCountSql(){
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT COUNT(*) FROM ")
		  .append(this.getTableName());
		return sb.toString();
	}
	
	private String buildByIdSql(){
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT * FROM ")
		  .append(this.getTableName())
		  .append(" WHERE ")
		  .append(this.getPrimaryKey())
		  .append(" = ?");
		return sb.toString();
	}
	
	private String buildAllSql(){
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT * FROM ")
		  .append(this.getTableName())
		  .append(" WHERE ")
		  .append(this.getPrimaryKey())
		  .append(" = ?");
		return sb.toString();
	}
	
	
	private String buildDeleteSql(){
		StringBuilder sb = new StringBuilder();
		sb.append("DELETE FROM ")
		  .append(this.getTableName())
		  .append(" WHERE ")
		  .append(this.getPrimaryKey())
		  .append(" = ?");
		return sb.toString();
	}
	

}
