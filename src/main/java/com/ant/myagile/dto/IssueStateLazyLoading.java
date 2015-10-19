package com.ant.myagile.dto;

import java.util.List;

public class IssueStateLazyLoading {
	private LazySorter sorters;
	private List<LazyFilterInList> filterInLists;
	private int start;
	private int step;
	private int limit;
	public LazySorter getSorters() {
		return sorters;
	}
	public void setSorters(LazySorter sorters) {
		this.sorters = sorters;
	}
	public List<LazyFilterInList> getFilterInLists() {
		return filterInLists;
	}
	public void setFilterInLists(List<LazyFilterInList> filterInLists) {
		this.filterInLists = filterInLists;
	}
	public int getStart() {
		return start;
	}
	public void setStart(int start) {
		this.start = start;
	}
	public int getLimit() {
		return limit;
	}
	public void setLimit(int limit) {
		this.limit = limit;
	}
	public int getStep() {
		return step;
	}
	public void setStep(int step) {
		this.step = step;
	}
}
