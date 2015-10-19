package com.ant.myagile.managedbean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.ant.myagile.model.Member;
import com.ant.myagile.model.MemberRole;
import com.ant.myagile.model.Project;
import com.ant.myagile.model.Role;
import com.ant.myagile.model.TeamMember;
import com.ant.myagile.model.TeamProject;
import com.ant.myagile.service.MailService;
import com.ant.myagile.service.MemberRoleService;
import com.ant.myagile.service.MemberService;
import com.ant.myagile.service.ProjectService;
import com.ant.myagile.service.RoleService;
import com.ant.myagile.service.TeamMemberService;
import com.ant.myagile.utils.JSFUtils;
import com.ant.myagile.utils.Utils;

@Component("adminBean")
@Scope("session")
public class AdminBean implements Serializable {

	private static final String ROLE_USER = "ROLE_USER";
	private static final String ROLE_ADMIN = "ROLE_ADMIN";
	private static final long serialVersionUID = 1L;
	private List<Member> members;
	private List<MemberRole> memberRole;
	private Role role;
	private Member memberLogin;
	private Long memId = -1l;

	private Long idUserForProject;
	private List<Project> allProjects;
	private List<Project> allProjectOfUser;

	@Autowired
	MemberService memberService;
	@Autowired
	MemberRoleService memberRoleService;
	@Autowired
	RoleService roleService;
	@Autowired
	ProjectService projectService;
	@Autowired
	TeamMemberService teamMemberService;
	@Autowired
	MailService mailService;
	@Autowired
	private Utils utils;
	@Autowired
	ProductBacklogBean producBacklogBean;

	@PostConstruct
	public void init() {
		members = memberService.findAll();
		memberLogin = memberService.getMemberByUsername(utils
				.getLoggedInMember().getUsername());
		allProjects = projectService.findAllProjects();

		// default current user login
		idUserForProject = utils.getLoggedInMember().getMemberId();
		allProjectOfUser = new ArrayList<Project>();
	}

	public void initProjectList() {
		allProjectOfUser = projectService.findAllByMemberId(idUserForProject);
	}

	public void initRoleAdmin() {
		// add role admin
		Role r = roleService.getRoleByObject("authority", ROLE_ADMIN);
		// create role admin
		if (r.getAuthority() == null) {
			r.setAuthority(ROLE_ADMIN);
			Long roleId = roleService.save(r);
			// assign mr khanh
			Member member = memberService
					.findMemberByEmail("khanh.tran@axonactive.vn");
			if (member != null) {
				MemberRole mr = new MemberRole(roleId, member.getMemberId());
				memberRoleService.updateRoleForMember(mr);
			}
		}
	}

	public Member getMemberByIduserForProject() {
		return memberService.findMemberById(this.idUserForProject);
	}

	public String getTeamMember(Long idMember, List<TeamProject> listTeam) {
		String strRole = "";
		for (int i = 0; i < listTeam.size(); i++) {
			TeamMember temp = teamMemberService.findTeamMemberByMemberAndTeam(
					memberService.findMemberById(idMember), listTeam.get(i)
							.getTeam());
			if (temp != null) {
				strRole = strRole
						+ teamMemberService.findTeamMemberByMemberAndTeam(
								memberService.findMemberById(idMember),
								listTeam.get(i).getTeam()).getPosition() + "; ";
			}
		}
		strRole = strRole.replaceAll("DEVELOPER", "Developer");
		strRole = strRole.replaceAll("SCRUM_MASTER", "Scrum Master");
		strRole = strRole.replaceAll("STAKEHOLDER", "Stakeholder");
		strRole = strRole.replaceAll("PRODUCT_OWNER", "Product Owner");
		if (strRole.length() > 0)
			return strRole.trim().substring(0, strRole.length() - 1);
		else
			return strRole;
	}

	public String getRoleAccount(Long idMember) {
		List<MemberRole> getMemberRole = memberRoleService
				.getRoleByIdMember(idMember);
		Role getRoleFromMember = roleService.getRoleByObject("roleId",
				getMemberRole.get(0).getRoleId());
		if (ROLE_ADMIN.equals(getRoleFromMember.getAuthority()))
			return "Admin";
		else
			return "User";
	}

	public void toViewUserProject() {
		String contextPath = FacesContext.getCurrentInstance()
				.getExternalContext().getRequestContextPath()
				+ "/admin/userproject";
		JSFUtils.redirect(contextPath);
	}

	public List<Member> getMembers() {
		return members;
	}

	public void setMembers(List<Member> members) {
		this.members = members;
	}

	public List<MemberRole> getMemberRole() {
		return memberRole;
	}

	public void setMemberRole(List<MemberRole> memberRole) {
		this.memberRole = memberRole;
	}

	public Member getMemberLogin() {
		return memberLogin;
	}

	public void setMemberLogin(Member memberLogin) {
		this.memberLogin = memberLogin;
	}

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	public Long getIdUserForProject() {
		return idUserForProject;
	}

	public void setIdUserForProject(Long idUserForProject) {
		this.idUserForProject = idUserForProject;
	}

	public List<Project> getAllProjects() {
		return allProjects;
	}

	public void setAllProjects(List<Project> allProjects) {
		this.allProjects = allProjects;
	}

	public List<Project> getAllProjectOfUser() {
		return allProjectOfUser;
	}

	public void setAllProjectOfUser(List<Project> allProjectOfUser) {
		this.allProjectOfUser = allProjectOfUser;
	}

	public void lockUser(long memberId) {
		Member member = memberService.findMemberById(memberId);
		member.setAccountLocked(true);
		memberService.update(member);
		
		// update value to view
		members.get(members.indexOf(member)).setAccountLocked(true);
	}
	
	public void assignUserToAdministrator(long memberId) {
		assign(memberId, AdminBean.ROLE_ADMIN);
	}

	private void assign(long memberId, String roleName) {
		List<MemberRole> roleList = memberRoleService
				.getRoleByIdMember(memberId);
		for (MemberRole memberRole : roleList) {
			memberRole.setRoleID(this.getRoleIdByName(roleName));
			memberRoleService.updateRoleForMember(memberRole);
		}
	}

	public void assignUserToUser(long memberId) {
		assign(memberId, AdminBean.ROLE_USER);
	}

	public Long getMemId() {
		return memId;
	}

	public void setMemId(Long memId) {
		this.memId = memId;
	}

	public long getRoleIdByName(String name) {
		return roleService.getRoleByObject("authority", name).getRoleId();
	}

	public void unlockUser(long memberId) {
		Member member = memberService.findMemberById(memberId);
		member.setAccountLocked(false);
		memberService.update(member);

		// update value to view
		members.get(members.indexOf(member)).setAccountLocked(false);
	}

	public void archiveProject(long projectId) {
		Project project = projectService.findProjectById(projectId);
		project.setIsArchived(true);
		projectService.update(project);

		// update value to view
		allProjects.get(allProjects.indexOf(project)).setIsArchived(true);

		if(null != producBacklogBean.getProjectList() && !producBacklogBean.getProjectList().isEmpty()){
			producBacklogBean.getProjectList().remove(producBacklogBean.getProjectList().indexOf(project));
			producBacklogBean.setProjectId(producBacklogBean.getProjectList().get(0).getProjectId());
		}
		
		if (!allProjectOfUser.isEmpty()) {
			allProjectOfUser.get(allProjectOfUser.indexOf(project))
					.setIsArchived(true);
		}
	}

	public void unArchiveProject(long projectId) {
		Project project = projectService.findProjectById(projectId);
		project.setIsArchived(false);
		projectService.update(project);

		// update value to view
		allProjects.get(allProjects.indexOf(project)).setIsArchived(false);

		if(null != producBacklogBean.getProjectList() && !producBacklogBean.getProjectList().isEmpty()){
			producBacklogBean.getProjectList().add(project);
			producBacklogBean.setProjectId(producBacklogBean.getProjectList().get(0).getProjectId());
		}
		
		if (!allProjectOfUser.isEmpty()) {
			allProjectOfUser.get(allProjectOfUser.indexOf(project))
					.setIsArchived(false);
		}
	}
}
