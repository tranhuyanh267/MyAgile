package com.ant.myagile.managedbean;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.primefaces.context.RequestContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.ant.myagile.model.Member;
import com.ant.myagile.model.Project;
import com.ant.myagile.model.Team;
import com.ant.myagile.model.TeamMember;
import com.ant.myagile.model.TeamProject;
import com.ant.myagile.service.HistoryService;
import com.ant.myagile.service.KanbanIssueService;
import com.ant.myagile.service.KanbanStatusService;
import com.ant.myagile.service.KanbanSwimlineService;
import com.ant.myagile.service.MailService;
import com.ant.myagile.service.MemberRoleService;
import com.ant.myagile.service.MemberService;
import com.ant.myagile.service.MyTeamLinkedService;
import com.ant.myagile.service.ProjectService;
import com.ant.myagile.service.SprintService;
import com.ant.myagile.service.TeamMemberService;
import com.ant.myagile.service.TeamProjectService;
import com.ant.myagile.service.TeamService;
import com.ant.myagile.service.WikiService;
import com.ant.myagile.utils.JSFUtils;
import com.ant.myagile.utils.StringUtils;
import com.ant.myagile.utils.Utils;
import com.ant.myagile.viewmodel.TeamMemberProjects;

@Component("teamBean")
@Scope("session")
public class TeamBean implements Serializable 
{
	private static final Logger LOGGER = Logger.getLogger(TeamBean.class);
	
	private static final long DUE_TOKEN_DATE = 172800000; // 172800000 = (2*24*60*60*1000) <=> two days
	private static final long serialVersionUID = 1L;
	
	private static final String MY_TEAM_URL = "https://myteam.axonactive.vn/home/show/";
	 public static final String PRODUCT_OWNER = "PRODUCT_OWNER";

	private Team team;
	private Team teamSelected;
	private Team deletedTeam;
	private Team teamTemp;
	private List<Long> selectedProjects;
	private List<Team> teamList;
	private List<Team> listTeamsForSelection;
	private List<TeamMember> teamMemberList;
	private List<TeamMemberProjects> teamMemberProjectsList;
	private List<Long> selectedProjectIdsForPO;
	private List<Project> projectsOfTeam;
	private Project selectedProject;
	private String mailActive = "";
	private HashMap<String, String> emailReferToIdList = new HashMap<String, String>();
	private boolean hideTeam = false;
	private Long selectedMemberId;
	private String newRole;

	@Autowired
	private TeamService teamService;
	@Autowired
	private MemberService memberService;
	@Autowired
	private ProjectService projectService;
	@Autowired
	private TeamProjectService teamProjectService;
	@Autowired
	private TeamMemberService teamMemberServices;
	@Autowired
	private MailService mailServices;
	@Autowired
	MemberRoleService memberRoleService;
	@Autowired
	private HistoryService historyService;
	@Autowired
	private Utils utils;
	@Autowired
	private MyTeamLinkedService myTeamLinkedService;
	@Autowired
	private KanbanStatusService kanbanStatusService;
	@Autowired
	private KanbanSwimlineService kanbanSwimlineService;
	@Autowired
	private WikiService wikiService;
	@Autowired
	private KanbanIssueService kanbanIssueService;
	@Autowired 
	private ProductBacklogBean productBacklogBean;
	@Autowired
	private SprintService sprintService;

	public void initPreview() 
	{
		if (JSFUtils.isPostbackRequired()) 
		{
			this.teamMemberList = new ArrayList<TeamMember>();
			Member memberOwner = this.utils.getLoggedInMember();
			this.teamList = this.teamService.findTeamsByMemberAndOwner(memberOwner);
			excludeHiddenTeamsForSelection();
			
			if (this.listTeamsForSelection != null && !this.listTeamsForSelection.isEmpty()) 
			{
				if (this.team == null || this.team.getTeamId() == null || this.team.getTeamId() == 0) {
					this.team = this.listTeamsForSelection.get(0);
				}
				if (this.teamMemberList == null || this.teamMemberList.size() <= 0) {
					this.teamMemberList = this.memberService.findTeamMemberListByTeamId(this.team.getTeamId());
				}
			}
		}
	}
	
	public void assignProductOnwerforProjects()
	{
		List<Project> selectedProjectsToView = new ArrayList<Project>();
		TeamMember selectedMemberToUpdate = null;
		//get old data of member
		for (TeamMemberProjects teamMemPro : teamMemberProjectsList) 
		{
		    if (teamMemPro.getTeamMember().getMember().getMemberId().longValue() == this.selectedMemberId.longValue())
		    {
			selectedMemberToUpdate = teamMemPro.getTeamMember();
			teamMemberProjectsList.remove(teamMemPro);
			break;
		    }
		}
		//update PO of projects
	    	boolean isUpdated = false;
		for (Project project : projectsOfTeam) 
		{
		    String productOwnerIds = project.getProductOwnerIds();
		    List<Long> productOwnerList = StringUtils.parseStringToList(productOwnerIds);
		    
		    if (this.selectedProjectIdsForPO != null && this.selectedProjectIdsForPO.contains(project.getProjectId()))
		    {
			if (!productOwnerList.contains(this.selectedMemberId)){
			    productOwnerList.add(this.selectedMemberId);
			    isUpdated = true;
			}
		    } else {
			if (productOwnerList.contains(this.selectedMemberId)){
			    productOwnerList.remove(this.selectedMemberId);
			    isUpdated = true;
			} 
		    }
		    
		    if (isUpdated)
		    {
			Long ownerId = project.getOwner().getMemberId();
			if (productOwnerList.contains(ownerId)){
			    productOwnerList.remove(ownerId);
			}
        		    String newProductOwnerIds = StringUtils.parseListToString(productOwnerList);
        		    project.setProductOwnerIds(newProductOwnerIds);
        		    projectService.update(project);
        		    selectedProjectsToView.add(project);
		    }
		    
		    isUpdated = false;
		}
		//add memberProject into list
		TeamMemberProjects teamMemPro = new TeamMemberProjects(selectedMemberToUpdate, selectedProjectsToView);
		this.teamMemberProjectsList.add(teamMemPro );
		//reset selected project list
		selectedProjectIdsForPO = new ArrayList<Long>();
	    }
	

	/**
	 * Return true if input email is axon active mail
	 * 
	 * @param email
	 * @return
	 */
	public boolean checkAxonActiveMail(String email)
	{
		if (email.indexOf("@axonactive.vn") > 0) 
		{
			return true;
		}
		
		return false;
	}

	/**
	 * Return id on myteam.axonactive.vn with email input
	 * 
	 * @param email
	 * @return
	 */
	public String getIdByEmail(String email) 
	{
		if (checkAxonActiveMail(email)) 
		{
			return myTeamLinkedService.findIdByEmail(email);
		}
		return "";
	}

	/**
	 * Return myteam.axonactive.vn URL of a member's email input
	 * 
	 * @param email
	 * @return
	 */
	public String getMyTeamUrl(String email) 
	{
		if (!checkAxonActiveMail(email)) 
		{
			return "";
		} else {
			if (getIdByEmail(email).equals("")) {
				return "";
			} else {
				return MY_TEAM_URL + getIdByEmail(email);
			}
		}
	}

	/**
	 * Find all teams which are joined by logged in member and teams are created
	 * by logged in member
	 * 
	 * @return List of Team
	 */
	public List<Team> findTeamsByUser() 
	{
		try {
			Member memberOwner = this.utils.getLoggedInMember();
			this.teamList = this.teamService.findTeamsByMemberAndOwner(memberOwner);
			excludeHiddenTeamsForSelection();
		} catch (Exception ex) {
			this.setTeam(new Team());
		}
		return this.listTeamsForSelection;
	}

	public void setSelectedProjectsForTeam() 
	{
		List<Project> projectsTemp = new ArrayList<Project>();
		projectsTemp = this.projectService.findProjectsAssignedToTeam(this.teamTemp);
		List<Long> longTemp = new ArrayList<Long>();
		for (Project p : projectsTemp) {
			longTemp.add(p.getProjectId());
		}
		this.setSelectedProjects(longTemp);
	}

	/**
	 * Find project(s) created by user
	 * 
	 * @return a List of Project(s)
	 */
	public List<Project> findProjectsByUser() 
	{
		List<Project> projects = new ArrayList<Project>();
		Member memberOwner = this.utils.getLoggedInMember();
		projects = this.projectService.findByOwnerId(memberOwner.getMemberId());
		if (this.teamTemp != null && this.teamTemp.getTeamId() != null && this.teamTemp.getTeamId() != 0) {
			this.setSelectedProjectsForTeam();
		} else {
			this.setSelectedProjects(new ArrayList<Long>());
		}
		return projects;
	}

	public Team findTeamById(long id) 
	{
		Team editTeam = this.teamService.findTeamByTeamId(id);
		return editTeam;
	}

	/**
	 * Find project(s) assinged to a team
	 * 
	 * @param team
	 *            - instance of Team
	 * @return a List of Project(s)
	 */
	public List<Project> projectAssignedToTeam(Team team) 
	{
		List<Project> projectList = new ArrayList<Project>();
		if (team.getTeamId() != null && team.getTeamId() != 0) {
			projectList = this.projectService.findProjectsAssignedToTeam(this.teamService.findTeamByTeamId(team.getTeamId()));
		}
		return projectList;
	}

	public List<Project> findProjectAssignedToTeam() {
		this.setProjectsOfTeam(projectAssignedToTeam(this.team));
		return this.projectsOfTeam;
	}

	public List<Project> findProjectAssignedToTeam(Long teamId) 
	{
		return projectAssignedToTeam(this.teamService.findTeamByTeamId(teamId));
	}

	public String toAddTeamPage() {
		this.deleteTempLogo();
		this.resetValue();
		return "addTeam?faces-redirect=true";
	}

	public String toEditTeamPage() 
	{
		this.deleteTempLogo();
		this.setTeamTemp(this.teamService.findTeamByTeamId(this.team.getTeamId()));
		if(null == teamTemp.getValidTo()){
			hideTeam = false;
		}else{
			hideTeam = true;
		}
		return "addTeam?faces-redirect=true";
	}

	public void resetForm() 
	{
		JSFUtils.resetForm("teamForm");
	}

	public void resetValue() 
	{
		this.setTeamTemp(new Team());
		this.getTeamTemp().setEstablishedDate(new Date());
		this.setSelectedProjects(new ArrayList<Long>());
	}

	public void createNewTeam() 
	{
		this.deleteTempLogo();
		this.resetForm();
		this.resetValue();
		RequestContext.getCurrentInstance().addCallbackParam("create", true);
	}

	public void editTeam() 
	{
		this.deleteTempLogo();
		this.resetForm();
		this.resetValue();
		RequestContext context = RequestContext.getCurrentInstance();
		try {
			this.setTeamTemp(this.teamService.findTeamByTeamId(this.teamSelected.getTeamId()));
			hideTeam = false;
			if(null != teamTemp.getValidTo())
			{
			    hideTeam = true;
			}
			context.addCallbackParam("edit", true);
		} catch (Exception a) {
			this.setTeamTemp(new Team());
			context.addCallbackParam("edit", false);
		}
	}

	public void deleteTempLogo() 
	{
		if (this.teamTemp != null && this.teamTemp.getTeamId() == null && this.teamTemp.getLogo() != null) {
			this.teamService.deleteTempLogo(this.teamTemp.getLogo());
		}
	}

	/**
	 * Set new logo for Team, and delete old logo (if exist)
	 */
	public void newTeamLogo() 
	{
		String filename = JSFUtils.getRequestParameter("filename");
		String newFileName = this.teamService.teamImageNameProcess(FilenameUtils.removeExtension(filename));
		String oldFileName = this.teamTemp.getLogo();
		this.teamService.deleteTempLogo(oldFileName);
		String newLogoFilename = this.teamService.renameLogo(filename, newFileName);
		this.teamTemp.setLogo(newLogoFilename);
		if (this.teamTemp.getTeamId() != null && this.teamTemp.getTeamId() != 0) {
			this.teamService.deleteLogo(this.teamTemp, oldFileName);
			this.teamService.moveLogo(this.teamTemp, this.teamTemp.getLogo());
			this.teamService.update(this.teamTemp);
			if (this.team.getTeamId().equals(this.teamTemp.getTeamId())) {
				this.setTeam(this.teamTemp);
			}
			RequestContext context = RequestContext.getCurrentInstance();
			context.update("listTeam");
		}
	}

	/**
	 * Create a new Team with information entered by user in view, and save this
	 * team into database
	 * 
	 */
	
	
	@SuppressWarnings("static-access")
	public void saveTeam() 
	{
		if (this.teamTemp.getEstablishedDate() == null) 
		{
			this.teamTemp.setEstablishedDate(new Date());
		}
		
		if (this.teamTemp.getTeamId() == null) {
			Member memberOwner = this.utils.getLoggedInMember();
			this.teamTemp.setOwner(memberOwner);
			if (!this.teamService.checkTeamNameExisted(this.teamTemp)) {
				JSFUtils.addErrorMessage("teamForm:teamName", this.utils.getMessage("myagile.team.ExistTeamName", null));
				return;
			} else if (!this.teamService.checkMailGroupExisted(this.teamTemp)) {
				JSFUtils.addErrorMessage("teamForm:mailGroup", this.utils.getMessage("myagile.team.ExistMailGroup", null));
				return;
			} else {
				//Remove Control Characters
				this.teamTemp.setDescription(this.teamTemp.getDescription().trim().replaceAll("\\p{Cntrl}", ""));
				this.teamTemp.setTeamName(this.teamTemp.getTeamName().replaceAll("\\p{Cntrl}", ""));
				if (this.teamService.save(this.teamTemp)) {
					JSFUtils.addCallBackParam("edit", false);
					JSFUtils.addCallBackParam("save", true);
					kanbanStatusService.createStatusTodoAndDoneForTeam(teamTemp);
					kanbanSwimlineService.createSwimlineForNewTeam(teamTemp);
				}
				Team newTeam = this.teamService.findTeamByTeamId(this.teamTemp.getTeamId());
				this.teamService.moveLogo(newTeam, this.teamTemp.getLogo());
				newListProjectAssignedToTeam(newTeam);
				try {
					this.teamList.add(this.teamTemp);
					this.listTeamsForSelection.add(this.teamTemp);
				} catch (Exception e) {
				}
				resetValue();
			}
		} else {
			//Remove Control Characters
			this.teamTemp.setDescription(this.teamTemp.getDescription().trim().replaceAll("\\p{Cntrl}", ""));
			this.teamTemp.setTeamName(this.teamTemp.getTeamName().replaceAll("\\p{Cntrl}", ""));
			this.teamTemp.setNote(this.teamTemp.getNote().replaceAll("\\p{Cntrl}", ""));
			
			if(hideTeam == true)
			{
    			    if (this.teamTemp.getNote().trim().isEmpty())
    			    {
    				JSFUtils.addErrorMessage("teamForm:hideReason", "Note for hidden is required.");
    				return;
    			    }
    			    if(null == teamTemp.getValidTo())
    			    {
				teamTemp.setValidTo(new Date());
    			    }
			}else{
				teamTemp.setValidTo(null);
			}
			
			if (this.teamService.update(this.teamTemp)) {
				JSFUtils.addCallBackParam("edit", true);
				JSFUtils.addCallBackParam("save", false);
			}
			this.updateListProjectAssigned(this.teamTemp);
			this.setTeam(this.teamTemp);
			this.teamList.set(this.teamList.indexOf(this.teamService.findTeamByTeamId(this.teamTemp.getTeamId())), this.teamTemp);
			//resetValue();
		}
	}

	/**
	 * Set which team get which project to do, it means add a record in
	 * TeamProject table in database
	 * 
	 * @param team
	 *            - instance of Team
	 * @param project
	 *            - instance of Project
	 */
	public void assignProjectToTeam(Team team, Project project) 
	{
		TeamProject tp = new TeamProject();
		tp.setTeam(team);
		tp.setProject(project);
		tp.setStartDate(new Date());
		tp.setEndDate(null);
		this.teamProjectService.create(tp);
		this.historyService.saveHistoryForTeamProject(tp, "assign", "Team");
	}

	/**
	 * Set team to stop doing a project by updating the end day is current day
	 * 
	 * @param team
	 *            - instance of Team
	 * @param project
	 *            - instance of Project
	 */
	public void unassignProjectToTeam(Team team, Project project)
	{
		TeamProject tp = new TeamProject();
		tp = this.teamProjectService.findTeamProjectByProjectAndTeam(project, team);
		if (tp.getEndDate() == null) {
			this.historyService.saveHistoryForTeamProject(tp, "remove", "Team");
			tp.setEndDate(new Date());
		}
		this.teamProjectService.update(tp);
	}

	/**
	 * Set team to continue doing a project by updating the end day is null
	 * 
	 * @param team
	 *            - instance of Team
	 * @param project
	 *            - instance of Project
	 */
	public void assignProjectAgainToTeam(Team team, Project project)
	{
		TeamProject tp = new TeamProject();
		tp = this.teamProjectService.findTeamProjectByProjectAndTeam(project, team);
		if (tp.getEndDate() != null) {
			this.historyService.saveHistoryForTeamProject(tp, "reassign", "Team");
		}
		tp.setEndDate(null);
		this.teamProjectService.update(tp);
	}

	public void newListProjectAssignedToTeam(Team team) 
	{
		for (Long projectId : this.selectedProjects) {
			Project project = this.projectService.findProjectById(projectId);
			assignProjectToTeam(team, project);
		}
	}

	public void updateListProjectAssigned(Team team) 
	{
		List<Project> listProjectOwner = new ArrayList<Project>();
		Member memberOwner = this.utils.getLoggedInMember();
		listProjectOwner = this.projectService.findByOwnerId(memberOwner.getMemberId());
		for (Long projectId : this.selectedProjects) {
			Project project = this.projectService.findProjectById(projectId);
			if (listProjectOwner.contains(project)) {
				try {
					assignProjectAgainToTeam(team, project);
				} catch (Exception e) {
					assignProjectToTeam(team, project);
				}
			} else if (!(listProjectOwner.contains(project))) {
				assignProjectToTeam(team, project);
			}
		}
		for (Project oldProject : listProjectOwner) {
			if (!this.selectedProjects.contains(oldProject.getProjectId())) {
				try {
					unassignProjectToTeam(team, oldProject);
				} catch (Exception e) {
				}
			}
		}
	}

	public void handleTeamChange() {
		this.team = this.teamService.findTeamByTeamId(this.team.getTeamId());
		this.teamMemberList = this.memberService.findTeamMemberListByTeamId(this.team.getTeamId());
	}

	public void checkRemoveOwnSelf(Long memberId) 
	{
		if (this.utils.getLoggedInMember().getMemberId().equals(memberId)) {
			String contextPath = FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath() + "/j_spring_security_logout";
			JSFUtils.redirect(contextPath);
		}
	}

	/**
	 * Remove a member from a team
	 * 
	 * @param teamMemberId
	 *            - ID of TeamMember, Long number format
	 * @param memberId
	 *            - ID of Member, Long number format
	 */
	public void removeMember(Long teamMemberId, Long memberId) 
	{
		RequestContext context = RequestContext.getCurrentInstance();
		this.teamMemberServices.delete(this.teamMemberServices.findTeamMemberById(teamMemberId));
		if (this.team.getTeamId() != null || this.team.getTeamId() != 0) {
			this.teamMemberList = this.memberService.findTeamMemberListByTeamId(this.team.getTeamId());
		} else {
			this.teamMemberList = new ArrayList<TeamMember>();
		}
		this.checkRemoveOwnSelf(memberId);
		context.update("selectTeamForm");
		context.update("teamDetail");
	}

	/**
	 * Change position of a member in team
	 * 
	 * @param teamMemberId
	 *            - ID of TeamMember, Long number format
	 * @param position
	 *            - String, value can be 'Developer', 'Scrum Master' or
	 *            'Stakeholder'
	 */
	@SuppressWarnings("static-access")
	public String checkName(Member member) 
	{
		return this.utils.checkName(member);
	}

	public void changePosition(Long teamMemberId, String position) 
	{
	    	this.newRole = position;
	    	TeamMember teamMember = this.teamMemberServices.findTeamMemberById(teamMemberId);
	    	
	    	this.selectedMemberId = teamMember.getMember().getMemberId();
		this.teamMemberServices.changePositionMember(teamMember, position);
		//initialize data for PO 
		if (PRODUCT_OWNER.equalsIgnoreCase(position))
		{
		    initializeDataForSelectedProjectIdsOfPO();
		}
	}

	/**
	 * Pick up mail address(es) in a string then return a list of email
	 * 
	 * @param listEmail
	 *            - A String consist of mail(s) address
	 * @return Set<String> - A list of mail(s) address
	 */
	public Set<String> getListEmail(String listEmail)
	{
		Set<String> arrEmails = new HashSet<String>();
		String[] mails = listEmail.split(";");
		if (mails.length == 0) {
			if (listEmail.trim().length() > 0) {
				arrEmails.add(listEmail.trim());
			}
		} else {
			for (int i = 0; i < mails.length; i++) {
				if (mails[i].trim().length() > 0) {
					arrEmails.add(mails[i].trim());
				}
			}
		}
		return arrEmails;
	}

	/**
	 * Check format of a list of email address(es)
	 * 
	 * @param mails
	 *            - A Set of email address(es)
	 * @return true if all email address(es) in list correct format, false in
	 *         otherwise
	 */
	public boolean checkListMail(Set<String> mails) 
	{
		if (mails.size() > 0) {
			for (String mail : mails) {
				if (!this.mailServices.checkMail(mail)) {
					return false;
				}
			}
		}
		return true;
	}

	public void showMessages(List<FacesMessage> messageList) 
	{
		for (FacesMessage message : messageList) 
		{
			FacesContext.getCurrentInstance().addMessage(null, message);
		}
	}

	/**
	 * Set account active when logging in first time to system
	 * 
	 * @throws IOException
	 */
	@SuppressWarnings("static-access")
	public void activeAccount() throws IOException 
	{
		List<FacesMessage> messageList = new ArrayList<FacesMessage>();
		try {
			FacesContext facesContext = FacesContext.getCurrentInstance();
			String tk = facesContext.getExternalContext().getRequestParameterMap().get("tk");
			TeamMember tm = this.teamMemberServices.findTeamMemberByToken(tk);
			if (tm != null) {
				Date today = new Date();
				Date datetoken = tm.getTokenDate();
				if ((today.getTime() - datetoken.getTime()) > DUE_TOKEN_DATE) {
					this.teamMemberServices.delete(tm);
					this.memberService.delete(tm.getMember());
					JSFUtils.addErrorMessage("msgs", this.utils.getMessage("myagile.team.ActiveMember", null));
					messageList.add(new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "ERROR"));
				} else {
					if (!tm.getMember().getUsername().equals(this.utils.getLoggedInMember().getUsername())) {
						JSFUtils.addErrorMessage("msgs", this.utils.getMessage("myagile.member.WarringNotLogin", null));
					} else {
						tm.setIsAccepted(true);
						tm.setToken("");
						this.teamMemberServices.update(tm);

						Member m = tm.getMember();
						m.setEnabled(true);
						m.setActive(true);
						this.memberService.update(m);
						JSFUtils.redirect(this.mailServices.getRealPath() + "pages/profile/edit.xhtml");
					}
				}
			} else {
				JSFUtils.addErrorMessage("msgs", this.utils.getMessage("myagile.member.TokenInvalid", null));
			}
		} catch (Exception e) {
			JSFUtils.addErrorMessage("msgs", this.utils.getMessage("myagile.member.ActiveAccountUnsuccess", null));
		}
		this.showMessages(messageList);
	}

	/**
	 * Send an email to user(s) to announce about inviting to join team, with
	 * username and password
	 * 
	 * @throws Exception
	 */
	@SuppressWarnings("static-access")
	public void sendMailToNewUser() throws Exception 
	{
		Team t = this.team;
		List<FacesMessage> messageList = new ArrayList<FacesMessage>();
		Set<String> emails = getListEmail(this.mailActive);
		
		if (checkListMail(emails)) {
			if (emails.size() > 0) {
				
				for (String mail : emails) {
					mail = mail.toLowerCase();
					Member m = this.memberService.getMemberByUsername(mail);
					if (this.teamMemberServices.checkMemberInTeam(m, t)) {
						Object[] params = { mail };
						messageList.add(new FacesMessage(FacesMessage.SEVERITY_WARN, this.utils.getMessage("myagile.team.InviteExist", params), "WARNING"));
					} else {
						try {
							String path = this.mailServices.getRealPath();
							String tk = this.mailServices.createAutoNumber();
							String activeUrl = path + "pages/team/active.xhtml?tk=" + tk;
							String content = "<p>This mail has sent from MyAgile system by " + this.utils.checkName(this.utils.getLoggedInMember()) + "</p>" + "<h4>You had been added to team <b>"
									+ t.getTeamName() + "</b></h4>" + "<p><a href=\"" + activeUrl + "\">Click here</a> to accept!</p>"
									+ "<p> If you can't click on this, please copy this text below and paste to your browser: <br>"
									+ "<a href=\"" + activeUrl + "\">" + activeUrl + "</a></p>";
							Member mem = this.memberService.getMemberByUsername(mail);
							if (mem == null) {
								String passGenerate = this.memberService.generatePasswordDefault();
								mem = new Member(mail, false, this.utils.encodePassword(passGenerate), true);
								mem.setAvatar("user.png");
								mem.setActive(false);
								mem.setlDapUsername("");
								this.memberService.saveAndAddRoleMember(mem, (long) 1);

								content = "<p>Hi,</p>This is the first time you visit our website.Your information as below :" + "<ul>" + "<li>Username: " + mail + "</li>" + "<li>Password: "
										+ passGenerate + "</li>" + "</ul>" + content;
								TeamMember tm = new TeamMember(t, this.memberService.getMemberByUsername(mail), tk);
								this.teamMemberServices.save(tm);
								this.mailServices.sendMail("Invite to team", content, mail);
								messageList.add(new FacesMessage(mail + " sent"));
							}else{
								if (this.memberService.checkLockedMember(mail)) { 
									messageList.add(new FacesMessage(FacesMessage.SEVERITY_ERROR, this.utils.getMessage("myagile.MemberIsLocked", null), "ERROR")); 
								}else{
									TeamMember tm = new TeamMember(t, this.memberService.getMemberByUsername(mail), tk);
									this.teamMemberServices.save(tm);
									this.mailServices.sendMail("Invite to team", content, mail);
									messageList.add(new FacesMessage(mail + " sent"));
								}
							}
							
						} catch (Exception e) {
							messageList.add(new FacesMessage(FacesMessage.SEVERITY_ERROR, this.utils.getMessage("myagile.member.SendEmailError", null), "ERROR"));
						}
					}
				}
				this.teamMemberList = this.memberService.findTeamMemberListByTeamId(this.team.getTeamId());
			} else {
				messageList
						.add(new FacesMessage(FacesMessage.SEVERITY_ERROR, this.utils.getMessage("myagile.team.InviteNullEmail", null), this.utils.getMessage("myagile.team.InviteNullEmail", null)));
			}

		} else {
			messageList.add(new FacesMessage(FacesMessage.SEVERITY_ERROR, this.utils.getMessage("myagile.team.IncorrectEmail", null), this.utils.getMessage("myagile.team.IncorrectEmail", null)));
		}
		this.showMessages(messageList);
	}

	/**
	 * Return a string consist of Skype ID of a team
	 * 
	 * @return String
	 */
	public String getSkypeList() 
	{
		if (this.teamMemberList.size() == 0) {
			this.teamMemberList = this.memberService.findTeamMemberListByTeamId(this.team.getTeamId());
		}
		String skypeList = "";
		Member currentMember = this.utils.getLoggedInMember();
		for (TeamMember member : this.teamMemberList) {
			if (!member.getMember().getMemberId().equals(currentMember.getMemberId())) {
				skypeList += member.getMember().getSkype() + ";";
			}
		}
		return skypeList;
	}

	/**
	 * Show fullname or email of owner
	 * 
	 * @param member
	 * @return
	 */
	@SuppressWarnings("static-access")
	public String getProductOwner(Member member) {
		try {
			return this.utils.checkName(member);
		} catch (Exception e) {
			return "";
		}
	}

	public String getMailActive() {
		return this.mailActive;
	}

	public void setMailActive(String mailActive) {
		this.mailActive = mailActive;
	}

	public void setTeam(Team team) {
		this.team = team;
	}

	public Team getTeam() {
		if (this.team == null) {
			this.team = new Team();
		}
		return this.team;
	}

	public Team getTeamTemp() {
		if (this.teamTemp == null) {
			this.teamTemp = new Team();
			this.teamTemp.setEstablishedDate(new Date());
		}
		return this.teamTemp;
	}

	public void setTeamTemp(Team teamTemp) {
		this.teamTemp = teamTemp;
	}

	public List<TeamMember> getTeamMemberList() {
		if (this.teamMemberList == null) {
			this.teamMemberList = new ArrayList<TeamMember>();
		}
		return this.teamMemberList;
	}

	public List<Team> getTeamList() {
		if (this.teamList == null) {
			this.teamList = new ArrayList<Team>();
		}
		return this.teamList;
	}

	public void setTeamList(List<Team> teamList) {
		this.teamList = teamList;
	}

	public List<Long> getSelectedProjects() {
		if (this.selectedProjects == null) {
			this.selectedProjects = new ArrayList<Long>();
		}
		return this.selectedProjects;
	}

	public void setSelectedProjects(List<Long> selectedProjects) {
		this.selectedProjects = selectedProjects;
	}

	public Team getTeamSelected() {
		return this.teamSelected;
	}

	public void setTeamSelected(Team teamSelected) {
		this.teamSelected = teamSelected;
	}

	public HashMap<String, String> getEmailReferToIdList() {
		return emailReferToIdList;
	}

	public void setEmailReferToIdList(HashMap<String, String> emailReferToIdList) {
		this.emailReferToIdList = emailReferToIdList;
	}
	
	

	public boolean isTeamOwner(Team team)
	{
		boolean flag = false;
		Member user = this.utils.getLoggedInMember();
		if (team.getOwner().getMemberId().equals(user.getMemberId())){
			flag = true;
		}
		return flag;
	}
	
	public void deleteTeam()
	{
		teamMemberServices.deleteAllTeamMemberInTeam(deletedTeam);
		teamProjectService.deleteAllTeamProjectByTeam(deletedTeam);
		wikiService.delete(wikiService.getByTeamId(deletedTeam.getTeamId()));
		kanbanIssueService.deleteAllKanbanIssuesByTeam(deletedTeam);
		
		kanbanSwimlineService.deleteAllSwimlineInTeam(deletedTeam);
		kanbanStatusService.deleteAllKanbanStatusInTeam(deletedTeam);
		sprintService.deleteAllSprintInTeam(deletedTeam);
		teamService.delete(deletedTeam);
		//init teamList
		Member memberOwner = this.utils.getLoggedInMember();
		this.teamList = this.teamService.findTeamsByMemberAndOwner(memberOwner);
		excludeHiddenTeamsForSelection();
		
		if(!this.listTeamsForSelection.isEmpty())
		{
			this.team = this.listTeamsForSelection.get(0);
		}
		LOGGER.debug("can not get first team");
		//clear object bean
		FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("sprintBacklogBean", null);
		FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("dashboardBean", null);
		FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("kanbanBean", null);
		FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("meetingBean", null);
		FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("wikiBean", null);
		FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("retroMB", null);
		FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("sprintBean", null);
		FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("homeBean", null);
		FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("issueBean", null);
		FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("productBacklogBean", null);
		FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("projectBean", null);
		FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("wizardBean", null);
	}

	public Team getDeletedTeam() {
		return deletedTeam;
	}

	public void setDeletedTeam(Team deletedTeam) {
		this.deletedTeam = deletedTeam;
	}
	
	public void linkToProductBacklogPage() throws IOException 
	{
		String contextPath = FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath() + "/productbacklog";
		FacesContext.getCurrentInstance().getExternalContext().redirect(contextPath);
	}

	public boolean isHideTeam() {
		return hideTeam;
	}

	public void setHideTeam(boolean hideTeam) {
		this.hideTeam = hideTeam;
	}

	
	public List<Long> getSelectedProjectIdsForPO() {
	    return selectedProjectIdsForPO;
	}

	public void setSelectedProjectIdsForPO(List<Long> selectedProjectIdsForPO) {
	    this.selectedProjectIdsForPO = selectedProjectIdsForPO;
	}

	public List<Project> getProjectsOfTeam() {
		return projectsOfTeam;
	}
	
	private void initializeDataForSelectedProjectIdsOfPO()
	{
	    this.selectedProjectIdsForPO = new ArrayList<Long>();
	    if (this.projectsOfTeam != null && this.projectsOfTeam .size() > 0)
	    {
		for (Project pro : this.projectsOfTeam) 
		{
		    List<Long> productOwnerList = StringUtils.parseStringToList(pro.getProductOwnerIds());
		    if (productOwnerList != null && productOwnerList.contains(this.selectedMemberId))
		    {
			selectedProjectIdsForPO.add(pro.getProjectId());
		    } else if (pro.getOwner().getMemberId().longValue() == this.selectedMemberId.longValue()) {
			selectedProjectIdsForPO.add(pro.getProjectId());
		    }
		}
	    }
	}

	public void setProjectsOfTeam(List<Project> projectsOfTeam) 
	{
		this.projectsOfTeam = projectsOfTeam;
		initDataForPOProject();
	}

	private void initDataForPOProject() {
	    if (projectsOfTeam != null && projectsOfTeam.size() > 0)
	    {
	        if (this.teamMemberList != null && this.teamMemberList.size() > 0)
	        {
	    	List<Project> projectsOfPO = null;
	    	teamMemberProjectsList = new ArrayList<TeamMemberProjects>();
	    	for (TeamMember teamMem : this.teamMemberList) 
	    	{
	    	    Long memId = teamMem.getMember().getMemberId();
	    	    projectsOfPO = new ArrayList<Project>();
	    	    for (Project project : projectsOfTeam) 
	    	    {
	    		    List<Long> productOwnerList = StringUtils.parseStringToList(project.getProductOwnerIds());
	    		    if (productOwnerList != null && productOwnerList.contains(memId))
	    		    {
	    			projectsOfPO.add(project);
	    		    } else if (project.getOwner().getMemberId().longValue() == memId.longValue()){
	    			projectsOfPO.add(project);
	    		    }
	    			
	    	    }
	    	    //add data into teamMemberProjectsList
	    	    TeamMemberProjects teamMemPro = new TeamMemberProjects(teamMem, projectsOfPO);
	    	    teamMemberProjectsList.add(teamMemPro);
	    	}
	        }
	    }
	}
	
	
	public List<Team> getListTeamsForSelection() {
	    return listTeamsForSelection;
	}

	public void setListTeamsForSelection(List<Team> listTeamsForSelection) {
	    this.listTeamsForSelection = listTeamsForSelection;
	}
	
	public List<TeamMemberProjects> getTeamMemberProjectsList() {
	    return teamMemberProjectsList;
	}

	public void setTeamMemberProjectsList(
		List<TeamMemberProjects> teamMemberProjectsList) {
	    this.teamMemberProjectsList = teamMemberProjectsList;
	}

	public Project getSelectedProject() {
	    return selectedProject;
	}

	public void setSelectedProject(Project selectedProject) {
	    this.selectedProject = selectedProject;
	}
	
	public Long getSelectedMemberId() {
	    return selectedMemberId;
	}

	public void setSelectedMemberId(Long selectedMemberId) {
	    this.selectedMemberId = selectedMemberId;
	}
	
	public void resetSelectedMemberIdAndProjects(Long selectedMemberId) 
	{
	    this.newRole = PRODUCT_OWNER; //reset new role to show dialog to assign project
	    setSelectedMemberId(selectedMemberId);
	    initializeDataForSelectedProjectIdsOfPO();
	}
	
	public String getNewRole() {
	    return newRole;
	}

	public void setNewRole(String newRole) {
	    this.newRole = newRole;
	}

	private void excludeHiddenTeamsForSelection()
	{
	    listTeamsForSelection = new ArrayList<Team>();
	    if (teamList != null && teamList.size() > 0)
	    {
		for (Team team : teamList) 
		{
		    if (team.getValidTo() != null) //team is hidden. 
		    {
			if (teamSelected != null && teamSelected.getTeamId().longValue() == team.getTeamId().longValue()){
			    	teamSelected = null;
			    	this.team = null;
			}
		    } else {
			//validTo = null => team is not hidden.
			listTeamsForSelection.add(team);
			if (teamSelected == null){
			    teamSelected = team;
			    this.team = team;
			}
		    }
		}
	    }
	}
}
