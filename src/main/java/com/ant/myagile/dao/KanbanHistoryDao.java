package com.ant.myagile.dao;

import java.util.Date;
import java.util.List;

import com.ant.myagile.model.KanbanHistory;
import com.ant.myagile.model.KanbanIssue;

public interface KanbanHistoryDao {
    boolean save(KanbanHistory kanbanHistory);
    boolean update(KanbanHistory kanbanHistory);
    boolean delete(KanbanHistory kanbanHistory);
	List<KanbanHistory> getByKanbanIssue(KanbanIssue kanbanIssue);
	List<KanbanHistory> findByTeamAndFilter(long teamId, int start, int endrow,
			Date dateStart, Date dateEnd);
	List<Date> findDateRangeOfKanbanIssueHistoryByTeamId(long teamId);
}
