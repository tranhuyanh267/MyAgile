package com.ant.myagile.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ant.myagile.dao.IssueDao;
import com.ant.myagile.dao.KanbanStatusDao;
import com.ant.myagile.dao.SprintDao;
import com.ant.myagile.dao.StatusDao;
import com.ant.myagile.model.Issue;
import com.ant.myagile.model.KanbanIssue;
import com.ant.myagile.model.KanbanStatus;
import com.ant.myagile.model.KanbanStatus.StatusType;
import com.ant.myagile.model.Sprint;
import com.ant.myagile.model.Team;
import com.ant.myagile.service.KanbanStatusService;
import com.ant.myagile.service.KanbanSwimlineService;

@Service
@Transactional
public class KanbanStatusServiceImpl implements KanbanStatusService {

	@Autowired
	KanbanStatusDao kanbanStatusDao;
	@Autowired 
	KanbanSwimlineService kanbanSwimlineService;
	@Autowired
	StatusDao statusDao;
	@Autowired
	IssueDao issueDao;
	@Autowired
	SprintDao sprintDao;
	
	@Override
	public KanbanStatus getById(long id) {
		return kanbanStatusDao.getById(id);
	}

	@Override
	public boolean update(KanbanStatus kanbanStatus) {
		return kanbanStatusDao.update(kanbanStatus);
	}

	@Override
	public long add(KanbanStatus kanbanStatus) {
		return kanbanStatusDao.add(kanbanStatus);
	}

	@Override
	public boolean delete(long id) {
		return kanbanStatusDao.delete(id);
	}

	@Override
	public List<KanbanStatus> findKanbanStatusByTeamId(long id) {
		return kanbanStatusDao.findKanbanStatusByTeamId(id);
	}

	@Override
	public KanbanStatus findToDoStatusByTeamId(long sprintId) {
		return kanbanStatusDao.findToDoStatusByTeamId(sprintId);
	}

	@Override
	public KanbanStatus findDoneStatusByTeamId(long sprintId) {
		return kanbanStatusDao.findDoneStatusByTeamId(sprintId);
	}

	@Override
	public KanbanStatus findAcceptedStatusByTeamId(long sprintId) {
		return kanbanStatusDao.findAcceptedStatusByTeamId(sprintId);
	}

	@Override
	public void createStatusTodoAndDoneForTeam(Team team) {
		KanbanStatus todo = new KanbanStatus();
		todo.setName("To do");
		todo.setColumnDone(false);
		todo.setTeam(team);
		todo.setColor(KanbanStatus.DEFAULT_COLOR);
		todo.setType(StatusType.START);
		todo.setWeightPoint(15);
		
		KanbanStatus progress = new KanbanStatus();
		progress.setName("In progress");
		progress.setTeam(team);
		progress.setColumnDone(false);
		progress.setType(StatusType.IN_PROGRESS);
		progress.setWeightPoint(15);
		progress.setColor(KanbanStatus.DEFAULT_COLOR);
		
		KanbanStatus done = new KanbanStatus();
		done.setName("Done");
		done.setTeam(team);
		done.setColumnDone(false);
		done.setType(StatusType.DONE);
		done.setWeightPoint(15);
		done.setColor(KanbanStatus.DEFAULT_COLOR);

		KanbanStatus accepted = new KanbanStatus();
		accepted.setName("Accepted");
		accepted.setTeam(team);
		accepted.setColumnDone(false);
		accepted.setType(StatusType.ACCEPTED_HIDE);
		accepted.setColor(KanbanStatus.DEFAULT_COLOR);

		kanbanStatusDao.add(todo);
		kanbanStatusDao.add(done);
		kanbanStatusDao.add(progress);
		kanbanStatusDao.add(accepted);
	}

	@Override
	public List<KanbanIssue> getAllIssueByKanbanStatus(long idTeam,
			long kanbanStatusId, long kanbanSwimlineId) {
		return kanbanStatusDao.getAllIssueByKanbanStatus(idTeam,
				kanbanStatusId, kanbanSwimlineId);
	}

	@Override
	public void updateIssueByTeamId(Long teamId, Long statusId) {
		kanbanStatusDao.updateIssueByTeamId(teamId, statusId);
	}

	@Override
	public List<KanbanIssue> getAllIssueChildProgressByKanbanStatus(long idTeam,
			long kanbanStatusId, long kanbanSwimlineId) {
		return kanbanStatusDao.getAllIssueChildProgressByKanbanStatus(idTeam,
				kanbanStatusId, kanbanSwimlineId);
	}

	@Override
	public List<KanbanIssue> getAllIssueChildDoneByKanbanStatus(long idTeam,
			long kanbanStatusId, long kanbanSwimlineId) {
		return kanbanStatusDao.getAllIssueChildDoneByKanbanStatus(idTeam,
				kanbanStatusId, kanbanSwimlineId);
	}

	@Override
	public void deleteAllKanbanStatusInTeam(Team team) {
		List<KanbanStatus> kanbanStatus = kanbanStatusDao.getAllKanbanStatusByTeam(team);
		if(kanbanStatus.size() > 0){
			
			for (KanbanStatus ks : kanbanStatus) {
				kanbanStatusDao.delete(ks.getStatusId());
			}
		}
		
	}

	@Override
	public List<KanbanIssue> getAcceptedIssueNotDisappear(long teamId,
			long kanbanStatusId, long kanbanSwimlineId) {
		return kanbanStatusDao.getAcceptedIssueNotDisappear(teamId,
				kanbanStatusId, kanbanSwimlineId);
	}

	@Override
	public KanbanStatus findAcceptedStatusShowByTeamId(Long teamId) {
		return kanbanStatusDao.findAcceptedStatusShowByTeamId(teamId);
	}
}
