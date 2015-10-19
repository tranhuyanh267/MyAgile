package com.ant.myagile.dto;

public class StateLazyLoading {
	private LazyFilter filters;
	private LazySorter sorters;
	private LazyFilterInList filterInList;
	private int start;
	private int limit;
	private int step;

	public StateLazyLoading() {
	}

	public LazyFilter getFilters() {
		return filters;
	}

	public void setFilters(LazyFilter filters) {
		this.filters = filters;
	}

	public LazySorter getSorters() {
		return sorters;
	}

	public void setSorters(LazySorter sorters) {
		this.sorters = sorters;
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

	public LazyFilterInList getFilterInList() {
		return filterInList;
	}

	public void setFilterInList(LazyFilterInList filterInList) {
		this.filterInList = filterInList;
	}
}
