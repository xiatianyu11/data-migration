package com.my.miggration;

public class Table {
	
	private String name;
	
	private String hashKey;
	
	private int selectCount;
	
	private int currentNum;
	
	public Table(String name, String hashKey, int count, int commitNum) {
		super();
		this.name = name;
		this.hashKey = hashKey;
		this.selectCount = count / commitNum;
		this.currentNum = this.selectCount;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getHashKey() {
		return hashKey;
	}

	public void setHashKey(String hashKey) {
		this.hashKey = hashKey;
	}
	
	
	
	public int getSelectCount() {
		return selectCount;
	}

	public void setSelectCount(int selectCount) {
		this.selectCount = selectCount;
	}

	public synchronized int getCurrentNum(){
		return this.selectCount--;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Table other = (Table) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
	
	

}
