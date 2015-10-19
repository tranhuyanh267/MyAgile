package com.ant.myagile.service.impl;

import com.ant.myagile.dao.TeamProjectDao;
import com.ant.myagile.model.Project;
import com.ant.myagile.model.Team;
import com.ant.myagile.model.TeamProject;
import com.ant.myagile.service.TeamProjectService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
public class TeamProjectServiceImpl implements TeamProjectService {

	@Autowired
	private TeamProjectDao teamProjectDAO;

	@Override
	@Transactional
	public boolean create(TeamProject teamProject) {
		return this.teamProjectDAO.create(teamProject);
	}	

	@Override
	@Transactional
	public boolean update(TeamProject teamProject) {
		return this.teamProjectDAO.update(teamProject);
	}

	@Override
	@Transactional
	public TeamProject findTeamProjectById(long id) {
		return this.teamProjectDAO.findTeamProjectById(id);
	}

	@Override
	@Transactional
	public TeamProject findTeamProjectByProjectAndTeam(Project project, Team team) {
		return this.teamProjectDAO.findTeamProjectByProjectAndTeam(project, team);
	}
	
	@Override
	public List<TeamProject> findAllTeamProject() {
		return this.teamProjectDAO.findAllTeamProject();
	}

	@Override
	public List<TeamProject> findTeamProjectsByProjectId(long projectId) {
		return this.teamProjectDAO.findTeamsProjectsByProjectId(projectId);
	}

	@Override
	public List<TeamProject> getTeamProjectsByTeam(Team team) {
		return teamProjectDAO.getTeamProjectsByTeam(team);
	}

	@Override
	public void deleteAllTeamProjectByTeam(Team team) {
		List<TeamProject> teamProjects = new ArrayList<TeamProject>();
		teamProjects = teamProjectDAO.getTeamProjectsByTeam(team);
		if(teamProjects.size() > 0){
			for (TeamProject teamProject : teamProjects) {
				teamProjectDAO.delete(teamProject);
			}
		}
		
		
	}

}
