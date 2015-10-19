package com.ant.myagile.service;

import java.util.List;

import com.ant.myagile.dto.StateLazyLoading;
import com.ant.myagile.model.Issue;
import com.ant.myagile.model.KanbanIssue;
import com.ant.myagile.model.Sprint;
import com.ant.myagile.model.UserStory;
import com.ant.myagile.model.UserStory.StatusType;

public interface UserStoryService {	 	 
	 boolean create(UserStory userStory);	 
	 boolean update(UserStory userStory);	 
	 boolean delete(long userStoryId);
	 UserStory checkExistUserStory(String name, long projectId);	 
	 UserStory findUserStoryById(long userStoryId);
	 UserStory findLastUserStoryInProject(Long projectId);
	 UserStory findUserStoryByIssue(Issue issue);
	 List<UserStory> findAllUserStoryByProjectId(long projectId);	 
	 List<UserStory> findUserStoriesByProjectId(Long projectId);
	 List<UserStory> findAllUserStoryToDoByProjectId(long projectId);
	 List<UserStory> findAllUserStoryByIdList(List<Long> userStoryIdList);
	 UserStory checkUserStoryForEdit(String name, long projectId,
				long userStoryID);

     /**
     * Find user story by criteria suck as filter, keyword and sorting the result
     * @param selectedFilter    Filter user story by status
     * @param sortField         Sort user story by field
     * @param typeOfSort        Sorting method: ASC or DESC
     * @param searchedKeyWord   Searching keyword
     * @return List of UserStory
     */
	 List<UserStory> findUserStoriesByCriteria(List<UserStory.StatusType> selectedFilter, long projectId, String sortField, int typeOfSort, String searchedKeyWord);

	 /**
	  * This function calculate the <b>progress bar</b> of <b>userStory</b>
	  * based on <b>all issues</b> related to that <b>userStory</b>
	  * 
	  * <h2>Notice:</h2> 
	  * 1. If <b>all issues</b> are is <b>Done</b> column. Progress will be <b>100%</b>
	  * <p>
	  * 2. All issues are <b>not done</b> in <b>sprint</b> 
	  * that is <b>closed<b> will be <b>ignored</b>
	  * 
	  * @param userStory
	  * @return The number that show the percentage done of that userStory
	  * (Ex. 60 show that this userStory is done 60%
	  */
	 int findProgressOfUserStory(UserStory userStory);

	 UserStory.StatusType findStatusOfUserStory(UserStory us);	 
     boolean createUserStoryFromEmail(UserStory userStory, String email);
     List<UserStory> findAllUserStoryToDoAndProgress(long idProject);
     public List<UserStory> findRemainingUserStoriesByProject(long projectId);
     
     /**
      * note: always set default filter and sort for StateLazyLoading object
      * @param projectId
      * @param stateUserstoryList
      * @return list userstory lazy
      */
	List<UserStory> loadLazyUserstoryProjectId(long projectId,
			StateLazyLoading stateUserstoryList);
	List<UserStory> findLazyUserStoryByProjectNotInSprint(long projectId,
			long sprintId, StateLazyLoading lazyLoadingProductBacklogs);
	List<UserStory> findLazyUserStoryToDoAndProgress(Long currentProjectId,
			StateLazyLoading lazyLoadingProductBacklogs);
	int countTotalRowloadLazyUserstoryProjectId(long projectId,
			StateLazyLoading stateUserstoryList);
	int countTotalLazyUserStoryByProjectNotInSprint(long projectId,
			long sprintId, StateLazyLoading lazyLoadingProductBacklogs);
	void updateAllIssueOfUserStoryHaveTheSameContent(UserStory us);
	int countTotalUserStoryToDoAndProgress(Long currentProjectId);
	void updateSubjectDescriptionNotePriorityOfAllIssueOfUserStory(
			UserStory userStoryOfIssue);
	StatusType findStatusOfUserStoryBySprintAndUserStory(UserStory userStory,
			Sprint sprint);
	List<UserStory> loadAllLazyUserstoryProjectId(long projectId,
			StateLazyLoading stateUserstoryList);
}
