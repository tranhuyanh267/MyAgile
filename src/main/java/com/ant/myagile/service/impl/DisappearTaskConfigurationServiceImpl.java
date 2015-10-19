package com.ant.myagile.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ant.myagile.dao.DisappearTaskConfigurationDao;
import com.ant.myagile.model.DisappearTaskConfiguration;
import com.ant.myagile.model.KanbanStatus;
import com.ant.myagile.service.DisappearTaskConfigurationService;

@Service("disappearTaskConfigurationService")
public class DisappearTaskConfigurationServiceImpl implements DisappearTaskConfigurationService {
	
	private static int DEFAULT_HIDDEN_WEEK = 1;
	
	@Autowired
	DisappearTaskConfigurationDao disappearTaskConfigurationDao;

	@Override
	public DisappearTaskConfiguration getDisappearTaskConfigurationByKanbanStatusId(
			Long disappearTaskConfigurationByKanbanStatusId) {
		return disappearTaskConfigurationDao.getDisappearTaskConfigurationByKanbanStatusId(disappearTaskConfigurationByKanbanStatusId);
	}

	@Override
	public void createDisappearTaskConfigurationForAcceptedStatusOfTeam(
			KanbanStatus acceptedStatus) {
		DisappearTaskConfiguration disappearTaskConfiguration = new DisappearTaskConfiguration();
		disappearTaskConfiguration.setKanbanStatus(acceptedStatus);
		disappearTaskConfiguration.setWeek(DEFAULT_HIDDEN_WEEK);
		disappearTaskConfigurationDao.add(disappearTaskConfiguration);
	}

	@Override
	public DisappearTaskConfiguration getById(long id) {
		return disappearTaskConfigurationDao.getById(id);
	}

	@Override
	public boolean update(DisappearTaskConfiguration disappearTaskConfiguration) {
		return disappearTaskConfigurationDao.update(disappearTaskConfiguration);
	}

	@Override
	public long add(DisappearTaskConfiguration disappearTaskConfiguration) {
		return disappearTaskConfigurationDao.add(disappearTaskConfiguration);
	}

	@Override
	public boolean delete(long id) {
		return disappearTaskConfigurationDao.delete(id);
	}

}
