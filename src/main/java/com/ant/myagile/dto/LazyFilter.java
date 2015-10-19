package com.ant.myagile.dto;

import java.util.List;

public class LazyFilter {

	private List<String> field;
	private Object value;

	public LazyFilter() {
	}

	public List<String> getField() {
		return field;
	}

	public void setField(List<String> field) {
		this.field = field;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}
}
