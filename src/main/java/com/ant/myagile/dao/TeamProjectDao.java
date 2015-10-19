package com.ant.myagile.dao;

import java.util.List;

import com.ant.myagile.model.Project;
import com.ant.myagile.model.Team;
import com.ant.myagile.model.TeamProject;

public interface TeamProjectDao {
    boolean create(TeamProject tp);

    boolean update(TeamProject pro);
    
    boolean delete(TeamProject pro);

    TeamProject findTeamProjectById(long id);

    TeamProject findTeamProjectByProjectAndTeam(Project p, Team t);

    List<TeamProject> findTeamsProjectsByProjectId(long id);

    List<TeamProject> findAllTeamProject();
    
    List<TeamProject> getTeamProjectsByTeam(Team team);
}
