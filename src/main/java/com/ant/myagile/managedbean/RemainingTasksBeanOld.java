package com.ant.myagile.managedbean;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;

import org.apache.log4j.Logger;
import org.primefaces.context.RequestContext;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.binding.expression.ognl.OgnlExpressionParser;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.ant.myagile.model.Issue;
import com.ant.myagile.model.Project;
import com.ant.myagile.model.Sprint;
import com.ant.myagile.model.Team;
import com.ant.myagile.model.UserStory;
import com.ant.myagile.service.IssueService;
import com.ant.myagile.service.ProjectService;
import com.ant.myagile.service.RemainingTaskService;
import com.ant.myagile.service.SprintService;
import com.ant.myagile.service.StatusService;
import com.ant.myagile.service.TeamService;
import com.ant.myagile.service.UserStoryService;
import com.ant.myagile.utils.Utils;

@Component(value = "remainingTasksV2Bean")
@Scope("session")
public class RemainingTasksBeanOld {
	
	@Autowired
	private ProjectService projectService;
	
	@Autowired
	private TeamService teamService;
	
	@Autowired
	private UserStoryService userStoryService;
	
	@Autowired
	private Utils utilities;
	
	@Autowired
	private SprintService sprintService;
	
	@Autowired
	private RemainingTaskService remainingTaskService;
	
	@Autowired
	private StatusService statusService;
	
	@Autowired
	private IssueService issueService;
	
	private List<Project> allProjectsForDropdownList;
	private List<Team> allTeamsForDropdownList;
	private long selectedProjectIdInDropdownList = 0;
	private long selectedTeamIdInDropdownList = 0;
	private List<Issue> remainingTasks = new ArrayList<Issue>();
	private List<Long> originalIssueId = new ArrayList<Long>();
	private List<Long> selectedIssueId = new ArrayList<Long>();
	private Sprint nextSprint;
	private String msgConfirmCopyTaskToNextSprint = "";
	private String msgConfirmRemoveTaskFromNextSprint = "";
	private String msgInform = "";
	private long selectedTaskId = 0;
	private int totalTask = 0;
	private String assignConfirmMessage;
	
	private static final Logger LOGGER = Logger.getLogger(RemainingTasksBeanOld.class);
	

	@PostConstruct
	public void init() {
		allProjectsForDropdownList = projectService.findByMemberAndOwner(utilities.getLoggedInMember().getMemberId());
		
		if (allProjectsForDropdownListIsNotEmpty()) {
			if (selectedProjectIdInDropdownListIsNotSet()) {
				setSelectedProjectIdInDropdownList(allProjectsForDropdownList.get(0).getProjectId());
			}
			allTeamsForDropdownList = teamService.findTeamsbyProjectIdAndMemberId(selectedProjectIdInDropdownList, utilities.getLoggedInMember().getMemberId());
			if (!allTeamsForDropdownList.isEmpty()) {
				selectedTeamIdInDropdownList = allTeamsForDropdownList.get(0).getTeamId();
			}
			nextSprintOfTeam();
		}
		loadRemainingTasks();
	}
	
	public int findProgressOfIssue(long issueId) {
		return issueService.findProgressOfIssue(issueId);
	}
	
	public boolean checkIssueInNewSprint(Issue issue) {
		if (nextSprint == null) {
			return false;
		}
		if (issue.getSprint().getSprintId().equals(nextSprint.getSprintId())){
			return true;
		}
		return false;
	}
	
	public boolean checkEmptyChildsInParentIssue(Issue issue){
		List<Issue> childsIssue = issueService.findChildrenIssuesOfRemainingParent(issue.getIssueId());
		if(childsIssue.isEmpty()){
			return true;
		}
		return false;
	}
	
	public void assignSelectedTaskToNewSprint(long selectedTaskId) {
		Issue selectedRemainingTask = issueService.findIssueById(selectedTaskId);
		if (taskIsSingle(selectedRemainingTask)) {
			copyIssueToNextSprint(nextSprint, selectedRemainingTask);
		} else {
			Issue parentIssue = getParentIssueInNextSprint(selectedRemainingTask);
			if (parentIssueHasNotBeenCreated(parentIssue)) {
				Issue newParentIssue = createParentIssueInNextSprint(nextSprint,selectedRemainingTask);
				moveSubTaskToNextSprint(nextSprint, selectedRemainingTask, newParentIssue);
			} else {
				moveSubTaskToNextSprint(nextSprint, selectedRemainingTask, parentIssue);
			}
		}
		issueService.updateIssue(selectedRemainingTask);
	}
	
	public void unAssignSelectedTaskFromNewSprint(long unselectedTaskId) {
		Issue unselectedTask = issueService.findIssueById(unselectedTaskId);
		if (taskHasParent(unselectedTask)) {
			List<Issue> childrenIssues = issueService.findIssueByParent(unselectedTask.getParent());
			if (onlyOneChildLeft(childrenIssues)) {
				moveIssueBackToBacklog(unselectedTask.getParent().getIssueId());
			} else {
				moveIssueBackToBacklog(unselectedTask.getIssueId());
			}
			
		} else {
			moveIssueBackToBacklog(unselectedTask.getIssueId());
		}
		
	}
	
	public void applyChanges() {
		for (long selectedTaskId : taskSelected()) {
			assignSelectedTaskToNewSprint(selectedTaskId);
		}
		
		for (long unselectedTaskId : taskUnSelected()) {
			unAssignSelectedTaskFromNewSprint(unselectedTaskId);
		}
		
		loadRemainingTasks();
	}

	private void nextSprintOfTeam() {
		// get last sprint
		if (!theLatestSprintIsANewSprint()) {
			nextSprint = null;
		} else {
			nextSprint = sprintService.findLastSprintByTeamId(this.selectedTeamIdInDropdownList);
		}
	}
	
	public void selectedTask(){
		Long issueIdSelected = Long.valueOf(FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("issueIdSelected"));
		if(!selectedIssueId.contains(issueIdSelected)){
			selectedIssueId.add(issueIdSelected);
		}
	}
	public void unSelectedTask(){
		Long issueIdSelected = Long.valueOf(FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("issueIdSelected"));
		if(selectedIssueId.contains(issueIdSelected)){
			selectedIssueId.remove(issueIdSelected);
		}
	}
	
	
	public void confirmMoveTaskToSprint() {
		RequestContext context = RequestContext.getCurrentInstance();
		if (taskSelected().isEmpty() && taskUnSelected().isEmpty()) {
			context.execute("informDialog.show();");
		}else{ 
			if (taskUnSelected().isEmpty() && !taskSelected().isEmpty()) {
				this.assignConfirmMessage = "Would you like to add " + taskSelected().size() + " selected task(s) to '" + nextSprint.getSprintName() + "' ?";
			} else if (!taskUnSelected().isEmpty() && taskSelected().isEmpty()) {
				this.assignConfirmMessage = "Would you like to remove " + taskUnSelected().size() + " task(s) from '" + nextSprint.getSprintName() + "'?";
			} else if (!taskUnSelected().isEmpty() && !taskSelected().isEmpty()) {
				this.assignConfirmMessage = "Would you like to remove  " + taskUnSelected().size() + " user story(ies) and add " + taskSelected().size() + " new user story(ies) to '"+nextSprint.getSprintName()+"'?";
			}
			context.update("msgConfirm");
			context.execute("confirmAssignTaskToNewSprint.show();");
		}
	}
	
	private List<Long> taskSelected(){
		List<Long> addTasks = new ArrayList<Long>();
		for(Long selectedTask:selectedIssueId){
			if(!originalIssueId.contains(selectedTask)){
				addTasks.add(selectedTask);
			}
		}
		return addTasks;
	}
	
	private List<Long> taskUnSelected(){
		List<Long> removeTasks = new ArrayList<Long>();
		for(Long unSelectedTask : originalIssueId){
			if(!selectedIssueId.contains(unSelectedTask)){
				removeTasks.add(unSelectedTask);
			}
		}
		return removeTasks;
	}
	
	public void refreshBeanData() {
		nextSprintOfTeam();
		loadRemainingTasks();
	}

	private void loadRemainingTasks() {
		List<Issue> remainingParentTasks = new ArrayList<Issue>();
		remainingParentTasks = issueService.findRemainingTasksByProjectAndTeam(selectedProjectIdInDropdownList,
				selectedTeamIdInDropdownList);
		remainingTasks.clear();
		selectedIssueId.clear();
		originalIssueId.clear();
		totalTask = 0;
		//sort list remaing by value
		Comparator<Issue> comparator = new Comparator<Issue>() {
			@Override
			public int compare(Issue o1, Issue o2) {
				return o1.getSubject().compareTo(o2.getSubject());
			}
		};
		Collections.sort(remainingParentTasks,comparator);
		for (Issue parentIssue : remainingParentTasks) {
			
			List<Issue> subTasks = new ArrayList<Issue>();
			//add parent to new list
			if (!issueService.checkIssueParentDoneBySubject(parentIssue.getSubject())) {
				remainingTasks.add(parentIssue);
				//check issue in next sprint or not
				if(checkIssueInNewSprint(parentIssue)){
					originalIssueId.add(parentIssue.getIssueId());
					selectedIssueId.add(parentIssue.getIssueId());
				}
				//load child task
				subTasks = issueService.findChildrenIssuesOfRemainingParent(parentIssue.getIssueId());
				//dont count parent issue
				if(subTasks.isEmpty()){
					totalTask = totalTask + 1;
				}
				totalTask = totalTask + subTasks.size();
			}
			//add child to new list to
			for (Issue subTaskItem : subTasks) {
				if (!issueService.checkIssueChildDoneBySubject(subTaskItem.getSubject())) {
					//check issue not in next sprint
					if(nextSprint != null){
						if(!issueService.checkSubIssueInSprintBySubject(subTaskItem, nextSprint)){
							remainingTasks.add(subTaskItem);
							if(checkIssueInNewSprint(subTaskItem)){
								originalIssueId.add(subTaskItem.getIssueId());
								selectedIssueId.add(subTaskItem.getIssueId());
							}
						}else{
							if(subTaskItem.getSprint().getSprintId().equals(nextSprint.getSprintId())){
								remainingTasks.add(subTaskItem);
								if(checkIssueInNewSprint(subTaskItem)){
									originalIssueId.add(subTaskItem.getIssueId());
									selectedIssueId.add(subTaskItem.getIssueId());
								}
							}
						}
					}
				}
			}
		}
	}
	
	public boolean checkIssueInSelectedTask(Long issueId){
		if (selectedIssueId.contains(issueId)) {
			return true;
		} else {
			return false;
		}
		
	}
	
	public void confirmSetVoidAndInformation(long taskId) {
		selectedTaskId = taskId;
		Issue selectedTask = issueService.findIssueById(taskId);
		if (selectedTask.getParent() == null) {
			RequestContext context = RequestContext.getCurrentInstance();
			context.execute("confirmSetVoid.show();");
		} else {
			RequestContext context = RequestContext.getCurrentInstance();
			context.execute("informCannotSerVoid.show();");
		}
	}
	
	public void setVoid() {
		try {
			Issue taskToSetVoid = issueService.findIssueById(selectedTaskId);
			UserStory userStoryToSetVoid = taskToSetVoid.getUserStory();
			userStoryToSetVoid.setStatus(UserStory.StatusType.VOID);
			userStoryService.update(userStoryToSetVoid);
			loadRemainingTasks();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	

	public void setSprintWhenChangingTeam() {
		nextSprintOfTeam();
		loadRemainingTasks();
	}
	
	

	private boolean onlyOneChildLeft(List<Issue> childrenIssues) {
		return childrenIssues.size() == 1;
	}

	private boolean taskHasParent(Issue unselectedTask) {
		return unselectedTask.getParent() != null;
	}

	private void moveSubTaskToNextSprint(Sprint latestSprint,
			Issue selectedRemainingTask, Issue newParentIssue) {
		//move task to new sprint
		Issue newIssue = new Issue();
		newIssue.setSubject(selectedRemainingTask.getSubject());
		newIssue.setDescription(selectedRemainingTask.getDescription());
		newIssue.setType("Task");
		newIssue.setPriority(selectedRemainingTask.getPriority());
		newIssue.setEstimate("D0T0");
		newIssue.setRemain("D0T0");
		newIssue.setPointFormat("1");
		newIssue.setSprint(latestSprint);
		newIssue.setParent(newParentIssue);
		newIssue.setStatus(statusService.findStatusStartBySprintId(latestSprint.getSprintId()));
		newIssue.setNote(selectedRemainingTask.getNote());
		
		if (selectedRemainingTask.getOldId() == null) {
			newIssue.setOldId(selectedRemainingTask.getIssueId());
		} else {
			newIssue.setOldId(selectedRemainingTask.getOldId());
		}
		
		newIssue.setCreatedDate(new Date());
		issueService.saveIssue(newIssue);
	}
	
	public String getSprintNameIssueNotFinishInOldSprint(Issue issue){
		Issue issueNotInNextSprint = issueService.findNewestIssueAvailableBySubjectNotInSprint(issue, nextSprint);
		return issueNotInNextSprint.getSprint().getSprintName();
	}

	private Issue createParentIssueInNextSprint(Sprint latestSprint,
			Issue selectedRemainingTask) {
		//create parent issue
		Issue parent = selectedRemainingTask.getParent();
		Issue newParentIssue = createNewIssueFromPreviousTaskContent(latestSprint,
				parent);
		issueService.saveIssue(newParentIssue);
		return newParentIssue;
	}

	private boolean parentIssueHasNotBeenCreated(Issue parentIssue) {
		return parentIssue == null;
	}

	private Issue getParentIssueInNextSprint(Issue selectedRemainingTask) {
		Issue parentIssue = issueService.findIssueByUserStoryAndSprintAndCurrentIssue(selectedRemainingTask,nextSprint);
		return parentIssue;
	}

	private void copyIssueToNextSprint(Sprint latestSprint,
			Issue selectedRemainingTask) {
		Issue issue = issueService.findIssueByUserStoryAndSprint(selectedRemainingTask.getUserStory().getUserStoryId(),
				nextSprint.getSprintId());
		
		if (parentIssueHasNotBeenCreated(issue)) {
			Issue newParentIssue = createNewIssueFromPreviousTaskContent(latestSprint,
					selectedRemainingTask);
			issueService.saveIssue(newParentIssue);
		}
	}

	private boolean taskIsSingle(Issue selectedRemainingTask) {
		return selectedRemainingTask.getParent() == null;
	}
	
	private Issue createNewIssueFromPreviousTaskContent(Sprint latestSprint,
			Issue selectedRemainingTask) {
		Issue issue = new Issue();
		issue.setSubject(selectedRemainingTask.getSubject());
		issue.setDescription(selectedRemainingTask.getDescription());
		issue.setType("Task");
		issue.setPriority(selectedRemainingTask.getPriority());
		issue.setEstimate("D0T0");
		issue.setRemain("D0T0");
		issue.setPointFormat("1");
		issue.setSprint(latestSprint);
		issue.setUserStory(selectedRemainingTask.getUserStory());
		issue.setStatus(statusService.findStatusStartBySprintId(latestSprint.getSprintId()));
		issue.setNote(selectedRemainingTask.getNote());
		issue.setParent(null);
		
		if (issueService.findIssuesByUserStory(issue.getUserStory().getUserStoryId()) == null ||
				issueService.findIssuesByUserStory(issue.getUserStory().getUserStoryId()).isEmpty()) {
			issue.setOldId(null);
		} else {
			Issue originalTask = issueService.findOldestTaskOfUserStory(issue.getUserStory().getUserStoryId());
			issue.setOldId(originalTask.getIssueId());
		}
		
		return issue;
	}
	
	public void moveIssueBackToBacklog(long issueId) {
		// Delete issue
		Issue issueToMoveBack = issueService.findIssueById(issueId);
		UserStory userStory = userStoryService.findUserStoryByIssue(issueToMoveBack);
		issueService.deleteIssue(issueToMoveBack);
		
		// Update user story status
		UserStory.StatusType status = userStoryService.findStatusOfUserStory(userStory);
		userStory.setStatus(status);
		userStoryService.update(userStory);
	}
	
	public boolean theLatestSprintIsANewSprint() {
		Sprint latestSprint = sprintService.findLastSprintByTeamId(this.selectedTeamIdInDropdownList);
		if (latestSprint != null) {
			SimpleDateFormat formatYMD = new SimpleDateFormat("yyyy-MM-dd");
			Date now = new Date();
			try {
				now = formatYMD.parse(formatYMD.format(new Date()));
			} catch (ParseException e) {
				LOGGER.error("parse date error",e);
			}
			
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(now);
			return calendar.getTime().compareTo(latestSprint.getDateEnd()) <= 0;
		} else {
			return false;
		}
	}
	
	private boolean selectedProjectIdInDropdownListIsNotSet() {
		return selectedProjectIdInDropdownList == 0;
	}

	private boolean allProjectsForDropdownListIsNotEmpty() {
		return allProjectsForDropdownList != null && allProjectsForDropdownList.size() > 0;
	}
	
	public void updateTeamsDropDownListWhenProjectChanges() {
		allTeamsForDropdownList = teamService.findTeamsbyProjectIdAndMemberId(selectedProjectIdInDropdownList, utilities.getLoggedInMember().getMemberId());
		
		if (!allTeamsForDropdownList.isEmpty()) {
			selectedTeamIdInDropdownList = allTeamsForDropdownList.get(0).getTeamId();
		}
		
		nextSprintOfTeam();
		loadRemainingTasks();
	}


	public long getSelectedProjectIdInDropdownList() {
		return selectedProjectIdInDropdownList;
	}

	public void setSelectedProjectIdInDropdownList(long selectedProjectIdInDropdownList) {
		this.selectedProjectIdInDropdownList = selectedProjectIdInDropdownList;
	}

	public List<Project> getAllProjectsForDropdownList() {
		return allProjectsForDropdownList;
	}

	public List<Team> getAllTeamsForDropdownList() {
		return allTeamsForDropdownList;
	}
	
	public long getSelectedTeamIdInDropdownList() {
		return selectedTeamIdInDropdownList;
	}

	public void setSelectedTeamIdInDropdownList(long selectedTeamIdInDropdownList) {
		this.selectedTeamIdInDropdownList = selectedTeamIdInDropdownList;
	}

	public String getMsgConfirmCopyTaskToNextSprint() {
		return msgConfirmCopyTaskToNextSprint;
	}

	public void setMsgConfirmCopyTaskToNextSprint(
			String msgConfirmCopyTaskToNextSprint) {
		this.msgConfirmCopyTaskToNextSprint = msgConfirmCopyTaskToNextSprint;
	}

	public String getMsgInform() {
		return msgInform;
	}

	public void setMsgInform(String msgInform) {
		this.msgInform = msgInform;
	}

	public Sprint getNextSprint() {
		return nextSprint;
	}

	public void setNextSprint(Sprint nextSprint) {
		this.nextSprint = nextSprint;
	}
	
	public long getSelectedTaskId() {
		return selectedTaskId;
	}

	public void setSelectedTaskId(long selectedTaskId) {
		this.selectedTaskId = selectedTaskId;
	}

	public String getMsgConfirmRemoveTaskFromNextSprint() {
		return msgConfirmRemoveTaskFromNextSprint;
	}

	public void setMsgConfirmRemoveTaskFromNextSprint(
			String msgConfirmRemoveTaskFromNextSprint) {
		this.msgConfirmRemoveTaskFromNextSprint = msgConfirmRemoveTaskFromNextSprint;
	}

	public int getTotalTask() {
		return totalTask;
	}

	public void setTotalTask(int totalTask) {
		this.totalTask = totalTask;
	}

	public List<Issue> getRemainingTasks() {
		return remainingTasks;
	}

	public void setRemainingTasks(List<Issue> remainingTasks) {
		this.remainingTasks = remainingTasks;
	}

	public List<Long> getOriginalIssueId() {
		return originalIssueId;
	}

	public void setOriginalIssueId(List<Long> originalIssueId) {
		this.originalIssueId = originalIssueId;
	}

	public List<Long> getSelectedIssueId() {
		return selectedIssueId;
	}

	public void setSelectedIssueId(List<Long> selectedIssueId) {
		this.selectedIssueId = selectedIssueId;
	}

	public String getAssignConfirmMessage() {
		return assignConfirmMessage;
	}
	
}
