package com.ant.myagile.dao.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.ant.myagile.dao.HistoryDao;
import com.ant.myagile.model.History;

@Repository("HistoryDao")
@Transactional
public class HistoryDaoImpl implements HistoryDao 
{
    @Autowired
    private SessionFactory sessionFactory;
    
    @Override
    public boolean save(History history) {
	try 
	{
	    this.sessionFactory.getCurrentSession().save(history);
	    return true;
	} catch (HibernateException e) {
	    return false;
	}
    }

    @Override
    public boolean update(History history) {
	try {
	    this.sessionFactory.getCurrentSession().update(history);
	    return true;
	} catch (HibernateException e) {
	    return false;
	}
    }

    @Override
    public boolean delete(History history) {
	try {
	    Session session = this.sessionFactory.getCurrentSession();
	    session.delete(history);
	    return true;
	} catch (HibernateException e) {
	    return false;
	}
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<History> findHistoryByContainer(String containerType, long containerId) {
	try {
	    Criteria criteria = this.sessionFactory.getCurrentSession().createCriteria(History.class);
	    List<History> histories = new ArrayList<History>();
	    criteria.add(Restrictions.eq("containerType", containerType));
	    criteria.add(Restrictions.eq("containerId", containerId));
	    criteria.setCacheable(true);
	    criteria.addOrder(Order.desc("createdOn"));
	    criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
	    histories = criteria.list();
	    return histories;
	} catch (HibernateException e) {
	    return null;
	}
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<History> findHistoryByContainerWithNumberRow(String containerType, long containerId, int numRow) {
	try {
	    Criteria criteria = this.sessionFactory.getCurrentSession().createCriteria(History.class);
	    List<History> histories = new ArrayList<History>();
	    criteria.add(Restrictions.eq("containerType", containerType));
	    criteria.add(Restrictions.eq("containerId", containerId));
	    criteria.addOrder(Order.desc("createdOn"));
	    criteria.setCacheable(true);
	    criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
	    criteria.setFirstResult(numRow);
	    criteria.setMaxResults(numRow + 6);
	    histories = criteria.list();
	    return histories;
	} catch (HibernateException e) {
	    return null;
	}
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Object> findIssueHistoryBySprintWithStartAndEndRow(Long sprintId, int startRow, int endRow, Date dateStart, Date dateEnd) {
	try {
	    Session session = sessionFactory.getCurrentSession();
	    List<Object> histories = new ArrayList<Object>();
	    String queryStatement = "from History his, Issue iss where his.containerType ='Issue' and his.containerId = iss.id and " + "iss.sprint.sprintId = :sprintId " + "and his.createdOn >= :dateStart and his.createdOn <= :dateEnd order by his.createdOn desc ";
	    Query query = session.createQuery(queryStatement);
	    query.setLong("sprintId", sprintId);
	    if (endRow != 0) {
		query.setFirstResult(startRow);
		query.setMaxResults(endRow);
	    }
	    query.setTimestamp("dateStart", dateStart);
	    query.setTimestamp("dateEnd", dateEnd);
	    histories = query.list();
	    return histories;
	} catch (HibernateException e) {
	    return null;
	}
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Date> findDateRangeOfIssueHistoryBySprintId(Long sprintId) {
	Session session = sessionFactory.getCurrentSession();
	List<Date> histories = new ArrayList<Date>();
	String queryStatementDateEnd = "select his.createdOn from History his, Issue iss where his.containerType ='Issue' and his.containerId = iss.id and " + "iss.sprint.sprintId = :sprintId " + "order by his.createdOn desc";
	String queryStatementDateStart = "select his.createdOn from History his, Issue iss where his.containerType ='Issue' and his.containerId = iss.id and " + "iss.sprint.sprintId = :sprintId " + "order by his.createdOn asc";
	Query query = session.createQuery(queryStatementDateStart);
	query.setLong("sprintId", sprintId);
	query.setFirstResult(0);
	query.setMaxResults(1);
	histories = query.list();
	Query query1 = session.createQuery(queryStatementDateEnd);
	query1.setLong("sprintId", sprintId);
	query1.setFirstResult(0);
	query1.setMaxResults(1);
	List<Date> histories1 = query1.list();
	histories.addAll(histories1);
	return histories;
    }

}
