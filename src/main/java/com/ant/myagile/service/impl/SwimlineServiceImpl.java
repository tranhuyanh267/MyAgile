package com.ant.myagile.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ant.myagile.dao.SwimlineDao;
import com.ant.myagile.model.Sprint;
import com.ant.myagile.model.Swimline;
import com.ant.myagile.service.SwimlineService;

@Service
@Transactional
public class SwimlineServiceImpl implements SwimlineService{

	@Autowired
	SwimlineDao swimlineDao;
	
	@Override
	public long save(Swimline swimline) {
		return swimlineDao.save(swimline);
	}

	@Override
	public boolean delete(Swimline swimline) {
		return swimlineDao.delete(swimline);
	}

	@Override
	public boolean update(Swimline swimline) {
		return swimlineDao.update(swimline);
	}

	@Override
	public Swimline getSwimlineBySprint(long sprintId) {
		return swimlineDao.getSwimlineBySprint(sprintId);
	}

	@Override
	public List<Swimline> getAllSwimlineBySprint(Sprint sprint) {
		return swimlineDao.getAllSwimlineBySprint(sprint);
	}

	@Override
	public void deleteAllSwimlineInSprint(Sprint sprint) {
		List<Swimline> swimlines = swimlineDao.getAllSwimlineBySprint(sprint);
		if(swimlines.size() > 0){
			for (Swimline swimline : swimlines) {
				swimlineDao.delete(swimline);
			}
		}
		
	}

}
