package com.ant.myagile.service;

import com.ant.myagile.model.DisappearTaskConfiguration;
import com.ant.myagile.model.KanbanStatus;

public interface DisappearTaskConfigurationService {
	DisappearTaskConfiguration getById(long id);
	boolean update(DisappearTaskConfiguration disappearTaskConfiguration);
	long add(DisappearTaskConfiguration disappearTaskConfiguration);
	boolean delete(long id);
	DisappearTaskConfiguration getDisappearTaskConfigurationByKanbanStatusId(Long disappearTaskConfigurationByKanbanStatusId);
	void createDisappearTaskConfigurationForAcceptedStatusOfTeam(
			KanbanStatus acceptedStatus);
}
