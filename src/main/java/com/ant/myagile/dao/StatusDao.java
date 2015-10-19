package com.ant.myagile.dao;

import java.util.List;

import com.ant.myagile.model.Sprint;
import com.ant.myagile.model.Status;

public interface StatusDao {

    boolean save(Status status);

    boolean delete(Status status);

    boolean update(Status status);

    Status findStatusById(Long statusId);

    Status findStatusStartBySprintId(long sprintId);

    Status findStatusDoneBySprintId(long sprintId);

    Status findStatusAcceptedBySprintId(long sprintId);

    /**
     * List all status belong to sprint and order by type: The first status has
     * type START Status has type IN_PROGRESS Status has type DONE The last
     * status has type ACCEPTED_HIDE or ACCEPTED_SHOW
     * 
     * @param sprintId
     * @return
     */
    List<Status> findStatusBySprintId(long sprintId);

    /**
     * Update status for issue when delete status which issue in
     * 
     * @param sprintId
     * @param statusId
     */
    void updateIssueBySprintId(long sprintId, long statusId);
    
    List<Status> getAllStatusBySprint(Sprint sprint);
}
