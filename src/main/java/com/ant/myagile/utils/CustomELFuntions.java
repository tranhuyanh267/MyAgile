package com.ant.myagile.utils;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.BeansException;
import org.springframework.stereotype.Component;

import com.ant.myagile.model.MemberRole;
import com.ant.myagile.model.Project;
import com.ant.myagile.model.Role;
import com.ant.myagile.model.Sprint;
import com.ant.myagile.model.Team;
import com.ant.myagile.model.TeamMember;
import com.ant.myagile.service.MemberRoleService;
import com.ant.myagile.service.MemberService;
import com.ant.myagile.service.ProjectService;
import com.ant.myagile.service.RoleService;
import com.ant.myagile.service.SprintService;
import com.ant.myagile.service.TeamMemberService;
import com.ant.myagile.service.TeamService;

@Component
public class CustomELFuntions {

	public static final String DEVELOPER = "DEVELOPER";
	public static final String SCRUM_MASTER = "SCRUM_MASTER";
	public static final String PRODUCT_OWNER = "PRODUCT_OWNER";
	
	
	/**
	 * Check role when user login 
	 * ROLE_USER, ROLE_ADMIN
	 * 
	 * @param roles
	 * @param teamId
	 * @return the result of the authorization decision
	 */
	private static boolean isRole(String roles) {
		try {
			
			MemberRoleService memberRoleService = SpringContext.getApplicationContext().getBean(MemberRoleService.class);
			RoleService roleService = SpringContext.getApplicationContext().getBean(RoleService.class);
			Utils utils = SpringContext.getApplicationContext().getBean(Utils.class);
			List<String> desiredRoles = Arrays.asList(roles.split(","));
			
			List<MemberRole> memberRoles = memberRoleService.getRoleByIdMember(utils.getLoggedInMember().getMemberId());
			
			for(MemberRole memberRole : memberRoles){
				Role role = roleService.getRoleByObject("roleId", memberRole.getRoleId());
				if(desiredRoles.contains(role.getAuthority())){
					return true;
				}
			}
		} catch (BeansException e) {
			return false;
		}
		return false;
	}
	
	/**
	 * Make an authorization decision based on check permission of member in
	 * each team.
	 * 
	 * @param roles
	 * @param teamId
	 * @return the result of the authorization decision
	 */
	private static boolean isGranted(String roles, Long teamId) {
		try {
			TeamService teamService = SpringContext.getApplicationContext().getBean(TeamService.class);
			TeamMemberService teamMemberService = SpringContext.getApplicationContext().getBean(TeamMemberService.class);
			MemberService memberService = SpringContext.getApplicationContext().getBean(MemberService.class);
			Utils utils = SpringContext.getApplicationContext().getBean(Utils.class);
			List<String> desiredRoles = Arrays.asList(roles.split(","));

			List<Team> teams = teamService.findTeamsByOwner(memberService.findMemberById(utils.getLoggedInMember().getMemberId()));
			if ((teams != null) && (teams.size() != 0)) {
				if (teams.contains(teamService.findTeamByTeamId(teamId))) {
					return true;
				}
			}
			if (teamId > 0) {
				TeamMember teamMember = teamMemberService.findTeamMemberByMemberAndTeam(utils.getLoggedInMember(), teamService.findTeamByTeamId(teamId));
				if(teamMember != null){
					String position = teamMember.getPosition();
					if (desiredRoles.contains(position)) {
						return true;
					}
				}
			}

		} catch (BeansException e) {
			e.printStackTrace();
		}
		return false;
	}

	private static boolean isOwner(Long projectId) {
		if (projectId != 0) {
			Utils utils = SpringContext.getApplicationContext().getBean(Utils.class);
			ProjectService projectService = SpringContext.getApplicationContext().getBean(ProjectService.class);
			List<Project> projects = projectService.findByOwnerId(utils.getLoggedInMember().getMemberId());
			if ((projects != null) && (projects.size() != 0)) {
				if (projects.contains(projectService.findProjectById(projectId))) {
					return true;
				}
			}
		}
		return false;
	}

	public static boolean isStackHolder(Long projectId) {
		if (isOwnerOfProject(projectId)) {
			return false;
		}
		if(projectId != 0){
			TeamService teamService = SpringContext.getApplicationContext().getBean(TeamService.class);
			TeamMemberService teamMemberService = SpringContext.getApplicationContext().getBean(TeamMemberService.class);
			Utils utils = SpringContext.getApplicationContext().getBean(Utils.class);
			List<Team> teamList = teamService.findTeamsByProjectIdAndMemberId(projectId, utils.getLoggedInMember().getMemberId());
			if(null != teamList) {
				for (Team teamTemp : teamList) {
					TeamMember member = teamMemberService.findTeamMemberByMemberAndTeam(utils.getLoggedInMember(), teamService.findTeamByTeamId(teamTemp.getTeamId()));
					if(member == null){
						return false;
					}else{
						String position = member.getPosition();
						if (position == null | position.equals(DEVELOPER) || position.equals(SCRUM_MASTER) || position.equals(PRODUCT_OWNER)) {
							return false;
						}
					}
				}
			}
			
		}
		return true;
	}

	/**
	 * Return false if sprint is closed
	 * 
	 * @param sprintId
	 * @param statusSprint
	 * @return
	 */
	public static boolean isSprintClosed(Long sprintId, String statusSprint) {
		if (sprintId != 0) {
			SprintService sprintService = SpringContext.getApplicationContext().getBean(SprintService.class);
			Sprint sprint = sprintService.findSprintById(sprintId);
			if (sprint.getStatus().equals(statusSprint)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Return true if the user has any of the given authorities.
	 * 
	 * @param roles
	 *            a comma-separated list of user authorities.
	 * @param teamId
	 * @return the result of the authorization decision
	 */
	public static boolean isUserInAnyRoles(String roles, Long teamId) {
	   if(teamId != null && teamId > 0){
		return isGranted(roles, teamId);
	   }
	   return false;
	}

	/**
	 * Returns true if the user does not have any of the given authorities.
	 * 
	 * @param roles
	 *            a comma-separated list of user authorities.
	 * @param teamId
	 * @return
	 */
	public static boolean isUserNotInRoles(String roles, Long teamId) {
		return !isGranted(roles, teamId);
	}

	/**
	 * Returns true if the user is owner of project
	 * 
	 * @param projectId
	 * @return
	 */
	public static boolean isOwnerOfProject(Long projectId) {
		return isOwner(projectId);
	}
	
	public static boolean isAdmin(){
		return isRole("ROLE_ADMIN");
	}
	
	public static boolean hasTeam(){
		TeamService teamService = SpringContext.getApplicationContext().getBean(TeamService.class);
		Utils utils = SpringContext.getApplicationContext().getBean(Utils.class);
		boolean existTeam = true;
		if(teamService.findTeamsByMemberAndOwner(utils.getLoggedInMember()).isEmpty()){
			existTeam = false;
		}
		return existTeam;
	}
	
	
}
