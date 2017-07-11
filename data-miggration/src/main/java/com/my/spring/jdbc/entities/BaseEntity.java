package com.my.spring.jdbc.entities;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.my.common.annotations.Table;

public class BaseEntity {
	
	public  SqlParams insertSqlParams() throws IllegalArgumentException, IllegalAccessException{
		Table table = this.getClass().getAnnotation(Table.class);
		StringBuilder csb = new StringBuilder();
		StringBuilder vsb = new StringBuilder();
		
		Field[] fields = this.getClass().getDeclaredFields();
		List<Object> params = new ArrayList<Object>();
		for(Field f : fields){
			if(f.getName().equalsIgnoreCase(table.key()))continue;
			csb.append(f.getName()).append(",");
			vsb.append("?").append(",");
			f.setAccessible(true);
			params.add(f.get(this));
		}
		
		
		StringBuilder sqlsb = new StringBuilder("INSERT INTO ");
		
		if(StringUtils.isEmpty(table.name())){
			sqlsb.append(this.getClass().getSimpleName());
		}else{
			sqlsb.append(table.name());
		}
		sqlsb.append("(")
			.append(csb.deleteCharAt(csb.lastIndexOf(",")))
			.append(") VALUES(")
			.append(vsb.deleteCharAt(vsb.lastIndexOf(",")))
			.append(")");
		
		return new SqlParams(sqlsb.toString(), params.toArray());
	}
	
	
	public SqlParams updateSqlParams() throws IllegalArgumentException, IllegalAccessException{
		Table table = this.getClass().getAnnotation(Table.class);
		
		
		StringBuilder sqlsb = new StringBuilder("UPDATE ");
		if(StringUtils.isEmpty(table.name())){
			sqlsb.append(this.getClass().getSimpleName());
		}else{
			sqlsb.append(table.name());
		}
		sqlsb.append(" SET ");
		
		Field[] fields = this.getClass().getDeclaredFields();
		List<Object> params = new ArrayList<Object>();
		Object primaryValue = null;
		for(Field f : fields){
			if(f.getName().equalsIgnoreCase(table.key())){
				f.setAccessible(true);
				primaryValue = f.get(this);
				continue;
			}
			f.setAccessible(true);
			sqlsb.append(f.getName())
				.append("=")
				.append("?")
				.append(",");
			params.add(f.get(this));
		}
		params.add(primaryValue);
		StringBuilder sb = new StringBuilder();
		sb.append(sqlsb.deleteCharAt(sqlsb.lastIndexOf(",")))
			.append(" WHERE ")
			.append(table.key())
			.append("=?");
		
		return new SqlParams(sb.toString(), params.toArray());
	}
	
	

}
