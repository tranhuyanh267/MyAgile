package com.ant.myagile.dto;

public class LazySorter {
	public static enum LAZYSORTER_VALUE {
		ASC, DESC
	}

	private String field;
	private LAZYSORTER_VALUE value;

	public LazySorter() {
	}

	public String getField() {
		return field;
	}

	public void setField(String field) {
		this.field = field;
	}

	public LAZYSORTER_VALUE getValue() {
		return value;
	}

	public void setValue(LAZYSORTER_VALUE value) {
		this.value = value;
	}

}
