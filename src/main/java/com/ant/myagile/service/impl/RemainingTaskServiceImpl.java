package com.ant.myagile.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ant.myagile.dao.SprintDao;
import com.ant.myagile.dao.TeamDao;
import com.ant.myagile.model.Sprint;
import com.ant.myagile.service.RemainingTaskService;

@Service
@Transactional
public class RemainingTaskServiceImpl implements RemainingTaskService{

	@Autowired
	TeamDao teamDao;
	
	@Autowired
	SprintDao sprintDao;
	
	@Override
	public Sprint getSprintOfTeamInCurrentTime(long teamId) {
		return teamDao.getSprintOfTeamInCurrentTime(teamId);
	}

	@Override
	public boolean checkUserstoryInSprint(long userstoryId, Sprint sprint) {
		return sprintDao.checkUserstoryInSprint(userstoryId, sprint);
	}

}
