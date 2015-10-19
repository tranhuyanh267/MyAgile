package com.ant.myagile.dto;

import java.util.List;

public class IssueOrder {

	private String colName;
	private List<String> issueId;

	public String getColName() {
		return colName;
	}

	public void setColName(String colName) {
		this.colName = colName;
	}

	public List<String> getIssueId() {
		return issueId;
	}

	public void setIssueId(List<String> issueId) {
		this.issueId = issueId;
	}

}
