package com.github.naf.jta;

import java.util.HashMap;
import java.util.Map;

public class XADataSourceFactory {
	public String className;
	public Boolean allowLocalTransactions;
	public String id;
	public Map<String, String> properties = new HashMap<>();
	public Integer minPool;
	public Integer maxPool;

	public XADataSourceFactory setClassName(String name) {
		this.className = name;
		return this;
	}

	public XADataSourceFactory setAllowLocalTransactions(boolean b) {
		this.allowLocalTransactions = b;
		return this;
	}

	public XADataSourceFactory setUniqueName(String string) {
		this.id = string;
		return this;
	}

	public XADataSourceFactory setPoolSize(int i, int j) {
		this.minPool = i;
		this.maxPool = j;
		return this;
	}

	public XADataSourceFactory addProperty(String string, String string2) {
		properties.put(string, string2);
		return this;
	}

}