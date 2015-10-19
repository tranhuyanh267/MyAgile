package com.ant.myagile.managedbean;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
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

import com.ant.myagile.model.Issue;
import com.ant.myagile.model.Member;
import com.ant.myagile.model.Project;
import com.ant.myagile.model.Sprint;
import com.ant.myagile.model.Team;
import com.ant.myagile.model.UserStory;
import com.ant.myagile.service.IssueService;
import com.ant.myagile.service.PointRemainService;
import com.ant.myagile.service.ProjectService;
import com.ant.myagile.service.SprintService;
import com.ant.myagile.service.StatusService;
import com.ant.myagile.service.TeamService;
import com.ant.myagile.service.UserStoryService;
import com.ant.myagile.utils.JSFUtils;
import com.ant.myagile.utils.Utils;

@Component("meetingBean")
@Scope("session")
public class MeetingBean implements Serializable {
	private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger
            .getLogger(MeetingBean.class);
	private Issue selectedIssue;
	private Issue newIssue = new Issue();
	private String selectedParentIssueId;	
	private String totalEstimatePoint;
	private String pointEstimateFormat;
	
	@Autowired
	private IssueService issueService;
	@Autowired
	private StatusService statusService;
	@Autowired
	private PointRemainService pointRemainService;
	@Autowired
	private UserStoryService userStoryService;

	private List<Project> projects;
	private List<Team> teams;
	private List<Sprint> sprints;
	private List<Issue> issues;

	private long projectId;
	private long teamId;
	private long sprintId;	
	private TreeNode issueListTree;
	@Autowired
	private ProjectService projectService;
	@Autowired
	private TeamService teamService;
	@Autowired
	private SprintService sprintService;
	@Autowired
	private Utils utils;

	@PostConstruct
	public void initial() {
		projects = projectService.findByMemberAndOwner(getLoggedInMember()
				.getMemberId());
		try {
			selectProject(projects.get(0).getProjectId());
		} catch (Exception e) {
            LOGGER.warn(e);
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
		issueService.saveIssue(newIssue);
		
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
		Issue issueParent = selectedIssue.getParent(); 
		issueService.deleteIssue(this.selectedIssue);
		if(issueParent != null){
			issueService.updateStatusOfIssueParent(issueParent);
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
		teamId = selectedTeamId;
		sprints = sprintService.findSprintsByTeamId(teamId);
		try {
			selectSprint(sprints.get(0).getSprintId());
		} catch (Exception e) {
            LOGGER.warn(e);
			selectSprint(0);
		}
	}

	public void selectSprint(long selectedSprintId) {
		try{
			this.resetListIssueForm();
		}catch(Exception e){
            LOGGER.warn(e);
		}
		sprintId = selectedSprintId;
		loadIssues();
	}
	/**
	 * Create a tree of issues with relationship between parent and child issues
	 */
	public void loadIssues() {	
		if(this.getIssues() != null) {
			this.getIssues().clear();
		}
		issueListTree = new DefaultTreeNode("root", null);
		List<Issue> issuestemp = issueService.findIssuesParent(projectId, sprintId);
		try {
			issuestemp.addAll(issueService.findIssuesSingle(sprintId));
		} catch (Exception e) {
            LOGGER.warn(e);
		}
		try {
			for (Issue i : issuestemp) {
				TreeNode parent = new DefaultTreeNode(String.valueOf(i
						.getIssueId()), i, issueListTree);
				parent.setExpanded(true);
				
				List<Issue> subissues = issueService.findIssueByParent(i);
				if(subissues == null || subissues.size() <= 0) {
					this.getIssues().add(i);
				}
				try {					
					for (Issue j : subissues) {
						new DefaultTreeNode(String.valueOf(j.getIssueId()), j,parent);
						this.getIssues().add(j);
					}
							
				} catch (Exception e) {
                    LOGGER.warn(e);
				}
			}
			
		} catch (Exception e) {
            LOGGER.warn(e);
		}
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

	public TreeNode getissueListTree() {
		return issueListTree;
	}

	public void setissueListTree(TreeNode issueListTree) {
		this.issueListTree = issueListTree;
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
}
