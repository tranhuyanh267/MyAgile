package com.ant.myagile.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ant.myagile.dao.KanbanStatusDao;
import com.ant.myagile.dao.KanbanSwimlineDao;
import com.ant.myagile.dao.SprintDao;
import com.ant.myagile.model.Issue;
import com.ant.myagile.model.KanbanStatus;
import com.ant.myagile.model.KanbanStatus.StatusType;
import com.ant.myagile.model.KanbanSwimline;
import com.ant.myagile.model.Sprint;
import com.ant.myagile.model.Swimline;
import com.ant.myagile.model.Team;
import com.ant.myagile.model.UserStory;
import com.ant.myagile.service.KanbanStatusService;
import com.ant.myagile.service.KanbanSwimlineService;

@Service
@Transactional
public class KanbanSwimlineServiceImpl implements KanbanSwimlineService {

	@Autowired
	KanbanSwimlineDao kanbanSwimlineDao;
	@Autowired
	SprintDao sprintDao;

	@Override
	public KanbanSwimline getById(long id) {
		return kanbanSwimlineDao.getById(id);
	}

	@Override
	public boolean update(KanbanSwimline kanbanSwimline) {
		return kanbanSwimlineDao.update(kanbanSwimline);
	}

	@Override
	public long add(KanbanSwimline kanbanSwimline) {
		return kanbanSwimlineDao.add(kanbanSwimline);
	}

	@Override
	public boolean delete(long id) {
		return kanbanSwimlineDao.delete(id);
	}

	@Override
	public List<KanbanSwimline> findKanbanSwimlineByTeamId(long id) {
		return kanbanSwimlineDao.findKanbanSwimlineByTeamId(id);
	}

	@Override
	public void updateIssueByTeamId(Long teamId, Long swimlineId) {
		kanbanSwimlineDao.updateIssueByTeamId(teamId, swimlineId);

	}

	public void createSwimlineForNewTeam(Team team) {
		KanbanSwimline defaultSwimline = new KanbanSwimline();
		defaultSwimline.setName("Default Swimline");
		defaultSwimline.setColor(KanbanSwimline.DEFAULT_COLOR);
		defaultSwimline.setTeam(team);
		kanbanSwimlineDao.add(defaultSwimline);
	}

	@Override
	public void deleteAllSwimlineInTeam(Team team) {
		List<KanbanSwimline> kanbanSwimlines = kanbanSwimlineDao.getAllKanbanSwimlinesByTeam(team);
		if(kanbanSwimlines.size()>0){
			for (KanbanSwimline kanbanSwimline : kanbanSwimlines) {
				kanbanSwimlineDao.delete(kanbanSwimline.getSwimlineId());
			}
		}
		
	}
}
