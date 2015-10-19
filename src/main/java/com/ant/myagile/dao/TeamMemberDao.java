package com.ant.myagile.dao;

import java.util.List;

import com.ant.myagile.model.Member;
import com.ant.myagile.model.Team;
import com.ant.myagile.model.TeamMember;

public interface TeamMemberDao {
    boolean save(TeamMember teamMember);

    boolean delete(TeamMember teamMember);

    boolean update(TeamMember teamMember);

    void changePositionMember(TeamMember teamMember, String position);

    boolean checkMemberInTeam(Member mem, Team team);

    TeamMember findTeamMemberByMemberAndTeam(Member member, Team team);

    TeamMember findTeamMemberByToken(String token);

    TeamMember findTeamMemberById(Long id);
    
    List<TeamMember> getTeamMembersInTeam(Team team);
    
    
}
