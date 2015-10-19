package com.ant.myagile.dao;

import java.util.List;

import com.ant.myagile.dto.IssueStateLazyLoading;
import com.ant.myagile.dto.StateLazyLoading;
import com.ant.myagile.model.Issue;
import com.ant.myagile.model.Sprint;
import com.ant.myagile.model.Status;
import com.ant.myagile.model.UserStory;

public interface IssueDao {

    boolean saveIssue(Issue issue);

    boolean updateIssue(Issue issue);

    boolean checkIssuesBelongUserStory(Long userStoryId);

    void deleteIssue(Issue issue);
    
    long saveIssueAndGetId(Issue issue);

    Issue findIssueById(long issueId);

    List<Issue> findIssueBySprintId(long sprintId);

    List<Issue> findBugBySprintId(long string);

    List<Issue> findIssueByStatus(Status status);

    List<Issue> findIssueByTeamAndProject(Long teamId, Long projectId);

    List<Issue> findIssueByTeamAndSprint(Long teamId, Long sprintId);

    List<Issue> findIssueByParent(Issue parent);

    List<Issue> findIssuesParent(long projectId, long sprintId);

    List<Issue> findIssuesNotParent(long sprintId);

    List<Issue> findIssuesIsTaskBySprintId(long sprintId);

    List<Issue> findIssuesByTypeAndSprint(long sprintId, String type);

    List<Issue> findIssuesSingle(long sprintId);

    List<Issue> findIssuesByUserStory(Long userStoryId);

    /**
     * Find the parent issue that map to a userstory in a 
     * specific sprint
     * @param userStoryId
     * @param sprintId
     * @return
     */
    Issue findIssueByUserStoryAndSprint(long userStoryId, long sprintId);

    Issue findIssueParentByIssueChild(Issue issue);
    List<Issue> findParentIssuesBySprintAndExceptParentThis(long issueId,long sprintId);
    
    public List<Issue> findChildrenIssuesOfRemainingParent(long parentId);

	List<Issue> findRemainingTasksByProjectAndTeam(long projectId,
			long teamId);
	
	boolean checkIssueParentDoneBySubject(String subject);
	boolean checkIssueChildDoneBySubject(String subject);
	boolean checkSubIssueInSprintBySubject(Issue issue,Sprint sprint);
	Issue findNewestIssueAvailableBySubjectNotInSprint(Issue issue,Sprint exceptSprint);
	Issue findIssueByUserStoryAndSprintAndCurrentIssue(Issue currentIssue,Sprint sprint);

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

	List<Issue> getIssueOrderByStatus(long status);

	List<Issue> getIssueOrderByStatusAndSwimline(long statusId);

	List<Issue> getIssueOrderByStatusAndNotInSwimline(long statusId);
	
	/**
	 * 
	 * @param sprintId
	 * @param issueStateLazyLoading
	 * @return return all issue of sprint and with lazy filter
	 */
	List<Issue> allIssueLazyIssuesBySprintId(Long sprintId,
			IssueStateLazyLoading issueStateLazyLoading);

//	void deleteAllUnexpiredIssuesWhenSetUserStoryVoid(Long userStoryId);

	void setVoidAllUnexpiredIssuesWhenSetUserStoryVoid(Long userStoryId);

	void destroyVoidAllUnexpiredIssuesWhenDestroyVoidUserStory(Long userStoryId);

	List<Issue> findAllUnexpiredChildrenIssuesByUserStory(Long userStoryId);
}
