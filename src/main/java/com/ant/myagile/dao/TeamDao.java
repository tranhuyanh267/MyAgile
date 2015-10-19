package com.ant.myagile.dao;

import java.util.List;

import com.ant.myagile.model.Member;
import com.ant.myagile.model.Project;
import com.ant.myagile.model.Sprint;
import com.ant.myagile.model.Team;

public interface TeamDao {
    boolean save(Team team);

    boolean update(Team team);

    boolean delete(Team team);

    Team findTeamByTeamId(long teamId);

    List<Team> findTeamsbyProjectId(long projectId);
    
    List<Team> findAllTeamsbyProjectId(long projectId);

    List<Team> findTeamsbyProjectIdAndMemberId(long pid, long uid);

    List<Team> findTeamsByProjectIdAndMemberId(long pid, long uid);

    List<Team> findTeamsByMember(Member member);

    List<Team> findTeamsByOwner(Member owner);

    List<Team> findTeamsByProject(Project p);

    /**
     * Check team name is existed or not
     * 
     * @param newTeam
     * @return <strong>true</strong> if team name is existed </br>
     *         <strong>false</strong> if team name is not existed
     */
    boolean checkTeamNameExisted(Team newTeam);

    /**
     * Check mail group is existed or not
     * 
     * @param newTeam
     * @return <strong>true</strong> if mail group is existed </br>
     *         <strong>false</strong> if mail group is not existed
     */
    boolean checkMailGroupExisted(Team newTeam);

    int countTeamByProject(Long projectId);

    List<Team> findAllTeam();

    Team findTeamByGroupMail(String mailGroup);
    Sprint getSprintOfTeamInCurrentTime(long teamId);
}
