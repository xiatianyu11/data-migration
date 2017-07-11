package com.my.miggration;

public class Field<T> {
	
	private FieldType type;
	
	private T value;

	public Field(FieldType type, T value) {
		super();
		this.type = type;
		this.value = value;
	}

	public FieldType getType() {
		return type;
	}

	public void setType(FieldType type) {
		this.type = type;
	}

	public T getValue() {
		return value;
	}

	public void setValue(T value) {
		this.value = value;
	}
	
	

}
