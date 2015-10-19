package com.ant.myagile.dao;

import java.util.List;

import com.ant.myagile.model.Issue;
import com.ant.myagile.model.KanbanIssue;
import com.ant.myagile.model.KanbanStatus;
import com.ant.myagile.model.Status;
import com.ant.myagile.model.Team;
import com.ant.myagile.model.UserStory;

public interface KanbanStatusDao {
	
	KanbanStatus getById(long id);
	boolean update(KanbanStatus kanbanStatus);
	long add(KanbanStatus kanbanStatus);
	boolean delete(long id);
	
	List<KanbanStatus> findKanbanStatusByTeamId(long id);

	KanbanStatus findToDoStatusByTeamId(long sprintId);
	
	KanbanStatus findDoneStatusByTeamId(long sprintId);
	
	KanbanStatus findAcceptedStatusByTeamId(long sprintId);
	
	List<KanbanIssue> getAllIssueByKanbanStatus(long idTeam,long kanbanStatusId,long kanbanSwimlineId);
	
	/**
	 * @param kanbanStatusId
	 * @param kanbanSwimlineId
	 * @return all issue in colum progress 
	 */
	List<KanbanIssue> getAllIssueChildProgressByKanbanStatus(long idTeam,long kanbanStatusId,long kanbanSwimlineId);
	List<KanbanIssue> getAllIssueChildDoneByKanbanStatus(long idTeam,long kanbanStatusId,long kanbanSwimlineId);
		
	void updateIssueByTeamId(Long teamId, Long statusId);
	List<KanbanStatus> getAllKanbanStatusByTeam(Team team);
	List<KanbanIssue> getAcceptedIssueNotDisappear(long teamId, long kanbanStatusId,
			long kanbanSwimlineId);
	KanbanStatus findAcceptedStatusShowByTeamId(Long teamId);
	KanbanStatus getKanbanStatusDoneByTeamId(Long teamId);
	KanbanStatus getKanbanStatusStartByTeamId(Long teamId);
}
