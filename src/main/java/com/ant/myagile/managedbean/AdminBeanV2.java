package com.ant.myagile.managedbean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.ant.myagile.dto.LazyFilter;
import com.ant.myagile.dto.LazySorter;
import com.ant.myagile.dto.StateLazyLoading;
import com.ant.myagile.dto.LazySorter.LAZYSORTER_VALUE;
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

@Component("adminBeanV2")
@Scope("session")
public class AdminBeanV2 implements Serializable {
	private static final Logger LOGGER = Logger
			.getLogger(SharingTopicBean.class);


	private static final String ROLE_USER = "ROLE_USER";
	private static final String ROLE_ADMIN = "ROLE_ADMIN";
	private static final long serialVersionUID = 1L;
	private List<Member> members = new ArrayList<Member>();
	private List<MemberRole> memberRole;
	private Role role;
	private Member memberLogin;
	private Long memId = -1l;

	private Long idUserForProject;
	private List<Project> allProjects = new ArrayList<Project>();
	private List<Project> allProjectOfUser = new ArrayList<Project>();
	
	private List<Member> owners;

	@Autowired
	private MemberService memberService;
	@Autowired
	private MemberRoleService memberRoleService;
	@Autowired
	private RoleService roleService;
	@Autowired
	private ProjectService projectService;
	@Autowired
	private TeamMemberService teamMemberService;
	@Autowired
	private MailService mailService;
	@Autowired
	private Utils utils;
	@Autowired
	private ProductBacklogBean producBacklogBean;
	
	private StateLazyLoading lazyLoadingMember;
	private boolean remainMember = true;
	private int totalRowMember = 0;
	
	private StateLazyLoading lazyLoadingProject;
	private boolean remainProject = true;
	private int totalRowProject = 0;
	
	private StateLazyLoading lazyLoadingUserProject;
	private boolean remainUserProject = true;
	private int totalRowProjectOfUser = 0;
	private Project selectedProject;
	private boolean edit =true;
	
	private Long newOwner;
	
	@PostConstruct
	public void init() {
		defaultStateLoadingMembers();
		initMember();
		owners = memberService.getAllActiveMembers();
		memberLogin = memberService.getMemberByUsername(utils
				.getLoggedInMember().getUsername());
		defaultStateLoadingProjects();
		initProjects();
		// default current user login
		idUserForProject = utils.getLoggedInMember().getMemberId();
		allProjectOfUser = new ArrayList<Project>();
	}
	
	//load lazy user of project
	private void initProjectList() {
		allProjectOfUser = projectService.findLazyByMemberId(idUserForProject,lazyLoadingUserProject);
		int totalRowProjectOfUser = projectService.countTotalLazyByMemberId(idUserForProject,lazyLoadingUserProject);
		this.totalRowProjectOfUser = totalRowProjectOfUser;
		if(totalRowProjectOfUser == allProjectOfUser.size()){
			remainUserProject = false;
		}else{
			remainUserProject = true;
		}
	}
	
	public void filterUserProject(){
		initProjectList();
	}
	
	public void sortUserProject() {
		String dataField = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("dataField");
		String currentValueSortField = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("valueSortField");
		lazyLoadingUserProject.getSorters().setField(dataField);
		if("none".equals(currentValueSortField.toLowerCase())){
			lazyLoadingUserProject.getSorters().setValue(LAZYSORTER_VALUE.ASC);
		}else if("asc".equals(currentValueSortField.toLowerCase())){
			lazyLoadingUserProject.getSorters().setValue(LAZYSORTER_VALUE.DESC);
		}else if("desc".equals(currentValueSortField.toLowerCase())){
			lazyLoadingUserProject.getSorters().setValue(LAZYSORTER_VALUE.ASC);
		}
		initProjectList();
	}
	
	public void loadMoreUserProject() {
		if(remainUserProject){
			int currentRow = lazyLoadingUserProject.getLimit() + lazyLoadingUserProject.getStep();
			lazyLoadingUserProject.setLimit(currentRow);
			initProjectList();
		}
	}
	
	private void defaultStateLoadingUserProjects() {
		
		lazyLoadingUserProject = new StateLazyLoading();
		lazyLoadingUserProject.setStart(0);
		lazyLoadingUserProject.setLimit(20);
		lazyLoadingUserProject.setStep(20);
		
		//default sort
		LazySorter lazySorterForUserProject = new LazySorter();
		lazySorterForUserProject.setField("dateCreate");
		lazySorterForUserProject.setValue(LAZYSORTER_VALUE.DESC);
		
		//default field
		LazyFilter lazyFilterForUserProject = new LazyFilter();
		List<String> fields = new ArrayList<String>();
		
		fields.add("projectName");
		fields.add("description");
		
		lazyFilterForUserProject.setField(fields);
		lazyFilterForUserProject.setValue("");
		
		lazyLoadingUserProject.setFilters(lazyFilterForUserProject);
		lazyLoadingUserProject.setSorters(lazySorterForUserProject);
	}
	
	//load lazy project
	private void initProjects() {
		allProjects = projectService.findLazyProjects(lazyLoadingProject);
		int totalRow = projectService.countTotalLazyProjects(lazyLoadingProject);
		this.totalRowProject = totalRow;
		if(totalRow == allProjects.size()){
			remainProject = false;
		}else{
			remainProject = true;
		}
	}
	
	public void loadMoreProject(){
		if(remainProject){
			int currentRow = lazyLoadingProject.getLimit() + lazyLoadingProject.getStep();
			lazyLoadingProject.setLimit(currentRow);
			initProjects();
		}
	}
	
	public void sortProject(){
		String dataField = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("dataField");
		String currentValueSortField = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("valueSortField");
		lazyLoadingProject.getSorters().setField(dataField);
		if("none".equals(currentValueSortField.toLowerCase())){
			lazyLoadingProject.getSorters().setValue(LAZYSORTER_VALUE.ASC);
		}else if("asc".equals(currentValueSortField.toLowerCase())){
			lazyLoadingProject.getSorters().setValue(LAZYSORTER_VALUE.DESC);
		}else if("desc".equals(currentValueSortField.toLowerCase())){
			lazyLoadingProject.getSorters().setValue(LAZYSORTER_VALUE.ASC);
		}
		initProjects();
	}
	
	private void defaultStateLoadingProjects() {
		
		lazyLoadingProject = new StateLazyLoading();
		lazyLoadingProject.setStart(0);
		lazyLoadingProject.setLimit(20);
		lazyLoadingProject.setStep(20);
		
		//default sort
		LazySorter lazySorterForProject = new LazySorter();
		lazySorterForProject.setField("dateCreate");
		lazySorterForProject.setValue(LAZYSORTER_VALUE.DESC);
		
		//default field
		LazyFilter lazyFilterForProject = new LazyFilter();
		List<String> fields = new ArrayList<String>();
		
		fields.add("projectName");
		fields.add("description");
		
		lazyFilterForProject.setField(fields);
		lazyFilterForProject.setValue("");
		
		lazyLoadingProject.setFilters(lazyFilterForProject);
		lazyLoadingProject.setSorters(lazySorterForProject);
	}

	public void filterProject(){
		initProjects();
	}
	
	//load lazy member
	private void defaultStateLoadingMembers() {
		
		lazyLoadingMember = new StateLazyLoading();
		lazyLoadingMember.setStart(0);
		lazyLoadingMember.setLimit(20);
		lazyLoadingMember.setStep(20);
		
		//default sort
		LazySorter lazySorterForMember = new LazySorter();
		lazySorterForMember.setField("tokenDate");
		lazySorterForMember.setValue(LAZYSORTER_VALUE.DESC);
		
		//default field
		LazyFilter lazyFilterForMember = new LazyFilter();
		List<String> fields = new ArrayList<String>();
		fields.add("username");
		fields.add("firstName");
		fields.add("lastName");
		fields.add("company");
		lazyFilterForMember.setField(fields);
		lazyFilterForMember.setValue("");
		
		lazyLoadingMember.setFilters(lazyFilterForMember);
		lazyLoadingMember.setSorters(lazySorterForMember);
	}
	
	public void filterMember(){
		initMember();
	}
	
	public void sortMember(){
		String dataField = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("dataField");
		String currentValueSortField = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("valueSortField");
		lazyLoadingMember.getSorters().setField(dataField);
		if("none".equals(currentValueSortField.toLowerCase())){
			lazyLoadingMember.getSorters().setValue(LAZYSORTER_VALUE.ASC);
		}else if("asc".equals(currentValueSortField.toLowerCase())){
			lazyLoadingMember.getSorters().setValue(LAZYSORTER_VALUE.DESC);
		}else if("desc".equals(currentValueSortField.toLowerCase())){
			lazyLoadingMember.getSorters().setValue(LAZYSORTER_VALUE.ASC);
		}
		initMember();
	}
	
	public void loadMoreMember(){
		if(remainMember){
			int currentRow = lazyLoadingMember.getLimit() + lazyLoadingMember.getStep();
			lazyLoadingMember.setLimit(currentRow);
			initMember();
		}
	}
	
	private void initMember() {
		members = memberService.findMemberLazy(lazyLoadingMember);
		int totalRowMember = memberService.countTotalMemberLazy(lazyLoadingMember);
		this.totalRowMember = totalRowMember;
		if(totalRowMember == members.size()){
			remainMember = false;
		}else{
			remainMember = true;
		}
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
		if(getMemberRole.isEmpty()){
			return "User";
		}
		Role getRoleFromMember = roleService.getRoleByObject("roleId",
				getMemberRole.get(0).getRoleId());
		if (ROLE_ADMIN.equals(getRoleFromMember.getAuthority()))
			return "Admin";
		else
			return "User";
	}

	public void toViewUserProject() {
		defaultStateLoadingUserProjects();
		initProjectList();
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
		assign(memberId, AdminBeanV2.ROLE_ADMIN);
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
		assign(memberId, AdminBeanV2.ROLE_USER);
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
		if(allProjects.indexOf(project) != -1){
			allProjects.get(allProjects.indexOf(project)).setIsArchived(true);
		}

		if(null != producBacklogBean.getProjectList() && !producBacklogBean.getProjectList().isEmpty()){
			producBacklogBean.getProjectList().remove(producBacklogBean.getProjectList().indexOf(project));
			producBacklogBean.setProjectId(producBacklogBean.getProjectList().get(0).getProjectId());
		}
		
		if (!allProjectOfUser.isEmpty()) {
			if(allProjectOfUser.indexOf(project) != -1){
				allProjectOfUser.get(allProjectOfUser.indexOf(project))
				.setIsArchived(true);
			}
		}
	}

	public void unArchiveProject(long projectId) {
		Project project = projectService.findProjectById(projectId);
		project.setIsArchived(false);
		projectService.update(project);

		// update value to view
		if(allProjects.indexOf(project) != -1){
			allProjects.get(allProjects.indexOf(project)).setIsArchived(false);
		}
		if(null != producBacklogBean.getProjectList() && !producBacklogBean.getProjectList().isEmpty()){
			producBacklogBean.getProjectList().add(project);
			producBacklogBean.setProjectId(producBacklogBean.getProjectList().get(0).getProjectId());
		}
		
		if (!allProjectOfUser.isEmpty()) {
			if(allProjectOfUser.indexOf(project) != -1){
				allProjectOfUser.get(allProjectOfUser.indexOf(project))
					.setIsArchived(false);
			}
		}
	}

	public boolean isRemainMember() {
		return remainMember;
	}

	public void setRemainMember(boolean remainMember) {
		this.remainMember = remainMember;
	}

	public StateLazyLoading getLazyLoadingMember() {
		return lazyLoadingMember;
	}

	public void setLazyLoadingMember(StateLazyLoading lazyLoadingMember) {
		this.lazyLoadingMember = lazyLoadingMember;
	}

	public StateLazyLoading getLazyLoadingProject() {
		return lazyLoadingProject;
	}

	public void setLazyLoadingProject(StateLazyLoading lazyLoadingProject) {
		this.lazyLoadingProject = lazyLoadingProject;
	}

	public boolean isRemainProject() {
		return remainProject;
	}

	public void setRemainProject(boolean remainProject) {
		this.remainProject = remainProject;
	}

	public StateLazyLoading getLazyLoadingUserProject() {
		return lazyLoadingUserProject;
	}

	public void setLazyLoadingUserProject(StateLazyLoading lazyLoadingUserProject) {
		this.lazyLoadingUserProject = lazyLoadingUserProject;
	}

	public boolean isRemainUserProject() {
		return remainUserProject;
	}

	public void setRemainUserProject(boolean remainUserProject) {
		this.remainUserProject = remainUserProject;
	}

	public int getTotalRowMember() {
		return totalRowMember;
	}

	public void setTotalRowMember(int totalRowMember) {
		this.totalRowMember = totalRowMember;
	}

	public int getTotalRowProject() {
		return totalRowProject;
	}

	public void setTotalRowProject(int totalRowProject) {
		this.totalRowProject = totalRowProject;
	}

	public int getTotalRowProjectOfUser() {
		return totalRowProjectOfUser;
	}

	public void setTotalRowProjectOfUser(int totalRowProjectOfUser) {
		this.totalRowProjectOfUser = totalRowProjectOfUser;
	}
	
	public void saveNewOwner()
	{
		try 
		{
			if (newOwner != null && selectedProject != null) 
			{
				this.selectedProject.setOwner(memberService.findMemberById(newOwner));
				LOGGER.warn("Edit owner proid: " + selectedProject.getProjectId());

				projectService.update(selectedProject);

				LOGGER.warn("new ownerid: " + selectedProject.getOwner().getMemberId());

				newOwner = null;
				edit = false;

				JSFUtils.addSuccessMessage("msgs", this.utils.getMessage("myagile.UpdateSuccess", null));
			}
		} 
		catch (Exception ex) 
		{
			JSFUtils.addWarningMessage("msgs", "something Wrong!");
			LOGGER.error("saveNewOwner error: " + ex.getMessage());
		}
	}
	
	public Project getSelectedProject() {
		return selectedProject;
	}

	public void setSelectedProject(Project selectedProject) {
		this.selectedProject = selectedProject;
	}

	public boolean isEdit() {
		return edit;
	}

	public void setEdit(boolean edit) {
		this.edit = edit;
	}

	

	public Long getNewOwner() {
		return newOwner;
	}

	public void setNewOwner(Long newOwner) {
		this.newOwner = newOwner;
	}

	public List<Member> getOwners() {
		return owners;
	}

	public void setOwners(List<Member> owners) {
		this.owners = owners;
	}
	

}
