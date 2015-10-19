package com.ant.myagile.service;

import java.util.Date;
import java.util.List;

import com.ant.myagile.model.Attachment;
import com.ant.myagile.model.KanbanHistory;
import com.ant.myagile.model.KanbanIssue;
import com.ant.myagile.model.Member;
import com.ant.myagile.model.MemberIssue;

public interface KanbanHistoryService {
	boolean save(KanbanHistory kanbanHistory);

	boolean update(KanbanHistory kanbanHistory);

	boolean delete(KanbanHistory kanbanHistory);

	void historyForAddKanbanIssue(KanbanIssue addTask);

	void historyForDeleteKanbanIssue(KanbanIssue kanbanIssueDelete);
	
	/**
	 * add history when update subject, description, not, status
	 * @param kanbanIssueBeforeUpdate
	 * @param kanbanIssueAfterUpdate
	 * @param oldAssignees 
	 */
	void historyForEditKanbanIssue(KanbanIssue kanbanIssueBeforeUpdate,
			KanbanIssue kanbanIssueAfterUpdate, List<Member> oldAssignees);
	/**
	 * add history when update estimate point
	 * @param kanbanIssueBeforeUpdate
	 * @param kanbanIssueÀterUpdate
	 */
	void historyForEstimateKanbanIssue(KanbanIssue kanbanIssueBeforeUpdate, KanbanIssue kanbanIssueAfterUpdate);

	void historyForRemainPointKanbanIssue(KanbanIssue kanbanIssueBeforeUpdate,
			KanbanIssue kanbanIssueAfterUpdate);

	void historyForAssignMemberForKanbanIssue(Member member,
			KanbanIssue kanbanIssue);

	void historyForUnAssignMemberForKanbanIssue(Member member,
			KanbanIssue kanbanIssueById);

	List<KanbanHistory> getByKanbanIssue(KanbanIssue kanbanIssue);

	List<KanbanHistory> findByTeamAndFilter(long teamId,
			int start, int endrow,
			Date dateStart,
			Date dateEnd);

	List<Date> findDateRangeOfKanbanIssueHistoryByTeamId(long teamId);

	void historyForAddAttachment(KanbanIssue kanbanIssue,
			Attachment newAttachment);

	void historyForDeleteAttachment(KanbanIssue kanbanIssue,
			Attachment attachment);

}
