package com.ant.myagile.service;
import java.util.List;

import com.ant.myagile.model.KanbanSwimline;
import com.ant.myagile.model.Team;
public interface KanbanSwimlineService {
	KanbanSwimline getById(long id);
	boolean update(KanbanSwimline kanbanSwimline);
	long add(KanbanSwimline kanbanSwimline);
	boolean delete(long id);
	
	List<KanbanSwimline> findKanbanSwimlineByTeamId(long id);
	void updateIssueByTeamId(Long teamId, Long swimlineId);
	void createSwimlineForNewTeam(Team team);
	void deleteAllSwimlineInTeam(Team team);
}
