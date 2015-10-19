
package com.ant.myagile.service;

import java.util.List;

import com.ant.myagile.model.Member;
import com.ant.myagile.model.Project;
import com.ant.myagile.model.Team;

public interface TeamService 
{
	boolean save(Team team);
	boolean update(Team team);
	boolean delete(Team team);
	void deleteLogo(Team team, String filename); 
	void moveLogo(Team team, String filename);
	void deleteTempLogo(String filename);
	
	/**
	 * Check team name is existed or not
	 * @param newTeam
	 * @return <strong>true</strong> if team name is existed </br>
	 * 			<strong>false</strong> if team name is not existed
	 */
	boolean checkTeamNameExisted(Team newTeam);
    
    	/**
	 * Check mail group is existed or not
	 * @param newTeam
	 * @return <strong>true</strong> if mail group is existed </br>
	 * 			<strong>false</strong> if mail group is not existed
	 */
    	boolean checkMailGroupExisted(Team newTeam);
	Team findTeamByTeamId(long teamId);
	List<Team> findTeamsByProjectIdAndMemberId(long pid, long uid);
	List<Team> findTeamsbyProjectIdAndMemberId(long pid, long uid);
	List<Team> findTeamsByMember(Member memberOwner);
	List<Team> findTeamsByOwner(Member owner);
	List<Team> findTeamsByMemberAndOwner(Member userLogin);
	List<Team> findTeamsByProjectId(long projectId);
	List<Team> findAllTeamsByProjectId(long projectId);
	List<Team> findTeamsByProject(Project p);
	
	/**
	  * Change original file name to format "<strong>filename<strong>_time" 
	 * @param teamName
	 * @return file name was changed
	 */
	String teamImageNameProcess(String teamName);
	
	
	/**
	 * Create directory for team if not exist
	 * @param team
	 * @return <strong>String</strong> path of directory
	 */
	String makeTeamDir(Team team);
	
	
	/**
	  * Rename logo file and move file from temp folder to team's folder
	 * @param oldFileName
	 * @param newFileName
	 * @return new file name
	 */
	String renameLogo(String oldFileName, String newFileName);
	int countTeamByProject(Long projectId);
	List<Team> findAllTeam();
	Team findTeamByGroupMail(String mailGroup);
}

