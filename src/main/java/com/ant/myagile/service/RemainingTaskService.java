package com.ant.myagile.service;

import com.ant.myagile.model.Sprint;

public interface RemainingTaskService {
	Sprint getSprintOfTeamInCurrentTime(long teamId);
	boolean checkUserstoryInSprint(long userstoryId,Sprint sprint);
}
