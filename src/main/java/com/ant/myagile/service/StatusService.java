package com.ant.myagile.service;
import java.util.List;

import com.ant.myagile.model.Sprint;
import com.ant.myagile.model.Status;
import com.ant.myagile.model.Team;
public interface StatusService {
	 boolean save(Status status);	
	 boolean delete(Status status);	
	 boolean update(Status status);	
	 Status findStatusById(Long statusId);	 
	 Status findStatusStartBySprintId(long sprintId);
	 Status findStatusDoneBySprintId(long sprintId);
	 Status findStatusAcceptedBySprintId(long sprintId);
	 List<Status> findStatusBySprintId(long sprintId);
	 void updateIssueBySprintId(long sprintId, long statusId);
	 void createStatusTodoAndDoneForSprint(Team team, Sprint sprint);
	/**
	 * Copy previous setting of Kanban
	 * from oldSprint to newSprint
	 * @param team The team that the Kanban belongs to
	 * @param newSprint New sprint that copy to
	 * @param oldSprint old sprint that copy from
	 */
    void reuseKanbanSetting(Team team, Sprint newSprint, Sprint oldSprint);
    void deleteAllStatusInSprint(Sprint sprint);
    List<Status> getAllStatusBySprint(Sprint sprint);
}
