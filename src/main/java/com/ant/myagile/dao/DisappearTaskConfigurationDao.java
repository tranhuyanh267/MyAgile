package com.ant.myagile.dao;

import com.ant.myagile.model.DisappearTaskConfiguration;
import com.ant.myagile.model.KanbanStatus;

public interface DisappearTaskConfigurationDao {

	
	DisappearTaskConfiguration getById(long id);
	boolean update(DisappearTaskConfiguration disappearTaskConfiguration);
	long add(DisappearTaskConfiguration disappearTaskConfiguration);
	boolean delete(long id);
	
	DisappearTaskConfiguration getDisappearTaskConfigurationByKanbanStatusId(Long disappearTaskConfigurationByKanbanStatusId);
}
