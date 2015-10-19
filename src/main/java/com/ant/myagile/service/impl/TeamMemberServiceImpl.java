package com.ant.myagile.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ant.myagile.dao.TeamMemberDao;
import com.ant.myagile.model.Member;
import com.ant.myagile.model.Team;
import com.ant.myagile.model.TeamMember;
import com.ant.myagile.service.HistoryService;
import com.ant.myagile.service.TeamMemberService;

@Service
@Transactional
public class TeamMemberServiceImpl implements TeamMemberService {
	@Autowired
	private TeamMemberDao teamMemberDAO;
	@Autowired
	private HistoryService historyService;
	@Override
	public boolean checkMemberInTeam(Member mem, Team team) {
		return teamMemberDAO.checkMemberInTeam(mem, team);
	}

	@Override
	public boolean save(TeamMember teamMember) {
		if(teamMemberDAO.save(teamMember)){
			this.historyService.saveHistoryForTeamMember(teamMember,"Add");
			return true;
		}
		return false;
	}

	@Override
	public TeamMember findTeamMemberByToken(String token) {
		return teamMemberDAO.findTeamMemberByToken(token);
	}

	@Override
	public boolean delete(TeamMember teamMember) {
		if(teamMemberDAO.delete(teamMember)){
			this.historyService.saveHistoryForTeamMember(teamMember,"Remove");
			return true;
		}
		return false;
	}

	@Override
	public boolean update(TeamMember teamMember) {
		return teamMemberDAO.update(teamMember);
	}

	@Override
	public TeamMember findTeamMemberById(Long id){
		return teamMemberDAO.findTeamMemberById(id);
	}

	@Override
	public void changePositionMember(TeamMember teamMember, String position) {
		this.historyService.saveHistoryForChangeRole(teamMember, position);
		teamMemberDAO.changePositionMember(teamMember, position);
	}

	@Override
	public TeamMember findTeamMemberByMemberAndTeam(Member member, Team team) {
		return teamMemberDAO.findTeamMemberByMemberAndTeam(member, team);
	}

	@Override
	public void deleteAllTeamMemberInTeam(Team team) {
		List<TeamMember> teamMembers = teamMemberDAO.getTeamMembersInTeam(team);
		if(teamMembers.size()>0){
			for (TeamMember teamMember : teamMembers) {
				teamMemberDAO.delete(teamMember);
			}
		}
		
	}
}
