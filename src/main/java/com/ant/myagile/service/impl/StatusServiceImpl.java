package com.ant.myagile.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ant.myagile.dao.StatusDao;
import com.ant.myagile.model.Sprint;
import com.ant.myagile.model.Status;
import com.ant.myagile.model.Team;
import com.ant.myagile.service.StatusService;

@Service
@Transactional
public class StatusServiceImpl implements StatusService {
	@Autowired
	private StatusDao statusDao;

	@Override
	public boolean save(Status status) {
		return statusDao.save(status);		
	}

	@Override
	public boolean delete(Status status) {
		return statusDao.delete(status);		
	}

	@Override
	public boolean update(Status status) {
		return statusDao.update(status);		
	}
	
	@Override
	public Status findStatusById(Long statusId) {
		return statusDao.findStatusById(statusId);
	}

	@Override
	public List<Status> findStatusBySprintId(long sprintId) {
		return statusDao.findStatusBySprintId(sprintId);
	}

	@Override
	public void updateIssueBySprintId(long sprintId, long statusId) {
		statusDao.updateIssueBySprintId(sprintId, statusId);
		
	}
	
	@Override
	public Status findStatusStartBySprintId(long sprintId) {
		return statusDao.findStatusStartBySprintId(sprintId);
	}

	@Override
	public Status findStatusDoneBySprintId(long sprintId) {
		return statusDao.findStatusDoneBySprintId(sprintId);
	}

	@Override
	public Status findStatusAcceptedBySprintId(long sprintId) {
		return statusDao.findStatusAcceptedBySprintId(sprintId);
	}
	
	@Override
	public void createStatusTodoAndDoneForSprint(Team team, Sprint sprint) {
		Status todo = new Status();
		todo.setName("To do");
		todo.setType(Status.StatusType.START);
		todo.setSprint(sprint);
		
		Status progress = new Status();
		progress.setName("In progress");
		progress.setType(Status.StatusType.IN_PROGRESS);
		progress.setSprint(sprint);
		
		Status done = new Status();
		done.setName("Done");
		done.setType(Status.StatusType.DONE);
		done.setSprint(sprint);
		
		Status accepted = new Status();
		accepted.setName("Accepted");
		accepted.setType(Status.StatusType.ACCEPTED_HIDE);
		accepted.setSprint(sprint);
		
		statusDao.save(todo);
		statusDao.save(progress);
		statusDao.save(done);
		statusDao.save(accepted);
	}
	
	@Override
    public void reuseKanbanSetting(Team team, Sprint newSprint, Sprint oldSprint) {
        List<Status> oldStatus = this.findStatusBySprintId(oldSprint.getSprintId());
        for (Status status : oldStatus) {
            Status newStatus = new Status();
            newStatus.setColor(status.getColor());
            if (status.getType() == Status.StatusType.ACCEPTED_SHOW) {
            	newStatus.setType(Status.StatusType.ACCEPTED_HIDE);
            } else
            	newStatus.setType(status.getType());
            newStatus.setName(status.getName());
            newStatus.setSprint(newSprint);
            statusDao.save(newStatus);
        }
    }

	@Override
	public void deleteAllStatusInSprint(Sprint sprint) {
		List<Status> statusList = statusDao.getAllStatusBySprint(sprint);
		if(statusList.size() > 0){
			for (Status status : statusList) {
				statusDao.delete(status);
			}
		}
		
	}

	@Override
	public List<Status> getAllStatusBySprint(Sprint sprint) {
		return statusDao.getAllStatusBySprint(sprint);
	}
}
