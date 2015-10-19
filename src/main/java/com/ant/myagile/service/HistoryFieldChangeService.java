package com.ant.myagile.service;

import java.util.List;

import com.ant.myagile.model.HistoryFieldChange;

public interface HistoryFieldChangeService {
	 List<HistoryFieldChange> findUpdateHistoryOfPointRemainByIssueId(long issueId);
	 List<HistoryFieldChange> findUpdateHistoryOfProject(long projectId,String fieldName);
	 List<HistoryFieldChange> findUpdateHistoryOfTeam(long teamId,String fieldName);
}
