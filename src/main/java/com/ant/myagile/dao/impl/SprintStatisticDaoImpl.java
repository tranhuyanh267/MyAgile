package com.ant.myagile.dao.impl;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.ant.myagile.dao.SprintStatisticDao;
import com.ant.myagile.model.Sprint;
import com.ant.myagile.model.SprintStatistic;

@Repository("SprintStatisticDao")
@Transactional
public class SprintStatisticDaoImpl implements SprintStatisticDao {

    @Autowired
    private SessionFactory sessionFactory;

    @Override
    public boolean save(SprintStatistic sprintStatistic) {
	try {
	    sessionFactory.getCurrentSession().save(sprintStatistic);
	    return true;
	} catch (HibernateException e) {
	    return false;
	}
    }

    @Override
    public boolean update(SprintStatistic sprintStatistic) {
	try {
	    sessionFactory.getCurrentSession().update(sprintStatistic);
	    return true;
	} catch (HibernateException e) {
	    return false;
	}
    }

    @Override
    public boolean delete(SprintStatistic sprintStatistic) {
	try {
	    Session session = sessionFactory.getCurrentSession();
	    session.delete(sprintStatistic);
	    return true;
	} catch (HibernateException e) {
	    return false;
	}
    }

    @Override
    public SprintStatistic findSprintStatisticBySprint(Sprint sprint) {
	SprintStatistic result = new SprintStatistic();
	try {
	    Criteria criteria = sessionFactory.getCurrentSession().createCriteria(SprintStatistic.class);
	    criteria.add(Restrictions.eq("sprint", sprint));
	    result = (SprintStatistic) criteria.uniqueResult();
	} catch (Exception e) {
	    return null;
	}
	return result;
    }

}
