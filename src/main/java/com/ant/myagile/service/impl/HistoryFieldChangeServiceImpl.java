package com.ant.myagile.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ant.myagile.dao.HistoryFieldChangeDao;
import com.ant.myagile.model.HistoryFieldChange;
import com.ant.myagile.service.HistoryFieldChangeService;

@Service
public class HistoryFieldChangeServiceImpl implements HistoryFieldChangeService {

	@Autowired
	private HistoryFieldChangeDao historyFieldChangeDao;
	
	@Override
	public List<HistoryFieldChange> findUpdateHistoryOfPointRemainByIssueId(long issueId) {
		return this.historyFieldChangeDao.findUpdateHistoryOfPointRemainByIssueId(issueId);
	}

	@Override
	public List<HistoryFieldChange> findUpdateHistoryOfProject(long projectId,String fieldName) {
		return this.historyFieldChangeDao.findUpdateHistoryOfProject(projectId,fieldName);
	}

	@Override
	public List<HistoryFieldChange> findUpdateHistoryOfTeam(long teamId,String fieldName) {
		return this.historyFieldChangeDao.findUpdateHistoryOfTeam(teamId,fieldName);
	}

}
