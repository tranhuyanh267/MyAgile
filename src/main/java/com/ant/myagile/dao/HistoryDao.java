package com.ant.myagile.dao;

import java.util.Date;
import java.util.List;

import com.ant.myagile.model.History;

public interface HistoryDao {
    boolean save(History history);

    boolean update(History history);

    boolean delete(History history);

    /**
     * Find History by Container (Container is issue or project or team)
     * 
     * @param containerType
     *            ISSUE_HISTORY, PROJECT_HISTORY, TEAM_HISTORY
     * @param containerId
     *            issueId or projectId or teamId
     * @return list of History
     */
    List<History> findHistoryByContainer(String containerType, long containerId);

    /**
     * Find History by container (Container is issue or project or team) with
     * number row
     * 
     * @param containerType
     *            ISSUE_HISTORY, PROJECT_HISTORY, TEAM_HISTORY
     * @param containerId
     *            issueId or projectId or teamId
     * @param numRow
     *            number row will find in list (e.g 5 rows or 10 rows)
     * @return list of History
     */
    List<History> findHistoryByContainerWithNumberRow(String containerType, long containerId, int numRow);

    /**
     * Find History by sprint with start row, end row, date start and date end
     * 
     * @param sprintId
     * @param startRow
     * @param endRow
     * @param dateStart
     * @param dateEnd
     * @return list of object
     */
    List<Object> findIssueHistoryBySprintWithStartAndEndRow(Long sprintId, int startRow, int endRow, Date dateStart, Date dateEnd);

    /**
     * Find first date and last date that have History by sprintId
     * 
     * @param sprintId
     * @return list of date (include first date and end date)
     */
    List<Date> findDateRangeOfIssueHistoryBySprintId(Long sprintId);
}
