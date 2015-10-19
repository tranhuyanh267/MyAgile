package com.ant.myagile.managedbean;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.el.ELContext;
import javax.el.ExpressionFactory;
import javax.el.ValueExpression;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.validator.ValidatorException;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.omnifaces.model.tree.TreeModel;
import org.primefaces.component.menuitem.MenuItem;
import org.primefaces.component.submenu.Submenu;
import org.primefaces.context.RequestContext;
import org.primefaces.model.DefaultMenuModel;
import org.primefaces.model.MenuModel;
import org.primefaces.push.PushContext;
import org.primefaces.push.PushContextFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.ant.myagile.dao.HistoryFieldChangeDao;
import com.ant.myagile.model.Attachment;
import com.ant.myagile.model.History;
import com.ant.myagile.model.Issue;
import com.ant.myagile.model.Member;
import com.ant.myagile.model.PointRemain;
import com.ant.myagile.model.Project;
import com.ant.myagile.model.Sprint;
import com.ant.myagile.model.Status;
import com.ant.myagile.model.Swimline;
import com.ant.myagile.model.Team;
import com.ant.myagile.model.UserStory;
import com.ant.myagile.model.UserStory.StatusType;
import com.ant.myagile.service.AttachmentService;
import com.ant.myagile.service.HistoryService;
import com.ant.myagile.service.IssueService;
import com.ant.myagile.service.KanbanIssueService;
import com.ant.myagile.service.MemberService;
import com.ant.myagile.service.PointRemainService;
import com.ant.myagile.service.ProjectService;
import com.ant.myagile.service.SprintService;
import com.ant.myagile.service.StatusService;
import com.ant.myagile.service.SwimlineService;
import com.ant.myagile.service.TeamService;
import com.ant.myagile.service.UserStoryService;
import com.ant.myagile.utils.CustomELFuntions;
import com.ant.myagile.utils.JSFUtils;
import com.ant.myagile.utils.SetPropertyActionListener;
import com.ant.myagile.utils.Utils;
import com.google.gson.JsonObject;

@Component("dashboardBean")
@Scope("session")
public class DashboardBean 
{
	//private static final String[] colors = { "blue", "night", "green", "wisteria", "asbestos", "carrot", "alizarin", "emerald", "cloud", "amethyst" };
	private static final String[] colors = { "blue", "BlueViolet ", "Brown", "BurlyWood", "CadetBlue", "Chartreuse", "Chocolate", "Coral", "CornflowerBlue", "Cornsilk", "Crimson", "Cyan", "DarkBlue", "DarkCyan", "DarkGoldenRod", "DarkGray", "DarkGreen", "DarkKhaki" , "DarkMagenta", "DarkOliveGreen", "DarkOrange", "DarkOrchid", "DarkRed", "DarkSalmon", "DarkSeaGreen", "DarkSlateBlue", "DarkSlateGray", "DarkViolet", "DeepPink", "DeepSkyBlue", "DimGray", "GoldenRod" };
	private Map<String, String> projectColorMap;
	private Map<String, Map<String, String>> issueProjectMap;
	private boolean multiProject = false;

	private Member loggedMember;
	private Issue issue;
	private Issue issueDeleted;
	private Sprint sprint;
	private List<Sprint> sprintList;
	private List<Status> statusList;
	private List<Status> statusTempList;
	private List<Issue> issueList;
	private List<Team> teamList;
	//for attachment files
	private Attachment attachment;
	private List<Attachment> notAddedAttachmentList;
	private Attachment deleteAttachment;
	
	private long projectId = 0;
	private UserStory userStory;
	private List<Project> projectList;
	private String estimate;
	private boolean addSwimLine;

	@Autowired
	private TeamService teamService;
	@Autowired
	private SprintService sprintService;
	@Autowired
	private IssueService issueService;
	@Autowired
	private StatusService statusService;
	@Autowired
	private UserStoryService userStoryService;
	@Autowired
	private Utils utils;
	@Autowired
	private MemberService memberService;
	@Autowired
	private PointRemainService pointRemainService;
	@Autowired
	private ProjectService projectService;
	@Autowired
	private HistoryService historyService;
	@Autowired
	private HistoryFieldChangeDao historyFieldChangeDao;
	@Autowired
	private SwimlineService swimlineService;
	@Autowired
	private KanbanIssueService kanbanIssueService;
	@Autowired
	AttachmentService attachmentService;
	

	private boolean firstTimeLoad = true;
	private String pointRemainFormat;
	private String pointEstimateFormat;
	private boolean scrumTeamMember;

	// Properties for relationship dialog
	private Issue selectedForShowingRelationship;
	private TreeModel<Issue> issueTree;
	private static final Logger LOGGER = Logger.getLogger(DashboardBean.class);

	
	@PostConstruct
	public void init(){
		projectList = new ArrayList<Project>();
		userStory = new UserStory();
		userStory.setStatus(StatusType.TODO);
		notAddedAttachmentList = new ArrayList<Attachment>();
	}
	
	public void initPreview() {
		if (JSFUtils.isPostbackRequired()) {
			prepareData();
		}
	}

	void prepareData() 
	{
		setTeamList(teamService.findTeamsByMemberAndOwner(utils.getLoggedInMember()));
		
		if ((teamList != null) && (teamList.size() > 0)) 
		{
			if ((sprintList == null) || (sprintList.size() <= 0)) {
				Team firstTeam = teamList.get(0);
				changeScrumTeamMemberByTeam(firstTeam);
				setSprintList(sprintService.findSprintsByTeamId(firstTeam.getTeamId()));
			}
			if ((sprintList != null) && (sprintList.size() > 0)) {
				if (firstTimeLoad || (sprint == null) || (sprint.getSprintId() == null)) {
					setSprint(sprintList.get(0));
					firstTimeLoad = false;
				}
			}
			if ((sprint != null) && (sprint.getSprintId() != null)) {
				setSprint(sprintService.findSprintById(sprint.getSprintId()));
				setSprintList(sprintService.findSprintsByTeamId(sprint.getTeam().getTeamId()));
				setStatusList(statusService.findStatusBySprintId(sprint.getSprintId()));
				setStatusTempList(new ArrayList<Status>(statusList));
				setIssueList(issueService.findIssuesIsTaskBySprintId(sprint.getSprintId()));
				initProjectColor();
				filterIssueAndProject();
			} else {
				sprint = new Sprint();
				sprint.setTeam(new Team());
				resetValue();
			}
			this.projectList = this.projectService.findByMemberAndOwner(this.utils.getLoggedInMember().getMemberId());
		} else {
			sprint = new Sprint();
			sprint.setTeam(new Team());
			resetValue();
		}
		
		if (this.projectList != null && !this.projectList.isEmpty()) {
			if (this.projectId == 0) {
				this.projectId = this.projectList.get(0).getProjectId();
			}
		}
		//set status for swim line
		if(this.sprint.getSprintName() != null){
			Swimline statusSwimline = swimlineService.getSwimlineBySprint(this.sprint.getSprintId());
			if(statusSwimline != null){
				this.addSwimLine = true;
			}else{
				this.addSwimLine = false;
			}
		}else{
			this.addSwimLine = false;
		}
		
		
		this.userStory.setProject(this.projectService.findProjectById(this.projectId));
	}
	
	public void changeScrumTeamMemberByTeam(Team team)
	{
		if(team != null){
			this.scrumTeamMember = CustomELFuntions.isUserInAnyRoles("DEVELOPER,SCRUM_MASTER,PRODUCT_OWNER", team.getTeamId());
		}
	}
	
	
	
	
	/**	
	 * use validate when people input name user story
	 *
	 */
	public void validateUserStory(FacesContext context, UIComponent validate, Object value) throws ValidatorException {
		if (this.userStoryService.checkExistUserStory(Utils.standardizeString(value.toString()), getProjectId()) != null) {
			@SuppressWarnings("static-access")
			FacesMessage msg = new FacesMessage(this.utils.getMessage("myagile.backlog.Exists", null));
			throw new ValidatorException(msg);
		}
	}
	
	/**
	 * Save current user story that can be created or updated
	 */
	public void saveUserStory() 
	{
		boolean showInform = false;
		if (this.userStoryService.create(this.userStory)) 
		{
		    //save attachment file
		    saveAttachmentsToDatabase();
		    //add attachment files of User Story into project folder.
		    addAttachmentForCurrentUserStory(); 
			//add into issue
			String strEstimatePoint = this.estimate.trim();
			Issue issue = new Issue();
			issue.setUserStory(this.userStory);
			issue.setPointFormat("1");
			strEstimatePoint = this.issueService.checkingEstimatePoint(strEstimatePoint);
			issue.setEstimate(strEstimatePoint);
			issue.setRemain(strEstimatePoint);
			
			//Remove Control Characters
			issue.setDescription(this.userStory.getDescription().replaceAll("\\p{Cntrl}", ""));
			issue.setNote(this.userStory.getNote().replaceAll("\\p{Cntrl}", ""));
			issue.setSubject(this.userStory.getName().replaceAll("\\p{Cntrl}", ""));
			
			issue.setPriority(String.valueOf(this.userStory.getPriority()));
			issue.setType("Task");
			issue.setSprint(this.sprint);
			Status status = statusService.findStatusStartBySprintId(this.sprint.getSprintId());
			issue.setStatus(status);
			issue.setIsSwimLine(false);
			long idIssueAfterSave = this.issueService.saveIssueAndGetId(issue);
			if (idIssueAfterSave != 0) {
				//save point remain
				issue.setIssueId(idIssueAfterSave);
				PointRemain pmSaveIssueImmediately = new PointRemain();
				pmSaveIssueImmediately.setDateUpdate(new Date());
				pmSaveIssueImmediately.setIssue(issue);
				pmSaveIssueImmediately.setPointRemain(strEstimatePoint);
				
				//Remove Control Characters
				this.userStory.setDescription(this.userStory.getDescription().replaceAll("\\p{Cntrl}", ""));
				this.userStory.setName(this.userStory.getName().replaceAll("\\p{Cntrl}", ""));
				this.userStory.setNote(this.userStory.getNote().replaceAll("\\p{Cntrl}", ""));
				
				if (this.pointRemainService.save(pmSaveIssueImmediately)) {
					this.userStory.setStatus(UserStory.StatusType.IN_PROGRESS);
					
					//update status of user story
					if (this.userStoryService.update(this.userStory)) {
						History hisAfterCreateIssue = new History();
						hisAfterCreateIssue.setAuthor(this.utils.getLoggedInMember());
						hisAfterCreateIssue.setCreatedOn(new Date());
						hisAfterCreateIssue.setActionType(History.CREATE_ACTION);
						hisAfterCreateIssue.setContainerType(History.ISSUE_HISTORY);
						hisAfterCreateIssue.setContainerId(idIssueAfterSave);
						
						if (historyService.save(hisAfterCreateIssue)) {
							//reset form
							this.userStory.setDescription("");
							this.userStory.setNote("");
							this.userStory.setName("");
							this.userStory.setValue(0);
							this.userStory.setRisk(0);
							this.estimate = "";
							handleChangeSprint();
							showInform = true;
						}
					}
				}
			}
		}
		
		if (showInform) {
			FacesContext.getCurrentInstance().addMessage(
					"msg-save-inform", new FacesMessage(FacesMessage.SEVERITY_INFO, "Successful", "Successful"));
		} else {
			FacesContext.getCurrentInstance().addMessage(
					"msg-save-inform", new FacesMessage(FacesMessage.SEVERITY_WARN, "Unsuccessful", "Unsuccessful"));
		}
	}
	
	public void handleChangeProject() {
		this.userStory.setProject(this.projectService.findProjectById(this.projectId));
	}

	/**
	 * Find project with each issue in kanban
	 */
	public void filterIssueAndProject() {
		issueProjectMap = new HashMap<String, Map<String, String>>();
		Map<String, String> projectMap = new HashMap<String, String>();
		Project projectTemp = new Project();
		long checkMultiProject = 0;
		multiProject = false;
		for (Issue issueTemp : issueList) {
			projectTemp = projectService.findProjectOfIssue(issueTemp);
			projectMap.put("projectId", projectTemp.getProjectId().toString());
			projectMap.put("projectName", projectTemp.getProjectName());
			projectMap.put("projectLogo", projectTemp.getImagePath());
			projectMap.put("projectColor", projectColorMap.get(projectTemp.getProjectId().toString()));
			issueProjectMap.put(issueTemp.getIssueId().toString(), new HashMap<String, String>(projectMap));
			if (checkMultiProject == 0) {
				checkMultiProject = projectTemp.getProjectId();
			} else {
				if (checkMultiProject != projectTemp.getProjectId()) {
					multiProject = true;
				}
			}
		}
	}
	
	/**
	 * set default color for each project is assigned to team
	 */
	public void initProjectColor() {
		List<Project> projectList = projectService.findAllProjectsByTeam(sprint.getTeam());
		projectColorMap = new HashMap<String, String>();
		int i = 0;
		for (Project projectTemp : projectList) {
			projectColorMap.put(projectTemp.getProjectId().toString(), colors[i++]);
			if(i >= colors.length){
				i = 0;
			}
		}
	}

	public void resetValue() {
		setSprintList(new ArrayList<Sprint>());
		setStatusList(new ArrayList<Status>());
		setStatusTempList(new ArrayList<Status>());
		setIssueList(new ArrayList<Issue>());
	}

	public void refreshStatusTempList() {
		setStatusTempList(new ArrayList<Status>(statusList));
	}

	public void refeshIssueList() {
		setIssueList(issueService.findIssuesIsTaskBySprintId(sprint.getSprintId()));
		filterIssueAndProject();
	}

	public void refreshStatusList() {
		setStatusList(statusService.findStatusBySprintId(sprint.getSprintId()));
		refeshIssueList();
	}
	
	

	public void handleChangeTeam() {
		Team teamFromSprint = sprint.getTeam();
		setSprintList(sprintService.findSprintsByTeamId(teamFromSprint.getTeamId()));
		changeScrumTeamMemberByTeam(teamFromSprint);
		if (!sprintList.isEmpty() && (sprintList.size() > 0)) {
			setSprint(sprintList.get(0));
			setStatusList(statusService.findStatusBySprintId(sprint.getSprintId()));
			setIssueList(issueService.findIssuesIsTaskBySprintId(sprint.getSprintId()));
			setStatusTempList(new ArrayList<Status>(statusList));
			initProjectColor();
			filterIssueAndProject();
		} else {
			resetValue();
		}
		
		//setstatus for swimline
		if(this.sprint != null){
			Swimline statusSwimline = swimlineService.getSwimlineBySprint(this.sprint.getSprintId());
			if(statusSwimline != null){
				this.addSwimLine = true;
			}else{
				this.addSwimLine = false;
			}
		}else{
			this.addSwimLine = false;
		}
	}

	public void handleChangeSprint() {
		setSprint(sprintService.findSprintById(sprint.getSprintId()));
		setSprintList(sprintService.findSprintsByTeamId(sprint.getTeam().getTeamId()));
		setStatusList(statusService.findStatusBySprintId(sprint.getSprintId()));
		setIssueList(issueService.findIssuesIsTaskBySprintId(sprint.getSprintId()));
		setStatusTempList(new ArrayList<Status>(statusList));
		initProjectColor();
		filterIssueAndProject();
		
		//setstatus for swimline
		if(this.sprint != null){
			Swimline statusSwimline = swimlineService.getSwimlineBySprint(this.sprint.getSprintId());
			if(statusSwimline != null){
				this.addSwimLine = true;
			}else{
				this.addSwimLine = false;
			}
		}else{
			this.addSwimLine = false;
		}
	}
	
	public List<Issue> getIssueOrderByStatus(long statusId){
		return issueService.getIssueOrderByStatus(statusId);
	}
	
	public List<Issue> getIssueOrderByStatusAndSwimline(long statusId){
		return issueService.getIssueOrderByStatusAndSwimline(statusId);
	}
	
	public List<Issue> getIssueOrderByStatusAndNotInSwimline(long statusId){
		return issueService.getIssueOrderByStatusAndNotInSwimline(statusId);
	}

	/**
	 * Get issue by status
	 * 
	 * @param status
	 *            - long number format
	 * @return List of Issue
	 */
	public List<Issue> findIssueByStatus(long status) {
		List<Issue> isList = new ArrayList<Issue>();
		try {
			for (Issue is : issueList) {
				if ((is.getStatus() != null) && (is.getStatus().getStatusId() == status)) {
					isList.add(is);
				}
			}
		} catch (Exception e) {
		}
		return isList;
	}

	/**
	 * Add a new status on kanban for sprint
	 */
	public void addMoreField() {
		Status st = new Status();
		st.setColor("368EE0");
		st.setSprint(sprintService.findSprintById(sprint.getSprintId()));
		getStatusTempList().add(getStatusTempList().size() - 2, st);
	}

	public void removeStatus() {
		String statusId = JSFUtils.getRequestParameter("statusId");
		try {
			Long stId = Long.parseLong(statusId);
			Iterator<Status> statusListVal = statusTempList.iterator();
			while (statusListVal.hasNext()) {
				Status st = statusListVal.next();
				if (stId.equals(st.getStatusId())) {
					statusListVal.remove();
				}
			}
		} catch (HibernateException e) {
		}
	}

	public void handleSaveSettingForm() {
		try {
			for (Status st : statusList) {
				if (!statusTempList.contains(st)) {
					statusService.updateIssueBySprintId(sprint.getSprintId(), st.getStatusId());
					statusService.delete(st);
				}
			}
			for (Status st : statusTempList) {
				if (st.getStatusId() == null) {
					st.setType(Status.StatusType.IN_PROGRESS);
					statusService.save(st);
				} else {
					statusService.update(st);
				}
			}
			//check swimline exist in sprint
			Swimline statusSwimline =  swimlineService.getSwimlineBySprint(this.sprint.getSprintId());
			if(this.addSwimLine){
				if(statusSwimline != null){
					//update status
					statusSwimline.setName("Swimline");
					swimlineService.update(statusSwimline);
				}else{
					//add status swimline
					Swimline addStatusSwimline = new Swimline();
					addStatusSwimline.setName("Swimline");
					addStatusSwimline.setSprint(this.sprint);
					swimlineService.save(addStatusSwimline);
				}
			}else{
				//remove sprint
				if(statusSwimline != null){
					swimlineService.delete(statusSwimline);
				}
			}
			
			setStatusList(statusService.findStatusBySprintId(sprint.getSprintId()));
			setStatusTempList(new ArrayList<Status>(statusList));
			setIssueList(issueService.findIssuesIsTaskBySprintId(sprint.getSprintId()));
			RequestContext.getCurrentInstance().addCallbackParam("save", true);
			pushDataKanban(-1L, "true", "");
		} catch (Exception e) {
			RequestContext.getCurrentInstance().addCallbackParam("save", false);
		}
		
	}

	public boolean getShowAcceptedStatus() {
		if (statusList.size() >= 3) {
			Status acceptedStatus = statusList.get(statusList.size() - 1);
			if ((acceptedStatus != null) && (acceptedStatus.getType() == Status.StatusType.ACCEPTED_SHOW)) {
				return true;
			}
		}
		return false;
	}

	public void setShowAcceptedStatus(boolean isShow) {
		if (statusList.size() > 0) {
			Status acceptedStatus = statusList.get(statusList.size() - 1);
			if (isShow) {
				acceptedStatus.setType(Status.StatusType.ACCEPTED_SHOW);
			} else {
				acceptedStatus.setType(Status.StatusType.ACCEPTED_HIDE);
			}
		}
	}

	/**
	 * Update status of an issue when drag and drop to another status
	 */
	public void onDrop() {
		Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		Issue issueTemp = issueService.findIssueById(Long.parseLong(params.get("issueId")));
		long idStatusFromClient = Long.parseLong(params.get("statusId"));
 		if(sprintService.isPastSprint(issueTemp.getSprint()) && !issueService.canChangeToAccepted(issueTemp, idStatusFromClient) ){
			RequestContext.getCurrentInstance().execute("informCanNotMoveTask.show()");
			String senderId = params.get("senderId");
			String receiverId = params.get("receiverId");
			RequestContext.getCurrentInstance().update(senderId);
			RequestContext.getCurrentInstance().update(receiverId);
			RequestContext.getCurrentInstance().execute("sortable()");
		}else{
			try {
				issueService.updateStatusForIssue(Long.parseLong(params.get("statusId")), Long.parseLong(params.get("issueId")));
				//get array issue order
				String[] arryIdIssueOrder = params.get("orderIssue").split(";");
				//update issue on swim
				Issue issueSwim = issueService.findIssueById(Long.parseLong(params.get("issueId")));
				issueSwim.setIsSwimLine(Boolean.valueOf(params.get("isSwimLine")));
				issueService.updateIssue(issueSwim);
				if (issueTemp.getParent() != null) {
					issueService.updateStatusOfIssueParent(issueTemp.getParent());
				}
				//update issue order
				//long idStatusFromClient = Long.parseLong(params.get("statusId"));
				boolean swimline = Boolean.valueOf(params.get("isSwimLine"));
				if(issueService.updateOrderIssue(arryIdIssueOrder,idStatusFromClient,swimline)){
					//update order successful
				}
				if(!sprintService.isPastSprint(sprint)) {
					// Update status userstory
					UserStory userStory = userStoryService.findUserStoryByIssue(issueTemp);
					UserStory.StatusType status = userStoryService.findStatusOfUserStoryBySprintAndUserStory(userStory,this.sprint);
					if(userStory.getStatus().compareTo(status) != 0) {
						userStory.setStatus(status);
						userStoryService.update(userStory);
						//update kanbanissue status belong to userstory
						kanbanIssueService.updateAllKanbanIssueByUserStoryStatusOfTeam(userStory,sprint.getTeam());
					}
				}
			} 
			catch (Exception ex) 
			{
				LOGGER.error("erorr on drop unsuccessful: " + ex);
			}
			setIssueList(issueService.findIssuesIsTaskBySprintId(sprint.getSprintId()));
			pushDataKanban(Long.parseLong(params.get("issueId")), "true", "");
			List<String> updateString = new ArrayList<String>();
			//updateString.add(params.get("senderId"));
			updateString.add(params.get("receiverId"));
			RequestContext.getCurrentInstance().update(updateString);
			RequestContext.getCurrentInstance().execute("sortable()");
		}
		
	}

	public void removeStatusData() {
		try {
			List<Status> statusOld = statusService.findStatusBySprintId(sprint.getSprintId());
			List<Status> statusNew = statusList;
			for (Status st : statusOld) {
				if (!statusNew.contains(st)) {
					statusService.updateIssueBySprintId(sprint.getSprintId(), st.getStatusId());
					statusService.delete(st);
				}
			}
			statusList = statusService.findStatusBySprintId(sprint.getSprintId());
		} catch (Exception e) {
		}
	}

	/**
	 * Assign member to do a task
	 */
	public void assignTaskToMember() {
		String memberId = JSFUtils.getRequestParameter("memberId");
		String issueId = JSFUtils.getRequestParameter("issueId");
		String panelId = JSFUtils.getRequestParameter("columnId");
		Member member = null;
		Issue issueTemp = issueService.findIssueById(Long.valueOf(issueId));
		if (!memberId.equalsIgnoreCase("-1")) {
			member = memberService.findMemberById(Long.valueOf(memberId));
		}
		if (issueTemp != null) {
			issueService.assignMemberToIssue(issueTemp, member);
			for (int i = 0; i < issueList.size(); i++) {
				if (issueList.get(i).getIssueId().equals(issueTemp.getIssueId())) {
					issueList.get(i).setAssigned(member);
					setIssueList(issueService.findIssuesIsTaskBySprintId(sprint.getSprintId()));
				}
			}
			refeshIssueList();
			JSFUtils.resetForm("kanban");
			pushDataKanban(issueTemp.getIssueId(), "false", "");
			RequestContext.getCurrentInstance().update(panelId);
			RequestContext.getCurrentInstance().execute("sortable()");
			RequestContext.getCurrentInstance().execute("showTaskStickerDetails(sticker_" + issueTemp.getIssueId() + ")");
			RequestContext.getCurrentInstance().execute("removeMoreOptionButtons()");
		}
	}

	/**
	 * Get developer of a team, except others
	 * 
	 * @return
	 */
	public List<Member> findDevelopmentMembersByTeamId() {
		List<Member> memberList = memberService.findDevelopmentMembersByTeamId(sprint.getTeam().getTeamId());
		return memberList;
	}

	/**
	 * Get developer of a team, except 1 developer with ID in param
	 * 
	 * @param excludedMemberId
	 *            - String type
	 * @return List of Member
	 */
	public List<Member> findDevelopmentMembersByTeamIdWithExclude(String excludedMemberId) {
		List<Member> memberList = memberService.findDevelopmentMembersByTeamId(sprint.getTeam().getTeamId(), Long.valueOf(excludedMemberId));
		return memberList;
	}

	/**
	 * Calculate how many percent issue done
	 */
	public void calculateProgress() {
		String issueID = JSFUtils.getRequestParameter("issueId");
		String id = JSFUtils.getRequestParameter("id");
		String start = JSFUtils.getRequestParameter("start");
		Issue is = issueService.findIssueById(Long.parseLong(issueID));
		RequestContext requestContext = RequestContext.getCurrentInstance();
		requestContext.addCallbackParam("progressValue", issueService.calculateProgress(is));
		requestContext.addCallbackParam("id", id);
		requestContext.addCallbackParam("start", Boolean.valueOf(start));
	}

	/**
	 * Calculate how many percent issue done at first time load
	 * 
	 * @param issue
	 *            - instance of Issue Object
	 * @return int - percent done of parent issue
	 */
	public int calculateProgressInit(Issue issue) {
		return issueService.calculateProgress(issue);
	}

	/**
	 * Calculate how many percent parent issue done
	 * 
	 * @param issue
	 *            - instance of Issue Object
	 * @return float - percent done of parent issue
	 */
	public float calculateProgressForParentIssueTask(Issue issue) {
		float percent = issueService.calculateProgressForParentIssueTask(issue);
		return percent;
	}

	public void pushDataKanban(Long issueId, String updateAll, String remain) {
		PushContext pushContext = PushContextFactory.getDefault().getPushContext();
		JsonObject json = new JsonObject();
		json.addProperty("user", utils.getLoggedInMember().getMemberId());
		json.addProperty("sprintId", sprint.getSprintId());
		json.addProperty("issueId", issueId);
		json.addProperty("updateAll", updateAll);
		json.addProperty("remain", remain);
		pushContext.push("/issueChange", json.toString());
	}

	public void updatePointRemain(long issueId) {
		try {
			pointRemainService.updatePointRemain(pointRemainFormat, issueId);
			pushDataKanban(issueId, "false", pointRemainService.getPointRemainByFormat(issueService.findIssueById(issueId)));
		} catch (Exception e) {
			LOGGER.debug(e);
		}
	}

	/**
	 * Update estimate point for an issue
	 * 
	 * @param issueId
	 *            - Issue Id, Long number format
	 */
	public void updateEstimatePoint(Long issueId) {
		try {
			Issue issueTemp = issueService.findIssueById(issueId);
			issueService.updateEstimatePoint(issueTemp, pointEstimateFormat.trim());
			issueService.updatePointRemain(pointEstimateFormat.trim(), issueId);
			JSFUtils.addCallBackParam("update", true);
			pushDataKanban(issueId, "false", "");
		} catch (Exception e) {
			JSFUtils.addCallBackParam("update", false);
		}
	}

	/**
	 * Check if point remain right format
	 * 
	 * @param context
	 *            - FacesContext
	 * @param validate
	 *            - UIComponent
	 * @param value
	 *            - Object
	 */
	public void checkPointRemain(FacesContext context, UIComponent validate, Object value) {
		pointRemainService.checkPointRemain(context, validate, value, "");
	}

	public String getPointRemainFormat() {
		try {
			Long issueId = (Long) JSFUtils.resolveExpression("#{issue.issueId}");
			Issue issueTemp = issueService.findIssueById(issueId);
			pointRemainFormat = pointRemainService.getPointRemainByFormat(issueTemp);
			return pointRemainFormat;
		} catch (Exception e) {
			return null;
		}

	}

	public void checkEstimatePoint(FacesContext context, UIComponent validate, Object value) {
		pointRemainService.checkEstimatePoint(context, validate, value);
	}

	public List<PointRemain> findPointRemainByIssueId(long issueId) {
		try {
			Issue is = issueService.findIssueById(issueId);
			List<PointRemain> pointRemains = pointRemainService.findPointRemainByIssueId(issueId);

			if (is.getPointFormat().equals("2")) {
				for (int i = 0; i < pointRemains.size(); i++) {
					float pointDev = Float.parseFloat(Utils.getPointDev(pointRemains.get(i).getPointRemain()));
					String tt = String.format("%s", pointDev).replace(".0", "");
					PointRemain r = pointRemains.get(i);
					r.setPointRemain(tt);
					pointRemains.set(i, r);
				}
			}
			return pointRemains;
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Find all history about changing point remain of an issue
	 * 
	 * @param issueId
	 *            - Issue ID, Long number format
	 * @return List of String
	 */
	public List<String> findHistoryOfPointRemainByIssueId(long issueId) {
		try {
			List<String> issueHistoryList = pointRemainService.findHistoryOfPointRemainByIssueId(issueId);
			return issueHistoryList;
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Create context menu when right click on task in kanban with 2 options,
	 * 'Delete' and 'Move to'
	 * 
	 * @return
	 */
	public MenuModel getContextMenu() {
		try {
			FacesContext facesCtx = FacesContext.getCurrentInstance();
			ELContext elCtx = facesCtx.getELContext();
			ExpressionFactory expFact = facesCtx.getApplication().getExpressionFactory();
			Issue is = new Issue();
			is = (Issue) JSFUtils.resolveExpression("#{issue}");
			MenuModel model = new DefaultMenuModel();
			MenuItem item = new MenuItem();
			item.setGlobal(false);
			item.setValue("Delete");
			item.setIcon("icon-remove");
			item.setOnclick("changeIdPlaceholder(" + is.getDisplayIssueId() + ");deleteDlg.show()");
			ValueExpression target = expFact.createValueExpression(elCtx, "#{dashboardBean.issueDeleted}", Issue.class);
			ValueExpression value = expFact.createValueExpression(elCtx, "#{issue}", Issue.class);
			item.addActionListener(new SetPropertyActionListener(target, value));
			item.setId("delete-issue");
			model.addMenuItem(item);
			Submenu statusMenu;
			statusMenu = new Submenu();
			statusMenu.setLabel("Move to");
			statusMenu.setIcon("icon-arrow-right");
			for (Status st : statusList) {
				item = new MenuItem();
				item.setGlobal(false);
				item.setValue(st.getName());
				item.setId("st_" + st.getStatusId().toString());
				item.setUpdate(":kanban");
				item.setOnclick("updateStatusForIssue("+ is.getDisplayIssueId() + "," + is.getIssueId() + "," + st.getStatusId() + ",'" + issueService.getPointByFormat(is.getRemain(), is.getPointFormat()) + "')");
				statusMenu.getChildren().add(item);
			}

			Submenu sprintOpenMenu;
			sprintOpenMenu = new Submenu();
			sprintOpenMenu.setLabel("Copy to");
			sprintOpenMenu.setIcon("icon-copy");
			List<Sprint> sprintOpenList = sprintService.findSprintsAreOpen(sprint.getTeam().getTeamId());
			if (sprintOpenList != null) {
				for (Sprint sprinttemp : sprintOpenList) {
					if (!sprinttemp.equals(this.sprint)) {
						item = new MenuItem();
						item.setGlobal(false);
						item.setValue(sprinttemp.getSprintName());
						item.setId("sprint_" + sprinttemp.getSprintId().toString());
						item.setUpdate("@none");
						String sprintName = sprinttemp.getSprintName();
						if (!org.apache.commons.lang3.StringUtils.containsIgnoreCase(sprintName, "sprint")) {
							sprintName = "Sprint " + sprintName;
						}
						item.setOnclick("formatMessageCopyIssueToSprintDlg('#" + is.getIssueId() + "','" + sprintName + "'," + sprinttemp.getSprintId() + "); " + "confirmCopyIssueToSprintDlg.show()");
						sprintOpenMenu.getChildren().add(item);
					}
				}
			}
			model.addSubmenu(statusMenu);
			model.addSubmenu(sprintOpenMenu);
			return model;
		} catch (Exception e) {
			LOGGER.error(e);
		}
		return null;
	}

	public void deleteIssue() {
		Issue issueTemp = issueDeleted;
		Issue issueParent = issueTemp.getParent();
		issueService.deleteIssue(issueTemp);
		if (issueParent != null) {
			issueService.updateStatusOfIssueParent(issueParent);
		}
		refeshIssueList();
	}

	/**
	 * Copy an issue to a specific sprint
	 */
	public void copyIssueToSprint() {
		try {
			long issueId = Long.valueOf(JSFUtils.getRequestParameter("issueId"));
			long sprintId = Long.valueOf(JSFUtils.getRequestParameter("sprintId"));
			issueService.copyIssueToSprint(issueId, sprintId);
		} catch (Exception e) {
			LOGGER.error(e);
		}
	}

	public void showTasksRelationship(ActionEvent event) {
		setSelectedForShowingRelationship((Issue) event.getComponent().getAttributes().get("selectedIssueIdForRelationship"));
		Issue rootIssue = issueService.findRootIssueBySubIssue(selectedForShowingRelationship);
		setIssueTree(issueService.buildIssueRelationshipTree(rootIssue));
	}

	public void updateParentPoint() {
		String issueIdString = JSFUtils.getRequestParameter("issueId");
		String newPointString = JSFUtils.getRequestParameter("newPoint");
		Issue issueTemp = issueService.findIssueById(Long.parseLong(issueIdString));
		issueTemp.setEstimate(newPointString);
		issueService.updateIssue(issueTemp);
	}

	public String showUserName(Member member) {
		return Utils.checkName(member);
	}

	public void resetSelectedIssue() {
		selectedForShowingRelationship = null;
	}

	public void setPointRemainFormat(String pointRemainFormat) {
		this.pointRemainFormat = pointRemainFormat;
	}

	public String getPointEstimateFormat() {
		try {
			Long issueId = (Long) JSFUtils.resolveExpression("#{issue.issueId}");
			Issue issueTemp = issueService.findIssueById(issueId);
			pointEstimateFormat = pointRemainService.getPointEstimateByFormat(issueTemp);
			return pointEstimateFormat;
		} catch (Exception e) {
			return null;
		}
	}

	public boolean isPastSprint(){
		return sprintService.isPastSprint(sprint);
	}
	
	public void setPointEstimateFormat(String pointEstimateFormat) {
		this.pointEstimateFormat = pointEstimateFormat;
	}

	public boolean getExistHistoryOfPointRemain() {
		Long issueId = (Long) JSFUtils.resolveExpression("#{issue.issueId}");
		Issue issueTemp = issueService.findIssueById(issueId);
		return issueService.existHistoryOfPointRemain(issueTemp);
	}

	public Map<String, String> getProjectOfIssue() {
		Long issueId = (Long) JSFUtils.resolveExpression("#{issue.issueId}");
		return issueProjectMap.get(issueId.toString());
	}

	public String getProjectColorForIssue() {
		Long issueId = (Long) JSFUtils.resolveExpression("#{issue.issueId}");
		return issueProjectMap.get(issueId.toString()).get("projectColor");

	}

	public Issue getIssue() {
		if (issue == null) {
			issue = new Issue();
		}
		return issue;
	}

	public void setIssue(Issue issue) {
		this.issue = issue;
	}

	public Sprint getSprint() {
		if (sprint == null) {
			sprint = new Sprint();
		}
		return sprint;
	}

	public void setSprint(Sprint sprint) {
		this.sprint = sprint;
	}

	public List<Sprint> getSprintList() {
		if (sprintList == null) {
			sprintList = new ArrayList<Sprint>();
		}
		return sprintList;
	}

	public void setSprintList(List<Sprint> sprintList) {
		this.sprintList = sprintList;
	}

	public List<Status> getStatusList() {
		if (statusList == null) {
			statusList = new ArrayList<Status>();
		}
		return statusList;
	}

	public void setStatusList(List<Status> statusList) {
		this.statusList = statusList;
	}

	public List<Status> getStatusTempList() {
		if (statusTempList == null) {
			statusTempList = new ArrayList<Status>();
		}
		return statusTempList;
	}

	public void setStatusTempList(List<Status> statusTempList) {
		this.statusTempList = statusTempList;
	}

	public void setIssueList(List<Issue> issueList) {
		this.issueList = issueList;
	}

	public List<Issue> getIssueList() {
		if (issueList == null) {
			issueList = new ArrayList<Issue>();
		}
		return issueList;
	}
	

	public List<Team> getTeamList() 
	{
	    	excludeHiddenTeamsForSelection(teamService.findTeamsByMemberAndOwner(utils.getLoggedInMember()));
		
		return this.teamList;
	}
	

	public void setTeamList(List<Team> teamList) 
	{
	    excludeHiddenTeamsForSelection(teamList);
	}

	public Issue getSelectedForShowingRelationship() {
		return selectedForShowingRelationship;
	}

	public void setSelectedForShowingRelationship(Issue selectedForShowingRelationship) {
		this.selectedForShowingRelationship = selectedForShowingRelationship;
	}

	public TreeModel<Issue> getIssueTree() {
		return issueTree;
	}

	public void setIssueTree(TreeModel<Issue> issueTree) {
		this.issueTree = issueTree;
	}

	public Member getLoggedMember() {
		loggedMember = utils.getLoggedInMember();
		return loggedMember;
	}

	public void setLoggedMember(Member loggedMember) {
		this.loggedMember = loggedMember;
	}

	public Issue findIssueById(String id) {
		return issueService.findIssueById(Long.parseLong(id));

	}

	public Map<String, Map<String, String>> getIssueProjectMap() {
		return issueProjectMap;
	}

	public void setIssueProjectMap(Map<String, Map<String, String>> issueProjectMap) {
		this.issueProjectMap = issueProjectMap;
	}

	public boolean isMultiProject() {
		return multiProject;
	}

	public void setMultiProject(boolean multiProject) {
		this.multiProject = multiProject;
	}

	public Issue getIssueDeleted() {
		if (issueDeleted == null) {
			issueDeleted = new Issue();
		}
		return issueDeleted;
	}

	public void setIssueDeleted(Issue issueDeleted) {
		this.issueDeleted = issueDeleted;
	}
	
	public long getProjectId() {
		return projectId;
	}

	public void setProjectId(long projectId) {
		this.projectId = projectId;
	}

	public UserStory getUserStory() {
		return userStory;
	}

	public void setUserStory(UserStory userStory) {
		this.userStory = userStory;
	}

	public List<Project> getProjectList() {
		return projectList;
	}

	public void setProjectList(List<Project> projectList) {
		this.projectList = projectList;
	}

	public String getEstimate() {
		return estimate;
	}

	public void setEstimate(String estimate) {
		this.estimate = estimate;
	}

	public boolean isAddSwimLine() {
		return addSwimLine;
	}

	public void setAddSwimLine(boolean addSwimLine) {
		this.addSwimLine = addSwimLine;
	}

	public boolean isScrumTeamMember() {
		return scrumTeamMember;
	}

	public void setScrumTeamMember(boolean scrumTeamMember) {
		this.scrumTeamMember = scrumTeamMember;
	}
	
	public Attachment getAttachment() {
		return attachment;
	}

	public void setAttachment(Attachment attachment) {
		this.attachment = attachment;
	}

	public List<Attachment> getNotAddedAttachmentList() {
		return notAddedAttachmentList;
	}

	public void setNotAddedAttachmentList(List<Attachment> notAddedAttachmentList) {
		this.notAddedAttachmentList = notAddedAttachmentList;
	}
	
	public void resetNotAddedAttachmentList() 
	{
		this.notAddedAttachmentList = new ArrayList<Attachment>();
	}

	public Attachment getDeleteAttachment() {
		return deleteAttachment;
	}

	public void setDeleteAttachment(Attachment deleteAttachment) {
		this.deleteAttachment = deleteAttachment;
	}

	public void uploadAttachment() {
		if (addAttachment()) {
				this.notAddedAttachmentList.add(this.attachment);
			}
	}
	
	private boolean addAttachment() 
	{
	    	try
		{
        		this.attachment = new Attachment();
        			
        		String filename = JSFUtils.getRequestParameter("filename");
        		
        		String diskFileName = this.attachmentService.fileNameProcess(FilenameUtils.removeExtension(filename));
        		diskFileName = this.attachmentService.replaceFile(filename, diskFileName);
        		this.attachment.setFilename(filename);
        		this.attachment.setDiskFilename(diskFileName);
        		this.attachment.setContainerType(Attachment.USERSTORY_ATTACHMENT);
        		this.attachment.setTemp(true);
        		this.attachment.setCreatedOn(new Date());
        		this.attachment.setAuthor(this.utils.getLoggedInMember());
        		 
        		return true;
		}catch(Exception exception){
			LOGGER.error("addAttachment at KanbanBacklog error: " + exception.getMessage());
			return false;
		}
	}
	
	public void deleteNotAddedAttachment() {
		try {
			this.attachmentService.deleteFileInTemp(deleteAttachment.getDiskFilename());
			this.notAddedAttachmentList.remove(deleteAttachment);
			
		} catch (Exception e) {
		    LOGGER.error("deleteAttachment at KanbanBacklog " + e);
		}
	}
	
	
	private void saveAttachmentsToDatabase() 
	{
		try{
			for(Attachment attachment: this.notAddedAttachmentList){
				attachment.setContainerId(this.userStory.getUserStoryId());
				attachment.setTemp(false);
				this.attachmentService.save(attachment);
			}
		}catch(Exception e){
		    LOGGER.error("saveAttachmentsToDatabase: " + e );
		}
	}
	
	
	/**
	 * add attachments for user story with "create" mode
	 */
	private void addAttachmentForCurrentUserStory() 
	{
		if (this.notAddedAttachmentList.size() > 0) 
		{
			for (Attachment att : this.notAddedAttachmentList) 
			{
				att.setContainerId(this.userStory.getUserStoryId());
				att.setTemp(false);
				this.attachmentService.update(att);
				this.attachmentService.moveAttachmentFile(att, this.userStory.getProject().getProjectId());
			}
			this.notAddedAttachmentList.clear();
		}
	}
	
	private void excludeHiddenTeamsForSelection(List<Team> allTeams)
	{
	    this.teamList = new ArrayList<Team>();
	    
	    if (allTeams != null && allTeams.size() > 0)
	    {
		for (Team team : allTeams) 
		{
		    if (team != null && team.getValidTo() == null) //validTo = null => team is not hidden. 
		    {
			this.teamList.add(team);
		    }
		}
	    }
	}
}