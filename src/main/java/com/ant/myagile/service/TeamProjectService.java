package com.ant.myagile.service;

import java.util.List;

import com.ant.myagile.model.Project;
import com.ant.myagile.model.Team;
import com.ant.myagile.model.TeamProject;

public interface TeamProjectService {
	 boolean create(TeamProject tp);
	 boolean update(TeamProject pro);
	 List<TeamProject> findTeamProjectsByProjectId(long id);	 
	 List<TeamProject> findAllTeamProject();
	 TeamProject findTeamProjectById(long id);
	 TeamProject findTeamProjectByProjectAndTeam(Project p,Team t);
	 List<TeamProject> getTeamProjectsByTeam(Team team);
	 void deleteAllTeamProjectByTeam(Team team);
}
