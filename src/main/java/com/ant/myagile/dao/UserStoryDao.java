package com.ant.myagile.dao;

import java.util.List;

import com.ant.myagile.dto.StateLazyLoading;
import com.ant.myagile.model.Issue;
import com.ant.myagile.model.UserStory;

public interface UserStoryDao {
    boolean create(UserStory userStory);

    boolean updateUserStory(UserStory userStory);

    boolean deleteUserStory(long userStoryId);

    UserStory checkExistUserStory(String name, long projectId);
    
    UserStory checkUserStoryForEdit(String name, long projectId, long userStoryID);

    UserStory findUserStoryById(long userStoryId);

    UserStory findLastUserStoryInProject(Long projectId);

    UserStory findUserStoryByIssue(Issue issue);

    List<UserStory> findAllUserStoryByProjectId(long projectId);

    List<UserStory> findUserStoriesByProjectId(Long projectId);
    
    List<UserStory> findAllUserStoryToDoByProjectId(long projectId);

    List<UserStory> findUserStoriesByCriteria(List<UserStory.StatusType> selectedFilter, long projectId, String sortFiedl, int typeOfSort, String searchedKeyWord);
    
    List<UserStory> findAllUserStoryToDoAndProgress(long idProject);
    
    public List<UserStory> findRemainingUserStoriesByProject(long projectId);

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

	List<UserStory> loadAllLazyUserstoryProjectId(long projectId,
			StateLazyLoading stateUserstoryList);

	List<UserStory> findAllUserStoryByIdList(List<Long> userStoryIdList);
}
