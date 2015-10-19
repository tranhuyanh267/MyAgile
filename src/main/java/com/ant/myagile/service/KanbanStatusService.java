package com.ant.myagile.service;
import java.util.List;

import com.ant.myagile.model.Issue;
import com.ant.myagile.model.KanbanIssue;
import com.ant.myagile.model.KanbanStatus;
import com.ant.myagile.model.Sprint;
import com.ant.myagile.model.Status;
import com.ant.myagile.model.Team;
public interface KanbanStatusService {
	KanbanStatus getById(long id);

	boolean update(KanbanStatus kanbanStatus);

	long add(KanbanStatus kanbanStatus);

	boolean delete(long id);

	List<KanbanStatus> findKanbanStatusByTeamId(long id);

	KanbanStatus findToDoStatusByTeamId(long sprintId);

	KanbanStatus findDoneStatusByTeamId(long sprintId);

	KanbanStatus findAcceptedStatusByTeamId(long sprintId);

	void createStatusTodoAndDoneForTeam(Team team);
	
	List<KanbanIssue> getAllIssueByKanbanStatus(long idTeam,long kanbanStatusId,long kanbanSwimlineId);

	void updateIssueByTeamId(Long teamId, Long statusId);
	List<KanbanIssue> getAllIssueChildProgressByKanbanStatus(long idTeam,long kanbanStatusId,long kanbanSwimlineId);
	List<KanbanIssue> getAllIssueChildDoneByKanbanStatus(long idTeam,long kanbanStatusId,long kanbanSwimlineId);
	void deleteAllKanbanStatusInTeam(Team  team);

	List<KanbanIssue> getAcceptedIssueNotDisappear(long idTeam, long idKanbanStatus,
			long idKanbanSwimline);

	KanbanStatus findAcceptedStatusShowByTeamId(Long teamId);
	
}
