package com.ant.myagile.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ant.myagile.dao.RetrospectiveResultDao;
import com.ant.myagile.model.Project;
import com.ant.myagile.model.RetrospectiveResult;
import com.ant.myagile.model.Sprint;
import com.ant.myagile.model.Team;
import com.ant.myagile.service.RetrospectiveResultService;

@Service("retrospectiveResultService")
@Transactional
public class RetrospectiveResultServiceImpl implements RetrospectiveResultService {
	
	@Autowired
	private RetrospectiveResultDao retrospectiveResultDao;
	@Override
	public List<RetrospectiveResult> findAllRetrospective() {	
		return retrospectiveResultDao.findAllRetrospective();		
	}
	@Override
	public boolean addRetrospective(RetrospectiveResult retro) {
		return retrospectiveResultDao.addRetrospective(retro);
	}
	@Override
	public List<RetrospectiveResult> findRetrospectiveByTeamAndProject(
			Team team, Project project) {
		return retrospectiveResultDao.findRetrospectiveByTeamAndProject(team, project);		
	}
	@Override
	public boolean updateRetrospective(RetrospectiveResult retro) {
		return retrospectiveResultDao.updateRetrospective(retro);
	}
	@Override
	public boolean updateStatus(RetrospectiveResult retro) {
		return retrospectiveResultDao.updateStatus(retro);
	}
	@Override
	public RetrospectiveResult getRetrospectivetroById(long id) {
		return retrospectiveResultDao.getRetrospectiveById(id);
	}

	@Override
	public boolean deleteRetrospective(Long id) {
		return retrospectiveResultDao.deleteRetrospective(id);
	}
	@Override
	public List<RetrospectiveResult> getRetrospectiveResultsBySprint(Sprint sprint) {
		
		return retrospectiveResultDao.getRetrospectiveResultBySprint(sprint);
	}
	@Override
	public void deleteAllRetrospectiveResultsInSprint(Sprint sprint) {
		List<RetrospectiveResult> retrospectiveResults = retrospectiveResultDao.getRetrospectiveResultBySprint(sprint);
		if(retrospectiveResults.size() > 0){
			for (RetrospectiveResult retrospectiveResult : retrospectiveResults) {
				retrospectiveResultDao.deleteRetrospective(retrospectiveResult.getId());
			}
		}
		
	}
}
