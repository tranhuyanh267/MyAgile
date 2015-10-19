package com.ant.myagile.managedbean;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.TreeMap;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;

import org.apache.log4j.Logger;
import org.primefaces.component.dashboard.Dashboard;
import org.primefaces.context.RequestContext;
import org.primefaces.json.JSONObject;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.ant.myagile.model.Attachment;
import com.ant.myagile.model.Issue;
import com.ant.myagile.model.Project;
import com.ant.myagile.model.Sprint;
import com.ant.myagile.model.Team;
import com.ant.myagile.model.UserStory;
import com.ant.myagile.service.AttachmentService;
import com.ant.myagile.service.IssueService;
import com.ant.myagile.service.KanbanIssueService;
import com.ant.myagile.service.KanbanStatusService;
import com.ant.myagile.service.KanbanSwimlineService;
import com.ant.myagile.service.ProjectService;
import com.ant.myagile.service.SprintService;
import com.ant.myagile.service.StatusService;
import com.ant.myagile.service.TeamService;
import com.ant.myagile.service.UserStoryService;
import com.ant.myagile.utils.CustomELFuntions;
import com.ant.myagile.utils.JSFUtils;
import com.ant.myagile.utils.Utils;

@Component("sprintBacklogBean")
@Scope("session")
public class SprintBacklogBean implements Serializable {
	private static final long serialVersionUID = 1L;
	private static final int INDEX_ID_PANEL = 6;
	private static final Logger logger = Logger.getLogger(SprintBacklogBean.class);
	@Autowired
	private TeamService teamService;
	@Autowired
	private UserStoryService usService;
	@Autowired
	private Utils utils;
	@Autowired
	private IssueService issueService;
	@Autowired
	private SprintService sprintService;
	@Autowired
	private StatusService statusService;
	@Autowired
	private ProjectService projectService;
	@Autowired
	private UserStoryService userStoryService;
	@Autowired
	private KanbanStatusService kanbanStatusService;
	@Autowired
	private KanbanSwimlineService kanbanSwimlineService;
	@Autowired
	AttachmentService attachmentService;
	@Autowired
	private StatisticBean statisticBean;
	@Autowired
	private RemainingTasksBeanOld remainingTasksBean;
	@Autowired
	private DashboardBean dashboardBean;
	@Autowired
	private KanbanIssueService kanbanIssueService;
	
	private static TreeMap<String, Integer> sprintPosition = new TreeMap<String, Integer>();
	private Dashboard dashboard;
	private List<UserStory> listUs;
	private long projectId = 0;
	private List<Project> projects;
	private long sprintId = 0;
	private List<Sprint> sprints;
	private long teamId = 0;
	private List<Team> teams;
	private List<String> selectedFilter = Arrays.asList("TODO", "IN_PROGRESS");
	private boolean flagFirst = true;
	private boolean flagChange = true;
	private JSONObject jsonObject = new JSONObject();
	private List<UserStory> productBacklog;
	private String searchedKeyWord = "";
	private List<Issue> sprintBacklogIssue;
	private Hashtable<Long, List<Object>> sprintBacklogInfo;
	private static final Logger LOGGER = Logger.getLogger(SprintBacklogBean.class);
	public String sprintStatus = "";
	private Boolean showFormAddBL = false;
	private UserStory lastUserStory;
	
	private UserStory userStory;
	private Project currentProject;
	private boolean isUserInAnyRolesSprint;
	private boolean isUserInAnyRolesStatistic;
	private int totalUnDoneTask = 0;
	public String getSprintStatus() {
		try {
			Sprint sprint = sprintService.findSprintById(sprintId);
			return sprint.getStatus();
		} catch (Exception e) {
			return "null";
		}
	}
	
	public int countRemainingTasks() {
		List<Issue> remainingTasks = issueService.findRemainingTasksByProjectAndTeam(projectId,teamId);
		int totalTask = 0;
		for (Issue parentIssue : remainingTasks) {
			//add parent to new list
			if (!issueService.checkIssueParentDoneBySubject(parentIssue.getSubject())) {
				//count parent task
				totalTask = totalTask + 1;
			}
		}
		this.setTotalUnDoneTask(totalTask);
		return totalTask;
	}
	
	public List<Sprint> retrieveAllSprintBeforeByIssue(Issue issue){
		if(issue == null){
			return null;
		}
		List<Sprint> sprints = issueService.getAllSprintBeforeByIssue(issue);
		return sprints;
	}
	public List<Sprint> retrieveAllSprintAfterByIssue(Issue issue){
		if(issue == null){
			return null;
		}
		List<Sprint> sprints = issueService.getAllSprintAfterByIssue(issue);
		return sprints;
	}
	
	public void linkFromHistoryToScrumBoard(Sprint sprint) throws IOException {
		dashboardBean.prepareData();
		dashboardBean.getSprint().getTeam().setTeamId(this.teamId);
		dashboardBean.handleChangeTeam();
		dashboardBean.getSprint().setSprintId(sprint.getSprintId());
		String contextPath = FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath() + "/sprint";
		FacesContext.getCurrentInstance().getExternalContext().redirect(contextPath);
	}
	
	public void linkToRemainingTask() {
		try {
			remainingTasksBean.setSelectedProjectIdInDropdownList(this.projectId);
			remainingTasksBean.setSelectedTeamIdInDropdownList(this.teamId);
			remainingTasksBean.updateTeamsDropDownListWhenProjectChanges();
			String contextPath = FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath() + "/remaining";
			FacesContext.getCurrentInstance().getExternalContext().redirect(contextPath);
		} catch (Exception e) {
			LOGGER.error("errow when navigate to remaining task");
		}
		
	}
	
	public void linkToScrumBoard(Issue issue) throws IOException {
		Sprint continuousSprint = retrieveNextSprintFromPreviousId(issue);
		if(continuousSprint != null){
			dashboardBean.prepareData();
			dashboardBean.getSprint().getTeam().setTeamId(this.teamId);
			dashboardBean.handleChangeTeam();
			dashboardBean.getSprint().setSprintId(continuousSprint.getSprintId());
			String contextPath = FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath() + "/sprint";
			FacesContext.getCurrentInstance().getExternalContext().redirect(contextPath);
		}else{
			String contextPath = FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath() + "/sprintbacklog";
			FacesContext.getCurrentInstance().getExternalContext().redirect(contextPath);
		}
	}
	
	public Sprint retrieveNextSprintFromPreviousId(Issue issue) {
		return issueService.findSprintHasContinuousIssue(issue);
	}
	
	public void setSprintStatus(String sprintStatus) {
		this.sprintStatus = sprintStatus;
	}

	public String getSearchedKeyWord() {
		return searchedKeyWord;
	}

	public void setSearchedKeyWord(String searchedKeyWord) {
		this.searchedKeyWord = searchedKeyWord;
	}

	public Hashtable<Long, List<Object>> getSprintBacklogInfo() {
		return sprintBacklogInfo;
	}

	public void setSprintBacklogInfo(Hashtable<Long, List<Object>> sprintBacklogInfo) {
		this.sprintBacklogInfo = sprintBacklogInfo;
	}

	public List<Issue> getSprintBacklogIssue() {
		initSprintBacklog();
		return sprintBacklogIssue;
	}

	public void setSprintBacklog(List<Issue> sprintBacklog) {
		sprintBacklogIssue = sprintBacklog;
	}

	public List<UserStory> getProductBacklog() {
		return productBacklog;
	}

	public void setProductBacklog(List<UserStory> productBacklog) {
		this.productBacklog = productBacklog;
	}

	/**
	 * Load user stories for product backlog
	 * 
	 */
	public void initProductBacklog() {
		try {
			List<Issue> issues = issueService.findIssueBySprintId(sprintId);
			List<UserStory> usList = filterProductBackLog();
			List<UserStory> usRemove = new ArrayList<UserStory>();
			jsonObject = new JSONObject();
			// Create list of user stories
			for (UserStory us : usList) {
				for (Issue issue : issues) {
					try {
						if (issue.getUserStory().getUserStoryId().equals(us.getUserStoryId())) {
							usRemove.add(us);
							break;
						}
					} catch (Exception e) {
						continue;
					}

				}
				// Put progress data to json
				JSONObject jsonChild = new JSONObject();
				jsonChild.put("progress", usService.findProgressOfUserStory(us));
				jsonObject.put("US_" + us.getUserStoryId(), jsonChild);
			}
			for (UserStory us : usRemove) {
				usList.remove(us);
			}
			setProductBacklog(usList);
		} catch (Exception e) {
			LOGGER.error(e);
		}
	}
	
	public void getLastSortIdOfUS(){
		this.setLastUserStory(this.userStoryService.findLastUserStoryInProject(this.projectId));
	}

	/**
	 * Load issues for sprint backlog
	 * 
	 */
	public void initSprintBacklog() {
		isUserInAnyRolesSprint = CustomELFuntions.isUserInAnyRoles(CustomELFuntions.DEVELOPER + ","
						+ CustomELFuntions.SCRUM_MASTER + ","
						+ CustomELFuntions.PRODUCT_OWNER, teamId);
		isUserInAnyRolesStatistic = CustomELFuntions.isUserInAnyRoles(CustomELFuntions.DEVELOPER + ","
						+ CustomELFuntions.SCRUM_MASTER + ","
						+ CustomELFuntions.PRODUCT_OWNER, Long.parseLong(statisticBean.getSelectedDropDownTeamId()));
		
		sprintBacklogInfo = new Hashtable<Long, List<Object>>();
		try {
			List<Issue> issues = filterSprintBackLog();
			List<Issue> usListRS = new ArrayList<Issue>();
			List<Integer> usProgressRS = new ArrayList<Integer>();
			for (Issue parentIssue : issues) {
				if (parentIssue.getParent() != null) {
					continue;
				}
				usListRS.add(parentIssue);
				usProgressRS.add(issueService.findProgressOfIssue((parentIssue.getIssueId())));
				List<Object> l = new ArrayList<Object>();
				l.add(issueService.findProgressOfIssue(parentIssue.getIssueId()));
				sprintBacklogInfo.put(parentIssue.getIssueId(), l);
				String totalPoint = issueService.getTotalPoint(parentIssue);
				if ((!totalPoint.equals(parentIssue.getEstimate())) && (parentIssue.getParent() == null)) {
					if (parentIssue.getPointFormat().equals("1")) {
						l.add(parentIssue.getEstimate());
						l.add(totalPoint);
						l.add(totalPoint);
						l.add(totalPoint);
					} else {
						l.add((int) Utils.convertPoint(parentIssue.getEstimate()));
						l.add((int) Utils.convertPoint(totalPoint));
						l.add(totalPoint);
						l.add(totalPoint);
					}
				} else {
					l.add(null);
					l.add(null);
					l.add(null);
					l.add("none");
				}
				l.add(parentIssue.getEstimate());
				l.add(parentIssue.getRemain());
			}
			setSprintBacklog(usListRS);

		} catch (Exception e) {
			LOGGER.debug(e);
		}
	}

	private List<Issue> filterSprintBackLog() {
		List<Issue> listIssues = issueService.findIssueBySprintId(sprintId);
		List<Issue> issuesFilterAndSearch = new ArrayList<Issue>();
		for (int i = 0; i < listIssues.size(); i++) {
			String status = issueService.findStatusOfIssue(listIssues.get(i));
			if ((listIssues.get(i).getSubject().toLowerCase().contains(searchedKeyWord.toLowerCase()) || listIssues.get(i).getDisplayIssueId().toString().toLowerCase()
					.contains(searchedKeyWord.toLowerCase()))) {
				if (selectedFilter.contains(status)) {
					issuesFilterAndSearch.add(listIssues.get(i));
				}
			}
		}
		return issuesFilterAndSearch;
	}

	private List<UserStory> filterProductBackLog() {
		List<UserStory> userstoryFilterAndSearch = new ArrayList<UserStory>();
		List<UserStory> usList = getListUs();
		for (int i = 0; i < usList.size(); i++) {
			UserStory.StatusType status = usService.findStatusOfUserStory(usList.get(i));

			if ((usList.get(i).getName().toLowerCase().contains(searchedKeyWord.toLowerCase()) || usList.get(i).getUserStoryId().toString().toLowerCase().contains(searchedKeyWord.toLowerCase()))) {
				if (selectedFilter.contains(status.name()) && !usList.get(i).getStatus().name().equals("VOID")) {
					userstoryFilterAndSearch.add(usList.get(i));
				} else if (selectedFilter.contains("VOID") && usList.get(i).getStatus().name().equals("VOID")) {
					userstoryFilterAndSearch.add(usList.get(i));
				}
			}
		}
		return userstoryFilterAndSearch;
	}

	/**
	 * Move a user story to sprint backlog a issue will be created with the
	 * issue's priority is the same to the user's priority
	 * 
	 */
	public void moveUserStoryToSprintBacklog() {
		String idPanel = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("id");
		String idUserStory = idPanel.substring(INDEX_ID_PANEL);
		try {
			UserStory usChange = usService.findUserStoryById(Long.parseLong(idUserStory));
			Issue issue = new Issue();
			issue.setSubject(usChange.getName());
			issue.setDescription(usChange.getDescription());
			issue.setType("Task");
			issue.setPriority(usChange.getPriority().toString());
			issue.setEstimate("D0T0");
			issue.setRemain("D0T0");
			issue.setPointFormat("1");
			issue.setSprint(sprintService.findSprintById(sprintId));
			issue.setUserStory(usChange);
			issue.setStatus(statusService.findStatusStartBySprintId(sprintId));
			issue.setNote(usChange.getNote());
			
			if (issueService.findIssuesByUserStory(usChange.getUserStoryId()) == null ||
					issueService.findIssuesByUserStory(usChange.getUserStoryId()).isEmpty()) {
				issue.setOldId(null);
			} else {
				Issue originalTask = issueService.findOldestTaskOfUserStory(usChange.getUserStoryId());
				issue.setOldId(originalTask.getIssueId());
			}
			
			issue.setCreatedDate(new Date());
			
			long newIssueId = issueService.saveIssueAndGetId(issue);
			sprintBacklogIssue.add(issue);
			issueService.updatePointRemain("D0T0", issue.getIssueId());
			
			// copy the attachments from user story to issue
			List<Attachment> atts = attachmentService.findAttachmentByUserStory(usChange);
			if(!atts.isEmpty()) {
				logger.warn("copy all file from userstory to issue");
				for(Attachment USAttachment : atts){
					Attachment attachment = new Attachment();
					attachment.setFilename(USAttachment.getFilename());
					attachment.setDiskFilename(USAttachment.getDiskFilename());
					attachment.setContainerId(newIssueId);
					attachment.setContainerType(Attachment.ISSUE_ATTACHMENT);
					attachment.setTemp(false);
					attachment.setCreatedOn(new Date());
					attachment.setAuthor(this.utils.getLoggedInMember());
					attachmentService.save(attachment);
				}
			}
			
			RequestContext.getCurrentInstance().update("form2");
			String newID = issue.getIssueId() + "";
			RequestContext context = RequestContext.getCurrentInstance();
			context.addCallbackParam("newId", newID);

			// Update status userstory
			UserStory userStory = userStoryService.findUserStoryByIssue(issue);
			UserStory.StatusType status = userStoryService.findStatusOfUserStory(userStory);
			userStory.setStatus(status);
			userStoryService.update(userStory);
			//update kanbanissue status belong to userstory
			Sprint currentSprint = sprintService.findSprintById(sprintId);
			kanbanIssueService.updateAllKanbanIssueByUserStoryStatusOfTeam(userStory,currentSprint.getTeam());
		} catch (Exception e) {
			LOGGER.debug(e);
		}
	}

	/**
	 * Move issue to product backlog
	 * 
	 */
	public void moveIssueToProductBacklog() {
		String idPanel = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("id");
		String idIssueBase = idPanel.substring(INDEX_ID_PANEL);
		String idIssue = idIssueBase.split("-")[0];
		try {
			// Delete issue
			Issue issue = issueService.findIssueById(Long.parseLong(idIssue));
			UserStory userStory = userStoryService.findUserStoryByIssue(issue);
			
			issueService.deleteIssue(issue);
			sprintBacklogIssue.remove(issue);

			// Update status user story
			UserStory.StatusType status = userStoryService.findStatusOfUserStory(userStory);
			userStory.setStatus(status);
			userStoryService.update(userStory);
			//update kanbanissue status belong to userstory
			Sprint currentSprint = sprintService.findSprintById(sprintId);
			kanbanIssueService.updateAllKanbanIssueByUserStoryStatusOfTeam(userStory,currentSprint.getTeam());

			int index = 0;
			if (sprintPosition.get(idIssue) != null) {
				index = sprintPosition.get(idIssue);
				sprintPosition.remove(idIssue);
			}
			RequestContext.getCurrentInstance().addCallbackParam("save", true);
			for (String key : sprintPosition.keySet()) {
				if (sprintPosition.get(key) > index) {
					sprintPosition.put(key, sprintPosition.get(key) - 1);
				}
			}
			RequestContext.getCurrentInstance().update("form2");
		} catch (Exception e) {
		}
	}

	public JSONObject getJsonObject() {
		initProductBacklog();
		return jsonObject;
	}

	public void setJsonObject(JSONObject jsonObject) {
		this.jsonObject = jsonObject;
	}

	@PostConstruct
	public void initPost() {
		init();
		flagChange = false;
		initSprintBacklog();
	}

	public void initPreview() {
		if (JSFUtils.isPostbackRequired()) {
			if (flagFirst) {
				flagFirst = false;
				return;
			}
			init();
		}
	}

	/**
	 * Set projects, teams, sprints belong the current user for three combobox
	 */
	public void init() {
		try {
			setProjects(projectService.findByMemberAndOwner(utils.getLoggedInMember().getMemberId()));
			if ((projects != null) && (projects.size() > 0)) {
				if (getProjectId() == 0) {
					setProjectId(projects.get(0).getProjectId());
				}
				//init current project
				this.currentProject = this.projectService.findProjectById(getProjectId());
				
				setTeams(teamService.findTeamsByProjectIdAndMemberId(projectId, utils.getLoggedInMember().getMemberId()));
				if ((getTeams() != null) && (getTeams().size() > 0)) {
					if (getTeamId() == 0) {
						setTeamId(getTeams().get(0).getTeamId());
					}
				}

				setSprints(sprintService.findSprintsByTeamId(teamId));
				if ((getSprints() != null) && (getSprints().size() > 0)) {
					if (getSprintId() == 0) {
						setSprintId(sprints.get(0).getSprintId());
					}
				}
				if ((teams == null) || (teams.size() == 0)) {
					setTeams(null);
					setSprints(null);
					setSprintId(0);
				}

			} else {
				setProjects(null);
				setTeams(null);
				setSprints(null);
			}
			//init userstory
			this.userStory = new UserStory();
			
		} catch (Exception e) {
		}
		
	}
	
	public void validateUserStory(FacesContext context, UIComponent validate, Object value) throws ValidatorException {
		if (this.userStoryService.checkExistUserStory(Utils.standardizeString(value.toString()), getProjectId()) != null) {
			@SuppressWarnings("static-access")
			FacesMessage msg = new FacesMessage(this.utils.getMessage("myagile.backlog.Exists", null));
			throw new ValidatorException(msg);
		}
		if (utils.isExistTooLongWord(value.toString())) {
			@SuppressWarnings("static-access")
			FacesMessage msg = new FacesMessage(this.utils.getMessage("myagile.backlog.LongestLength", null));
			throw new ValidatorException(msg);
		}
	}

	/**
	 * Handle when user change project in combobox
	 */
	public void handleChangeProject() {
		setTeams(teamService.findTeamsByProjectIdAndMemberId(projectId, utils.getLoggedInMember().getMemberId()));
		this.currentProject = this.projectService.findProjectById(this.projectId);
		this.setLastUserStory(this.userStoryService.findLastUserStoryInProject(this.projectId));
		if ((teams != null) && (teams.size() > 0)) {
			setTeamId(teams.get(0).getTeamId());
		} else {
			setTeamId(0);
		}
		handleChangeTeam();
	}

	/**
	 * Handle when user change team in combobox
	 */
	public void handleChangeTeam() {
		setSprints(sprintService.findSprintsByTeamId(teamId));
		if (sprints.size() > 0) {
			setSprintId(sprints.get(0).getSprintId());
		} else {
			setSprintId(0);
		}
		initSprintBacklog();
	}
	
	public void handleChangeSprint() {
		initSprintBacklog();
	}
	
	public void saveUserStory(){
		this.userStory.setProject(this.projectService.findProjectById(this.projectId));
		
		//Remove Control Characters
		String newName = userStory.getName().replaceAll("\\p{Cntrl}", "");
		userStory.setName(newName);
		String newDescription = userStory.getDescription().replaceAll("\\p{Cntrl}", "");
		userStory.setDescription(newDescription);
		String newNote = userStory.getNote().replaceAll("\\p{Cntrl}", "");
		userStory.setNote(newNote);
		
		if (this.userStoryService.create(this.userStory)) {
			this.userStory.setName("");
			this.userStory.setValue(0);
			this.userStory.setRisk(0);
			this.userStory.setDescription("");
			this.userStory.setNote("");
		}
	}

	/**
	 * Update parent point equal with all its children
	 */
	public void updateParentPoint() {
		String issueIdString = JSFUtils.getRequestParameter("issueId");
		String newPointString = JSFUtils.getRequestParameter("newPoint");
		Issue issue = issueService.findIssueById(Long.parseLong(issueIdString));
		issue.setEstimate(newPointString);
		issueService.updateStatusOfIssueParent(issue);
	}

	public UserStory.StatusType getStatusUserStory(UserStory us) {
		return usService.findStatusOfUserStory(us);
	}

	public String getStatusIssue(Issue is) {
		return issueService.findStatusOfIssue(is);
	}

	public void setFlagChange(boolean flagChange) {
		this.flagChange = flagChange;
	}

	public boolean getFlagChange() {
		return flagChange;
	}

	public void setFlagChangeTrue() {
		flagChange = true;
	}

	public Dashboard getDashboard() {
		if ((dashboard == null) || flagChange) {
			flagChange = false;
		}
		return dashboard;
	}

	public void setDashboard(Dashboard dashboard) {
		this.dashboard = dashboard;
	}

	public long getProjectId() {

		return projectId;
	}

	public List<Project> getProjects() {
		if (projects == null) {
			projects = new ArrayList<Project>();
		}
		return projects;
	}

	public long getSprintId() {
		return sprintId;
	}

	public List<Sprint> getSprints() {
		if (sprints == null) {
			sprints = new ArrayList<Sprint>();
		}
		return sprints;
	}

	public long getTeamId() {
		return teamId;
	}

	public List<Team> getTeams() {
		if (teams == null) {
			teams = new ArrayList<Team>();
		}
		return teams;
	}

	public List<UserStory> getListUs() {
		return usService.findAllUserStoryByProjectId(projectId);
	}

	public void setListUs(List<UserStory> listUs) {
		this.listUs = listUs;
	}

	public void setProjectId(long projectId) {
		this.projectId = projectId;
	}

	public void setProjects(List<Project> projects) {
		this.projects = projects;
	}

	public void setSprintId(long sprintId) {
		this.sprintId = sprintId;
	}

	public void setSprints(List<Sprint> sprints) {
		this.sprints = sprints;
	}

	public void setTeamId(long teamId) {
		this.teamId = teamId;
	}

	public void setTeams(List<Team> teams) {
		this.teams = teams;
	}

	public boolean isProjectHasUserStory() {
		return (usService.findAllUserStoryByProjectId(projectId).size() > 0);
	}

	public List<String> getSelectedFilter() {
		return selectedFilter;
	}

	public void setSelectedFilter(List<String> selectedFilter) {
		this.selectedFilter = selectedFilter;
	}

	public Boolean getShowFormAddBL() {
		return showFormAddBL;
	}

	public void setShowFormAddBL(Boolean showFormAddBL) {
		this.showFormAddBL = showFormAddBL;
	}

	public UserStory getUserStory() {
		return userStory;
	}

	public void setUserStory(UserStory userStory) {
		this.userStory = userStory;
	}

	public Project getCurrentProject() {
		return currentProject;
	}

	public void setCurrentProject(Project currentProject) {
		this.currentProject = currentProject;
	}

	public UserStory getLastUserStory() {
		return lastUserStory;
	}

	public void setLastUserStory(UserStory lastUserStory) {
		this.lastUserStory = lastUserStory;
	}

	public boolean getIsUserInAnyRolesSprint() {
		return isUserInAnyRolesSprint;
	}

	public boolean getIsUserInAnyRolesStatistic() {
		return isUserInAnyRolesStatistic;
	}

	public int getTotalUnDoneTask() {
		return totalUnDoneTask;
	}

	public void setTotalUnDoneTask(int totalUnDoneTask) {
		this.totalUnDoneTask = totalUnDoneTask;
	}
}
