package com.ant.myagile.managedbean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.ant.myagile.converter.ClientTimeOffsetConverter;
import com.ant.myagile.model.History;
import com.ant.myagile.model.Issue;
import com.ant.myagile.service.HistoryService;
import com.ant.myagile.service.IssueService;

@Component("historyBean")
@Scope("session")
public class HistoryBean implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@Autowired
	IssueService issueService;
	
	@Autowired
    HistoryService historyService;
	
	private Issue issue;
	
	private Long issueId;
	
	private List<History> historyOfIssue;
	
	public Issue getIssue() {
		issue = issueService.findIssueById(issueId);
		return issue;
	}
	
	public void setIssue(Issue issue) {
		this.issue = issue;
	}

	public Long getIssueId() {
		return issueId;
	}

	public void setIssueId(Long issueId) {
		this.issueId = issueId;
	}
	/**
	 * Find all history of issue
	 * @return List of History
	 */
    public List<History> getHistoryOfIssue() {
    	this.historyOfIssue = new ArrayList<History>();
    	try{
    		this.historyOfIssue = historyService.findHistoryByContainer("Issue", this.issueId);
    		ClientTimeOffsetConverter.transformServerTimeToClientTime(this.historyOfIssue);
    		return this.historyOfIssue;
    	}catch(Exception e){
    		return this.historyOfIssue;
    	}
    }

    public void setHistoryOfIssue(List<History> historyOfIssue) {
        this.historyOfIssue = historyOfIssue;
    }

}
