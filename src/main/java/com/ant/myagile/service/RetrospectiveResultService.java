package com.ant.myagile.service;

import java.util.List;

import com.ant.myagile.model.Project;
import com.ant.myagile.model.RetrospectiveResult;
import com.ant.myagile.model.Sprint;
import com.ant.myagile.model.Team;

public interface RetrospectiveResultService {
	 List<RetrospectiveResult> findAllRetrospective();
	 boolean addRetrospective(RetrospectiveResult retro);
	 boolean updateRetrospective(RetrospectiveResult retro);
	 boolean updateStatus(RetrospectiveResult retro);
	 RetrospectiveResult getRetrospectivetroById(long id);
	 List<RetrospectiveResult> findRetrospectiveByTeamAndProject(Team team, Project project);
	 boolean deleteRetrospective(Long id);
	 List<RetrospectiveResult> getRetrospectiveResultsBySprint(
				Sprint sprint);
	 void deleteAllRetrospectiveResultsInSprint(Sprint  sprint);
}
