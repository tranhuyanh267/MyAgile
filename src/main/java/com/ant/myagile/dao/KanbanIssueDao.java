package com.ant.myagile.dao;

import java.util.List;

import com.ant.myagile.model.Issue;
import com.ant.myagile.model.KanbanIssue;
import com.ant.myagile.model.Status;
import com.ant.myagile.model.Team;
import com.ant.myagile.model.UserStory;

public interface KanbanIssueDao {

    boolean saveKanbanIssue(KanbanIssue issue);

    boolean updateKanbanIssue(KanbanIssue issue);

    void deleteKanbanIssue(KanbanIssue issue);
    
    long saveKanbanIssueAndGetId(KanbanIssue issue);

    KanbanIssue findKanbanIssueById(long issueId);

    List<KanbanIssue> findKanbanIssuesByUserStory(Long userStoryId);

    KanbanIssue findKanbanIssueParentByIssueChild(KanbanIssue issue);
    List<KanbanIssue> getListKanbanIssueByUserstory(long idUserstory);
    List<KanbanIssue> getListKanbanIssueNotNullByUserstory(long idUserstory);
    KanbanIssue checkDuplicate(String subject, long userStoryId,long id);
    boolean checkAllKanbanIssueDoneOfUserstory(long idUserstory);
    int calculateProgressUSForKanbanissue(UserStory us);
    List<KanbanIssue> getAllKanbanIssuesByTeam(Team team);

	boolean existKanbanIssueByUserStoryAndSubject(UserStory userStory,
			String subject);
	/**
	 * 
	 * @param userStory
	 * @param subject
	 * @param issue
	 * update kanban issue has the same with new issue by subject and userstory
	 */
	void updateKanbanIssueByUserStoryAndSubjectAndIssue(UserStory userStory,
			String subject, Issue issue);
	/**
	 * 
	 * @param userStory
	 * @param subject
	 * delete all kanban issue by userstory and subject
	 */
	void deleteAllKanbanIssueByUserStoryAndSubject(UserStory userStory,
			String subject);
	
	void deleteKanbanIssueByIssueId(Long issueId);

	void updateKanbanIssueByIssueId(Long issueId);

	List<KanbanIssue> getKanbanIssueProgressByUserStory(
			UserStory userStoryOfKanbanIssue);

	KanbanIssue findKanbanIssueIsNotSubIssueOfUserStory(UserStory userStory);

	void deleteAllKanbanIssueByUserStoryID(Long userStoryId);

	void setVoidAllKanbanIssueWhenSetVoidUserStory(Long userStoryId);

	void setVoidAllKanbanIssue(List<KanbanIssue> listKanbanIssue);

	void destroyVoidAllKanbanIssue(List<KanbanIssue> listKanbanIssue);

	void destroyVoidAllKanbanIssueWhenDestroyVoidUserStory(Long userStoryId);
}
