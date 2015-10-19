package com.ant.myagile.dao.impl;

import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.ant.myagile.dao.DisappearTaskConfigurationDao;
import com.ant.myagile.model.DisappearTaskConfiguration;
import com.ant.myagile.model.KanbanStatus;

@Repository("disappearTaskConfigurationDao")
@Transactional
public class DisappearTaskConfigurationDaoImp implements DisappearTaskConfigurationDao {
	
	@Autowired
	private SessionFactory sessionFactory;
	
	@Override
	public DisappearTaskConfiguration getById(long id) {
		return (DisappearTaskConfiguration) sessionFactory.getCurrentSession().get(
				KanbanStatus.class, id);
	}

	@Override
	public boolean update(DisappearTaskConfiguration disappearTaskConfiguration) {
		try {
			sessionFactory.getCurrentSession().update(disappearTaskConfiguration);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public long add(DisappearTaskConfiguration disappearTaskConfiguration) {
		return (Long) sessionFactory.getCurrentSession().save(disappearTaskConfiguration);

	}

	@Override
	public boolean delete(long id) {
		try {
			sessionFactory.getCurrentSession().delete(getById(id));
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public DisappearTaskConfiguration getDisappearTaskConfigurationByKanbanStatusId(
			Long disappearTaskConfigurationByKanbanStatusId) {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(DisappearTaskConfiguration.class);
		criteria.add(Restrictions.eq("kanbanStatus.statusId", disappearTaskConfigurationByKanbanStatusId));
		DisappearTaskConfiguration disappearTaskConfiguration = new DisappearTaskConfiguration();
		disappearTaskConfiguration = (DisappearTaskConfiguration) criteria.uniqueResult();
		return disappearTaskConfiguration;
	}


}
