package com.ant.myagile.service;

import com.ant.myagile.model.Member;
import com.ant.myagile.model.Team;
import com.ant.myagile.model.TeamMember;

public interface TeamMemberService {	
	boolean save(TeamMember teamMember);	
	boolean delete(TeamMember teamMember);
	boolean update(TeamMember teamMember);
	void changePositionMember(TeamMember teamMember, String position);
	boolean checkMemberInTeam(Member mem,Team team);
	TeamMember findTeamMemberByToken(String token);
	TeamMember findTeamMemberById(Long id);
	TeamMember findTeamMemberByMemberAndTeam(Member member, Team team);
	void deleteAllTeamMemberInTeam(Team team);
	
}
