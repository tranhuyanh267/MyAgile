package com.ant.myagile.service;

import java.util.List;

import com.ant.myagile.model.Issue;
import com.ant.myagile.model.KanbanIssue;
import com.ant.myagile.model.Team;
import com.ant.myagile.model.UserStory;

public interface KanbanIssueService {
	boolean saveKanbanIssue(KanbanIssue issue);

    boolean updateKanbanIssue(KanbanIssue issue);

    void deleteKanbanIssue(KanbanIssue issue);
    
    long saveKanbanIssueAndGetId(KanbanIssue issue);

    KanbanIssue findKanbanIssueById(long issueId);

    List<KanbanIssue> findKanbanIssuesByUserStory(Long userStoryId);

    KanbanIssue findKanbanIssueParentByIssueChild(KanbanIssue issue);
    List<KanbanIssue> getListKanbanIssueByUserstory(long idUserstory);
    KanbanIssue checkDuplicate(String subject, long userStoryId, long id);
    List<KanbanIssue> getListKanbanIssueNotNullByUserstory(long idUserstory);
    boolean checkAllKanbanIssueDoneOfUserstory(long idUserstory);
    int calculateProgress(KanbanIssue kanbanIssue);
    int calculateProgressUSForKanbanissue(UserStory us);
    
    List<KanbanIssue> getAllKanbanIssuesByTeam(Team team);
     void deleteAllKanbanIssuesByTeam(Team team);

	boolean updateAllKanbanIssueByUserStoryStatusOfTeam(UserStory userStory,
			Team team);
	boolean existKanbanIssueByUserStoryAndSubject(UserStory userStory,
			String subject);

	void updateKanbanIssueByUserStoryAndSubjectAndIssue(UserStory userStory,
			String subject, Issue issue);

	void deleteAllKanbanIssueByUserStoryAndSubject(UserStory userStory,
			String subject);

	void deleteKanbanIssueByIssueId(Long issueId);

	void updateKanbanIssueByIssueId(Long issueId);

	List<KanbanIssue> getKanbanIssueProgressByUserStory(
			UserStory userStoryOfKanbanIssue);

	/**
	 * find kanban issue which is not subissue of user story (this kanban issue map 1-1 with user story)
	 * @param userStory
	 * @return
	 */
	KanbanIssue findKanbanIssueIsNotSubIssueOfUserStory(UserStory userStory);

	void deleteAllKanbanIssueByUserStoryID(Long userStoryId);

	void setVoidAllKanbanIssueWhenSetVoidUserStory(Long userStoryId);

	void destroyVoidAllKanbanIssueWhenDestroyVoidUserStory(Long userStoryId);
}
