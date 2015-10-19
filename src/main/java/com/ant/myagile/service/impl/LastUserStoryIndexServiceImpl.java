package com.ant.myagile.service.impl;

import java.io.Serializable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ant.myagile.dao.LastUserStoryIndexDao;
import com.ant.myagile.model.Project;
import com.ant.myagile.service.LastUserStoryIndexService;

@Service
@Transactional
public class LastUserStoryIndexServiceImpl implements LastUserStoryIndexService,Serializable{

	private static final long serialVersionUID = 1L;
	
	@Autowired
	private LastUserStoryIndexDao lastUSIndexDao;

	@Override
	public Long findLastUserStoryIndex(long projectId) {
		return lastUSIndexDao.findLastUserStoryIndex(projectId);
	}

	@Override
	public boolean updateIndex(Project project, Long sortId) {
		return lastUSIndexDao.updateIndex(project,sortId);
	}

}
