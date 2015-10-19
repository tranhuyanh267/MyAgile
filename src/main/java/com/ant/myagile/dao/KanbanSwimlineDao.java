package com.ant.myagile.dao;

import java.util.List;

import com.ant.myagile.model.KanbanStatus;
import com.ant.myagile.model.KanbanSwimline;
import com.ant.myagile.model.Team;

public interface KanbanSwimlineDao {
	
	KanbanSwimline getById(long id);
	boolean update(KanbanSwimline kanbanSwimline);
	long add(KanbanSwimline kanbanSwimline);
	boolean delete(long id);
	
	List<KanbanSwimline> findKanbanSwimlineByTeamId(long idTeam);
	void updateIssueByTeamId(Long teamId, Long swimlineId);
	KanbanSwimline firstSwimlineByTeamId(long teamId);
	List<KanbanSwimline> getAllKanbanSwimlinesByTeam(Team team);
}
