package com.ant.myagile.managedbean;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import org.apache.log4j.Logger;
import org.primefaces.context.RequestContext;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.ant.myagile.dto.LazyFilter;
import com.ant.myagile.dto.LazySorter;
import com.ant.myagile.dto.StateLazyLoading;
import com.ant.myagile.dto.LazySorter.LAZYSORTER_VALUE;
import com.ant.myagile.model.Issue;
import com.ant.myagile.model.KanbanIssue;
import com.ant.myagile.model.Member;
import com.ant.myagile.model.Project;
import com.ant.myagile.model.Sprint;
import com.ant.myagile.model.Team;
import com.ant.myagile.model.UserStory;
import com.ant.myagile.service.IssueService;
import com.ant.myagile.service.KanbanIssueService;
import com.ant.myagile.service.PointRemainService;
import com.ant.myagile.service.ProjectService;
import com.ant.myagile.service.SprintService;
import com.ant.myagile.service.StatusService;
import com.ant.myagile.service.TeamService;
import com.ant.myagile.service.UserStoryService;
import com.ant.myagile.utils.CustomELFuntions;
import com.ant.myagile.utils.JSFUtils;
import com.ant.myagile.utils.Utils;

@Component("meetingBeanV2")
@Scope("session")
public class MeetingBeanV2 implements Serializable {
	private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger
            .getLogger(MeetingBeanV2.class);
    
    @Autowired
	private ProjectService projectService;
	@Autowired
	private TeamService teamService;
	@Autowired
	private SprintService sprintService;
	@Autowired
	private Utils utils;
	@Autowired
	private IssueService issueService;
	@Autowired
	private StatusService statusService;
	@Autowired
	private PointRemainService pointRemainService;
	@Autowired
	private UserStoryService userStoryService;
	@Autowired
	private KanbanIssueService kanbanIssueService;
	
	private Issue selectedIssue;
	private Issue newIssue = new Issue();
	private String selectedParentIssueId;	
	private String totalEstimatePoint;
	private String pointEstimateFormat;

	private List<Project> projects;
	private List<Team> teams;
	private List<Sprint> sprints;
	private List<Issue> issues;

	private long projectId;
	private long teamId;
	private long sprintId;	
	private List<Issue> issuesTree = new ArrayList<Issue>();
	private boolean devOrScrumMasterOrPO = false;
	private boolean sprintOpen = false;
	
	private StateLazyLoading lazyLoadingIsuseList;
	private boolean remainIssues = true;
	private int totalRowIssues = 0;
	
	
	
	

	@PostConstruct
	public void initial() {
		projects = projectService.findByMemberAndOwner(getLoggedInMember()
				.getMemberId());
		try {
			selectProject(projects.get(0).getProjectId());
		} catch (Exception e) {
            LOGGER.warn(e);
		}
		
		defaultStateLoadingIssue();
		
	}



	private void defaultStateLoadingIssue() {
		
		lazyLoadingIsuseList = new StateLazyLoading();
		lazyLoadingIsuseList.setStart(0);
		lazyLoadingIsuseList.setLimit(15);
		lazyLoadingIsuseList.setStep(20);
		//default sort
		LazySorter lazySorterForIssue = new LazySorter();
		lazySorterForIssue.setField("issueId");
		lazySorterForIssue.setValue(LAZYSORTER_VALUE.DESC);
		
		//default field
		LazyFilter lazyFilterForIssue = new LazyFilter();
		List<String> fields = new ArrayList<String>();
		fields.add("subject");
		fields.add("description");
		lazyFilterForIssue.setField(fields);
		lazyFilterForIssue.setValue("");
		
		lazyLoadingIsuseList.setFilters(lazyFilterForIssue);
		lazyLoadingIsuseList.setSorters(lazySorterForIssue);
		
	}
	
	private void resetStateLoadingIssue(){
		defaultStateLoadingIssue();
	}
	
	public void loadmoreIssue(){
		int nextLimit = lazyLoadingIsuseList.getLimit() + lazyLoadingIsuseList.getStep();
		lazyLoadingIsuseList.setLimit(nextLimit);
		loadIssues();
	}
	
	public void sortIssuesTree(){
		String dataField = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("dataField");
		String currentValueSortField = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("valueSortField");
		lazyLoadingIsuseList.getSorters().setField(dataField);
		if("none".equals(currentValueSortField.toLowerCase())){
			lazyLoadingIsuseList.getSorters().setValue(LAZYSORTER_VALUE.ASC);
		}else if("asc".equals(currentValueSortField.toLowerCase())){
			lazyLoadingIsuseList.getSorters().setValue(LAZYSORTER_VALUE.DESC);
		}else if("desc".equals(currentValueSortField.toLowerCase())){
			lazyLoadingIsuseList.getSorters().setValue(LAZYSORTER_VALUE.ASC);
		}
		loadIssues();
	}
	
	public void searchIssuesTree(){
		loadIssues();
	}
	
	/**
	 * Create a tree of issues with relationship between parent and child issues
	 */
	public void loadIssues() {	
		if(this.getIssues() != null) {
			this.getIssues().clear();
			issuesTree.clear();
		}
		List<Issue> issuestemp = issueService.findLazyIssuesParentByProjectIdAndSprintId(projectId, sprintId,lazyLoadingIsuseList);
		//check remain data or not
		int totalRowIssue = issueService.countTotalLazyIssuesParentByProjectIdAndSprintId(projectId, sprintId,lazyLoadingIsuseList);
		
		try {
			for (Issue i : issuestemp) {
				issuesTree.add(i);
				
				List<Issue> subissues = issueService.findIssueByParent(i);
				if(subissues == null || subissues.size() <= 0) {
					this.getIssues().add(i);
				}
				try {					
					for (Issue j : subissues) {
						issuesTree.add(j);
						this.getIssues().add(j);
					}
				} catch (Exception e) {
                    LOGGER.error(e);
				}
			}
			
		} catch (Exception e) {
            LOGGER.error(e);
		}
		this.totalRowIssues = totalRowIssue;
		if(totalRowIssue == issuesTree.size()){
			remainIssues = false;
		}else{
			remainIssues = true;
		}
	}

	public void syncBeanData() {
		if (JSFUtils.isPostbackRequired()) {
			projects = projectService.findByMemberAndOwner(getLoggedInMember()
					.getMemberId());
			verifyProjectId();
			verifyTeamId();
			verifySprintId();
			loadIssues();
			//sprint close is true
			sprintOpen = CustomELFuntions.isSprintClosed(this.sprintId, "closed");
			devOrScrumMasterOrPO = CustomELFuntions.isUserInAnyRoles("DEVELOPER,SCRUM_MASTER,PRODUCT_OWNER", this.teamId);
		}		
	}
	public void updateParentIssue(Issue currentIssue,Issue newParentIssue){
		currentIssue.setParent(newParentIssue);
		issueService.updateIssue(currentIssue);
		//update issue list
		loadIssues();
	}
	
	//get issue without parent and that issue don't have this child
	public List<Issue> getParentIssuesBySprintExceptParentThis(long issueId){
		return issueService.findParentIssuesBySprintAndExceptParentThis(issueId, this.sprintId);
	}
		
	/**
	 * Create a new Issue by information entered in view and save into database
	 */
	public void addIssue() {
		
		//Remove Control Characters
		newIssue.setSubject(newIssue.getSubject().replaceAll("\\p{Cntrl}", ""));
		newIssue.setDescription(newIssue.getDescription().replaceAll("\\p{Cntrl}", ""));
		newIssue.setNote(newIssue.getNote().replaceAll("\\p{Cntrl}", ""));
		
		newIssue.setParent(selectedIssue);
		newIssue.setSprint(selectedIssue.getSprint());
		newIssue.setStatus(statusService.findStatusStartBySprintId(selectedIssue.getSprint().getSprintId()));
		newIssue.setEstimate("D0T0");
		newIssue.setRemain("D0T0");
		newIssue.setType("Task");
		newIssue.setPointFormat("1");
		newIssue.setPriority("MUST");		
		newIssue.setOldId(null);
		newIssue.setCreatedDate(new Date());
		if(issueService.saveIssue(newIssue)){
			//check last sprint of team
			Sprint lastSprint = sprintService.findLastSprintByTeamId(teamId);
			if(lastSprint != null && lastSprint.getSprintId().compareTo(newIssue.getSprint().getSprintId()) == 0){
				//add kanban issue if not exist
				//check exist kanban issue by content and userstory
				if(!kanbanIssueService.existKanbanIssueByUserStoryAndSubject(this.newIssue.getParent().getUserStory(),this.newIssue.getSubject())){
					//add new kanban issue
					KanbanIssue addKanbanIssue = new KanbanIssue();
					addKanbanIssue.setUserStory(this.newIssue.getParent().getUserStory());
					addKanbanIssue.setSubject(this.newIssue.getSubject());
					addKanbanIssue.setDescription(this.newIssue.getDescription());
					addKanbanIssue.setNote(this.newIssue.getNote());
					addKanbanIssue.setColumnDone(false);
					addKanbanIssue.setRemain(this.newIssue.getRemain());
					addKanbanIssue.setEstimate(this.newIssue.getEstimate());
					addKanbanIssue.setPointFormat("1");
					addKanbanIssue.setType("Task");
					addKanbanIssue.setIsSubIssue(true);
					addKanbanIssue.setIssueOfLastSprint(newIssue.getIssueId());
					addKanbanIssue.setTeam(newIssue.getSprint().getTeam());
					kanbanIssueService.saveKanbanIssue(addKanbanIssue);
				}
			}
		}
		
		// Update status userstory
		UserStory userStory =  userStoryService.findUserStoryByIssue(newIssue);
		UserStory.StatusType status = userStoryService.findStatusOfUserStory(userStory);
		userStory.setStatus(status);
		userStoryService.update(userStory);
		
		resetForm();
		RequestContext.getCurrentInstance().addCallbackParam("save", true);
		loadIssues();
	}
	
	/**
	 * Delete a issue and all its children if existing
	 */
	public void deleteIssue(){
		Long idIssue = selectedIssue.getIssueId();
		Issue issueParent = selectedIssue.getParent(); 
		issueService.deleteIssue(this.selectedIssue);
		
		if(issueParent != null){
			issueService.updateStatusOfIssueParent(issueParent);
			
			//check last sprint of team
			Sprint lastSprint = sprintService.findLastSprintByTeamId(teamId);
			if(lastSprint != null && lastSprint.getSprintId().compareTo(selectedIssue.getSprint().getSprintId()) == 0){
				kanbanIssueService.deleteKanbanIssueByIssueId(idIssue);
			}
		}
		loadIssues();
	}
	
	public void resetForm() {
		newIssue = new Issue();
		newIssue.setSubject(null);
		newIssue.setDescription(null);
		RequestContext context = RequestContext.getCurrentInstance();
		context.reset("add-issue-form");
	}	
	
	public void resetListIssueForm(){
		JSFUtils.resetForm("list-form");
	}

	public void selectProject(long selectedProjectId) {
		projectId = selectedProjectId;
		teams = teamService.findTeamsByProjectIdAndMemberId(projectId,
				getLoggedInMember().getMemberId());
		try {
			selectTeam(teams.get(0).getTeamId());
		} catch (Exception e) {
            LOGGER.warn(e);
			selectTeam(0);
		}
	}

	public void selectTeam(long selectedTeamId) {
		resetStateLoadingIssue();
		teamId = selectedTeamId;
		sprints = sprintService.findSprintsByTeamId(teamId);
		try {
			selectSprint(sprints.get(0).getSprintId());
		} catch (Exception e) {
            LOGGER.warn(e);
			selectSprint(0);
		}
		devOrScrumMasterOrPO = CustomELFuntions.isUserInAnyRoles("DEVELOPER,SCRUM_MASTER,PRODUCT_OWNER", this.teamId);
	}

	public void selectSprint(long selectedSprintId) {
		try{
			this.resetListIssueForm();
		}catch(Exception e){
            LOGGER.warn(e);
		}
		sprintId = selectedSprintId;
		loadIssues();
		sprintOpen = CustomELFuntions.isSprintClosed(this.sprintId, "closed");
	}
	

	public void verifyProjectId() {
		try {
			boolean isContain = false;
			for (Project p : projects) {
				if (p.getProjectId() == projectId) {
					isContain = true;
					break;
				}
			}
			if (!isContain) {
				selectProject(projects.get(0).getProjectId());
			} else {
				teams = teamService.findTeamsByProjectIdAndMemberId(projectId,
						getLoggedInMember().getMemberId());
			}
		} catch (Exception e) {
            LOGGER.warn(e);
			selectProject(0);

		}
	}

	public void verifyTeamId() {
		try {
			boolean isContain = false;
			for (Team t : teams) {
				if (t.getTeamId() == teamId) {
					isContain = true;
					break;
				}
			}
			if (!isContain) {
				selectTeam(teams.get(0).getTeamId());
			} else {
				sprints = sprintService.findSprintsByTeamId(teamId);
			}
		} catch (Exception e) {
            LOGGER.warn(e);
			selectTeam(0);
		}
	}

	public void verifySprintId() {
		try {
			boolean isContain = false;
			for (Sprint sp : sprints) {
				if (sp.getSprintId() == sprintId) {
					isContain = true;
					break;
				}
			}
			if (!isContain) {
				selectSprint(sprints.get(0).getSprintId());
			}
		} catch (Exception e) {
            LOGGER.warn(e);
			selectSprint(0);
		}
	}
	
	public boolean showPointConflictWarning (Issue issue) {
		return issueService.showPointConflictWarning(issue);
	}
	/**
	 * Find total estimate point of all child of a parent issue
	 * @param issue - instance of Issue object
	 * @return true if estimate point of parent and all child is equal, false in otherwise
	 */
	public boolean findTotalChildEstimatePoint(Issue issue){
		Issue issueTemp =  issueService.findIssueById(issue.getIssueId());
		if(issueTemp != null){
			String totalPoint = issueService.getTotalPoint(issueTemp);
			float totalPointFloat = Utils.convertPoint(totalPoint);
			float oldPointFloat = Utils.convertPoint(issueTemp.getEstimate());
			if((totalPointFloat != oldPointFloat) && (issueTemp.getParent() == null)){
				this.totalEstimatePoint = totalPoint;					
				return true;
			}
		}
		return false;
			
	}	
	/**
	 * Check the format of estimate point entered by user
	 * @param context
	 * @param validate
	 * @param value
	 */
	public void checkEstimatePoint(FacesContext context, UIComponent validate, Object value){
		pointRemainService.checkEstimatePoint(context, validate, value);		
	}
	/**
	 * Get format of estimate point
	 * @return '1' for DT format and '2' for only number format
	 */
	public String getPointEstimateFormat() {
		try{
			Long issueId = (Long) JSFUtils.resolveExpression("#{issue.issueId}");
			Issue issue = issueService.findIssueById(issueId);
			this.pointEstimateFormat = pointRemainService.getPointEstimateByFormat(issue);
	        return this.pointEstimateFormat;
		}
		catch(Exception e){
			return null;
		}
	}
	
	public void setPointEstimateFormat(String pointEstimateFormat) {
		this.pointEstimateFormat = pointEstimateFormat;
	}
	/**
	 * Update estimate point if an issue
	 * @param issueId - Issue ID, Long number format
	 */
	public void updateEstimatePoint(Long issueId){
		Issue issue = issueService.findIssueById(issueId);
		issueService.updateEstimatePoint(issue, this.pointEstimateFormat.trim());
		issueService.updatePointRemain(this.pointEstimateFormat.trim(),issueId);
		issueService.updateStatusOfIssueParent(issue.getParent());
		JSFUtils.resetForm("list-form");
		JSFUtils.addCallBackParam("update", true);
		loadIssues();
	}
	/**
	 * Update estimate point of parent issue
	 */
	public void updateParentPoint(){
		String issueIdString = JSFUtils.getRequestParameter("issueId");
		String newPointString = JSFUtils.getRequestParameter("newPoint");
		Issue issue = issueService.findIssueById(Long.parseLong(issueIdString));
		issue.setEstimate(newPointString);
		issueService.updateStatusOfIssueParent(issue);
		resetListIssueForm();
		loadIssues();
	}
		
	public void redirectToMeetingPage(){
		String issueIdString = JSFUtils.getRequestParameter("issueId");
		Project project = projectService.findProjectOfIssue(issueService.findIssueById(Long.parseLong(issueIdString)));
		this.projectId = project.getProjectId();
		this.teamId = Long.parseLong(JSFUtils.getRequestParameter("teamId"));
		this.sprintId = Long.parseLong(JSFUtils.getRequestParameter("sprintId"));
		this.setSelectedParentIssueId(issueIdString);
		try {
			FacesContext.getCurrentInstance().getExternalContext().redirect("meeting");
		} catch (IOException e) {}		
	}
	
	public float calculateIssuesTotalPoints() {
		try {
			return this.issueService.calculateIssuesTotalPoints(this.getIssues());
		} catch (Exception e) {
			return 0;
		}
	}
	
	public Issue getSelectedIssue() {
		return selectedIssue;
	}

	public void setSelectedIssue(Issue selectedIssue) {
		this.selectedIssue = selectedIssue;
	}

	public Issue getNewIssue() {
		return newIssue;
	}

	public void setNewIssue(Issue newIssue) {
		this.newIssue = newIssue;
	}
	
	public String getSelectedParentIssueId() {
		return selectedParentIssueId;
	}

	public void setSelectedParentIssueId(String selectedParentIssueId) {
		this.selectedParentIssueId = selectedParentIssueId;
	}
		
	public String getTotalEstimatePoint() {
		return totalEstimatePoint;
	}

	public void setTotalEstimatePoint(String totalEstimatePoint) {
		this.totalEstimatePoint = totalEstimatePoint;
	}


	public List<Project> getProjects() {
		return projects;
	}

	public void setProjects(List<Project> projects) {
		this.projects = projects;
	}

	public List<Team> getTeams() {
		return teams;
	}

	public void setTeams(List<Team> teams) {
		this.teams = teams;
	}

	public List<Sprint> getSprints() {
		return sprints;
	}

	public void setSprints(List<Sprint> sprints) {
		this.sprints = sprints;
	}

	public Member getLoggedInMember() {
		return utils.getLoggedInMember();
	}

	public long getProjectId() {
		return projectId;
	}

	public void setProjectId(long projectId) {
		this.projectId = projectId;
	}

	public long getTeamId() {
		return teamId;
	}

	public void setTeamId(long teamId) {
		this.teamId = teamId;
	}

	public long getSprintId() {
		return sprintId;
	}

	public void setSprintId(long sprintId) {
		this.sprintId = sprintId;
	}
	
	public List<Issue> getIssues() {
		if(this.issues == null) {
			this.issues = new ArrayList<Issue>();
		}
		return issues;
	}

	public void setIssues(List<Issue> issues) {
		this.issues = issues;
	}

	public List<Issue> getIssuesTree() {
		return issuesTree;
	}

	public void setIssuesTree(List<Issue> issuesTree) {
		this.issuesTree = issuesTree;
	}

	public boolean isDevOrScrumMasterOrPO() {
		return devOrScrumMasterOrPO;
	}

	public void setDevOrScrumMasterOrPO(boolean devOrScrumMasterOrPO) {
		this.devOrScrumMasterOrPO = devOrScrumMasterOrPO;
	}

	public boolean isSprintOpen() {
		return sprintOpen;
	}

	public void setSprintOpen(boolean sprintOpen) {
		this.sprintOpen = sprintOpen;
	}

	public StateLazyLoading getLazyLoadingIsuseList() {
		return lazyLoadingIsuseList;
	}
	
	public void setLazyLoadingIsuseList(StateLazyLoading lazyLoadingIsuseList) {
		this.lazyLoadingIsuseList = lazyLoadingIsuseList;
	}

	public boolean isRemainIssues() {
		return remainIssues;
	}

	public void setRemainIssues(boolean remainIssues) {
		this.remainIssues = remainIssues;
	}

	public int getTotalRowIssues() {
		return totalRowIssues;
	}

	public void setTotalRowIssues(int totalRowIssues) {
		this.totalRowIssues = totalRowIssues;
	}
	
}
