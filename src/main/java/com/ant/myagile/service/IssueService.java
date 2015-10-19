package com.ant.myagile.service;

import java.util.List;

import org.omnifaces.model.tree.TreeModel;

import com.ant.myagile.dto.IssueStateLazyLoading;
import com.ant.myagile.dto.StateLazyLoading;
import com.ant.myagile.model.Issue;
import com.ant.myagile.model.Member;
import com.ant.myagile.model.Sprint;
import com.ant.myagile.model.Status;
import com.ant.myagile.model.UserStory;

public interface IssueService {
	
	void deleteIssue(Issue issue);
	void updateStatusForIssue(long statusId, long issueId);
	void updatePointRemainWhenDone(Issue issue);
	void updatePointRemain(String pointRemain, Long issueId);
	void updateStatusOfIssueParent(Issue issue);
	boolean saveIssue(Issue issue);
	boolean updateIssue(Issue issue);
	long saveIssueAndGetId(Issue issue);
	boolean assignMemberToIssue(Issue issue, Member member);
	boolean checkIssuesBelongToWithUserStory(Long userStory);
	boolean isParentAndTotalChildrenPointEqual(Issue parent);
	boolean checkStatusDoneOfIssue(Issue issue);
	String findCurrentPointRemainOfIssue(Issue issue);
		
	/**
	 * Check point of parent issue is conflict with point of all child issue
	 * @param issue
	 * @return <strong>boolean</strong>
	 */
	boolean showPointConflictWarning(Issue issue);
	Issue findIssueById(long issueId);
	List<Issue> findIssueBySprintId(long sprintId);
	List<Issue> findIssueByStatus(Status status);
	List<Issue> findIssueByTeamAndProject(Long teamId, Long projectId);
	List<Issue> findIssueByTeamAndSprint(Long teamId, Long sprintId);
	List<Issue> findIssueByParent(Issue parent);
	List<Issue> findIssuesParent(long projectId, long sprintId);
	List<Issue> findIssuesNotParent(long sprintId);
	List<Issue> findIssuesIsTaskBySprintId(long sprintId);
	List<Issue> findIssuesByTypeAndSprint(long sprintId, String type);

    /**
     * Find issues have no children and parent
     * @param sprintId  Find issues depend on sprint
     * @return          List of Issues have no children and parent
     */
	List<Issue> findIssuesSingle(long sprintId);
	
	/**
	 * Filter issues by type, status and priority
	 * @param originalIssueList
	 * @param filterIssueType
	 * @param filterIssueStatus
	 * @param filterIssuePriority
	 * @return Filtered Issue List
	 */
	List<Issue> filterIssues(List<Issue> originalIssueList, String filterIssueType, String filterIssueStatus, String filterIssuePriority);	
	List<Issue> findIssuesByUserStory(Long userStoryId);

	/**
	 * Find all issue that is not parent
	 * @param userStoryId
	 * @return
	 */
	List<Issue> findNoChidrenIssuesByUserStory(Long userStoryId);
	
	/**
	 * Get total point of issue which have child
	 * @param issue
	 * @return <strong>String</strong> total point (DT format or NUMBER format)
	 */
	String getTotalPoint(Issue issue);
	
	/**
	 * Calculate progress of issue which don't have child
	 * @param issue
	 * @return <strong>int</strong> progress of issue
	 */
	int calculateProgress(Issue issue);	
	
	
	/**
	 * Calculate progress for issue have child and issue don't have child
	 * @param issueID
	 * @return <strong>int</strong> progress of issue
	 */
	int findProgressOfIssue(long issueID);
	
	/**
	 * Calculate total point from a list of issues
	 * @param issues	List of issue to calculate
	 * @return			Total point
	 */
	float calculateIssuesTotalPoints(List<Issue> issues);
	
	/**
	 * Calculate total point delivered from a list of issues
	 * based on Accepted column
	 * @param issues	List of issue to calculate
	 * @return			Total point delivered
	 */
	float calculateIssuesPointDelivered(List<Issue> issues);

	/**
	 * Calculate number of userstories delivered from a list of issues
	 * based on Accepted column
	 * @param issues
	 * @return
	 */
	int calculateUserStoryDelivered(List<Issue> issues);

	/**
	 * Find total remain point of issue which have child issue or not
	 * <hr>
	 * 1. If input issue is <b>not a parent issue</b>. This function will return the
	 * total point of that issue. 
	 * <br>
	 * Ex: if issue's remain point is D4T2 will return 6.0
	 * <br>
	 * 2. If input issue is a <b>parent issue</b>. This function will calculate 
	 * the sum of all sub issues belong to that issue
	 * <br>
	 * Ex: if issue has two sub-issues which have remain point are: D2T3, D3T1
	 * this function will return 9.0
	 * <hr>
	 * Notice: All issues that 
	 * @param issue
	 * @return <strong>float</strong>
	 */
	float findTotalRemainPoint(Issue issue);
	
	/**
	 * Find total estimate point of issue which have child issue or not
	 * <hr>
     * 1. If input issue is <b>not a parent issue</b>. This function will return the
     * total point of that issue. 
     * <br>
     * Ex: if issue's estimate point is D4T2 will return 6.0
     * <br>
     * 2. If input issue is a <b>parent issue</b>. This function will calculate 
     * the sum of all sub issues belong to that issue
     * <br>
     * Ex: if issue has two sub-issues which have estimate point are: D2T3, D3T1
     * this function will return 9.0
     * 
	 * @param issue
	 * @return <strong>float</strong>
	 */
	float findTotalEstimatePoint(Issue issue);	
	String findStatusOfIssue(Issue issue);	
	
	/**
	 * Calculate total estimate point all of issue in issue list
	 * @param issueList
	 * @return <strong>String</strong> DT format or NUMBER format
	 */
	String countEstimatePoints(List<Issue> issueList);
	
	/**
	 * Update Estimate point base on point format point can be in two format:
	 * DOT0 or number only
	 * 
	 * @param issue
	 * @param point
	 */
	 void updateEstimatePoint(Issue issue, String point);

	/**
	 * Check if issue has history of point remain or not
	 * 
	 * @param issue
	 * @return
	 */
	 boolean existHistoryOfPointRemain(Issue issue);

	 Issue findRootIssueBySubIssue(Issue issue);
	 
	 /**
	 * Create relationship tree from root issue
	 * @param rootIssue
	 * @return <strong>TreeModel</strong> 
	 */
	TreeModel<Issue> buildIssueRelationshipTree(Issue rootIssue);	
	
	/**
	 * Calculate total progress of parent issue depends on child issues in current sprint.
	 * 
	 * @param issue
	 * @return progress percentage
	 */
	 float calculateProgressForParentIssueTask(Issue issue);	
	
	/**
	 * Calculate total progress of parent issue when total estimate points of parent issue is equal to total estimate points of child issues.
	 * Note: This method should be used with calculateProgressForParentIssueTask() method.
	 * 
	 * @param totalRemainPointOfChildIssues
	 * @param totalPointOfParentIssue
	 * @param hasIssueWithPoint
	 * @param totalTaskWithDoneStatus
	 * @param totalTask
	 * @return progress percentage
	 */
	float calculateProgressWhenParentEqualChilds(
			float totalRemainPointOfChildIssues, float totalPointOfParentIssue,
			boolean hasIssueWithPoint, int totalTaskWithDoneStatus,
			int totalTask);
	
	/**
	 * Calculate total progress of parent issue when total estimate points of parent issue is NOT equal to total estimate points of child issues.
	 * Note: This method should be used with calculateProgressForParentIssueTask() method.
	 * 
	 * @param totalRemainPointOfChildIssues
	 * @param totalEstimatePointOfChildIssues
	 * @param hasIssueWithPoint
	 * @param totalTaskWithDoneStatus
	 * @param totalTask
	 * @return progress percentage
	 */
	float calculateProgressWhenParentNotEqualChilds(
			float totalRemainPointOfChildIssues,
			float totalEstimatePointOfChildIssues, boolean hasIssueWithPoint,
			int totalTaskWithDoneStatus, int totalTask);
	
	
	boolean isDTFormat(String point);	
	boolean isNumberFormat(String point);	
	String converterToDTFormat(String point);	
	String converterNumberToDTFormat(String point);
	String getPointByFormat(String point, String format);
	/**
	 * @param point
	 * @return format of point (DT or NUMBER)
	 */
	String checkingPointFormat(String point);
	
	/**
	 * Get remain point of Issue
	 * @param remainPoint
	 * @return <strong>DT</strong> format if point format is DT </br>
	 * 			<strong>number</strong> if point format id NUMBER
	 */
	String checkingRemainPoint(String remainPoint);
	
	/**
	 * Get estimate point of Issue
	 * @param estimatePoint
	 * @return <strong>DT</strong> format if point format is DT </br>
	 * 			<strong>number</strong> if point format id NUMBER
	 */
	String checkingEstimatePoint(String estimatePoint);

	/**
     * Copy some common attributes to new issue 
     * @param issue
     * @return
     */
    Issue copyCommonIssueInformation(Issue issue);

	/**
	 * Copy issue to START status
	 * in a specific sprint
	 * @param issueId issue to be copied
	 * @param sprintId
	 */
	void copyIssueToSprint(long issueId, long sprintId);

	/**
	 * Find the parent issue that map to a userstory in a 
	 * specific sprint
	 * @param userStoryId
	 * @param sprintId
	 * @return
	 */
    Issue findIssueByUserStoryAndSprint(long userStoryId, long sprintId);
    
	String countRemainPoints(List<Issue> issueList);
    
    void deleteAllIssueInSprint(Sprint sprint);
    List<Issue> findParentIssuesBySprintAndExceptParentThis(long issueId,long sprintId);
    
    public List<Issue> findChildrenIssuesOfRemainingParent(long parentId);
    public List<Issue> findRemainingTasksByProjectAndTeam(long projectId, long teamId);
    boolean checkIssueParentDoneBySubject(String subject);
    boolean checkIssueChildDoneBySubject(String subject);
    boolean checkSubIssueInSprintBySubject(Issue issue,Sprint sprint);
    public Issue findNewestIssueAvailableBySubjectNotInSprint(Issue issue,
			Sprint exceptSprint);
    public Issue findIssueByUserStoryAndSprintAndCurrentIssue(
    		Issue currentIssue,Sprint sprint);
	public Issue findOldestTaskOfUserStory(Long userStoryId);
	public Issue findNewestTaskOfUserStory(Long userStoryId);
	public Sprint findSprintHasContinuousIssue(Issue issue);
	public List<Sprint> getAllSprintBeforeByIssue(Issue issue);
	public List<Sprint> getAllSprintAfterByIssue(Issue issue);
	List<Issue> loadLazyIssuesBySprintId(Long sprintId,
			IssueStateLazyLoading issueStateLazyLoading);
	List<Issue> findLazyIssuesParentByProjectIdAndSprintId(long projectId,
			long sprintId, StateLazyLoading lazyLoadingIsuseList);
	List<Issue> findLazyIssueBySprintId(long sprintId,
			StateLazyLoading lazyLoadingSprintBacklogs);
	int countTotalLazyIssueBySprintId(long sprintId,
			StateLazyLoading lazyLoadingSprintBacklogs);
	int countTotalLazyIssuesParentByProjectIdAndSprintId(long projectId,
			long sprintId, StateLazyLoading lazyLoadingIsuseList);
	int countTotalLazyIssuesBySprintId(Long sprintId,
			IssueStateLazyLoading issueStateLazyLoading);
	List<Issue> findAllLazyIssuesBySprint(long sprintId);
	boolean updateOrderIssue(String[] arryIdIssueOrder,
			long idStatusFromClient, boolean swimline);
	List<Issue> getIssueOrderByStatus(long statusId);
	List<Issue> getIssueOrderByStatusAndSwimline(long statusId);
	List<Issue> getIssueOrderByStatusAndNotInSwimline(long statusId);
	boolean updateAllIssueByStatusUserStoryInLastSprint(UserStory currentUserStory, Long currentTeamId);
	boolean canChangeToAccepted(Issue issueTemp, long idStatusFromClient);
	List<Issue> allIssueLazyIssuesBySprintId(Long sprintId,
			IssueStateLazyLoading issueStateLazyLoading);
	long getIdOfUserStory(long issueId);
//	void deleteAllUnexpiredIssuesWhenSetUserStoryVoid(Long userStoryId);
	void setVoidAllUnexpiredIssuesWhenSetUserStoryVoid(Long userStoryId);
	void destroyVoidAllUnexpiredIssuesWhenDestroyVoidUserStory(Long userStoryId);
	List<Issue> findAllUnexpiredChildrenIssuesByUserStory(Long userStoryId);
	
}