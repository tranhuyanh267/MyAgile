package com.ant.myagile.dao.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.ant.myagile.dao.KanbanHistoryDao;
import com.ant.myagile.model.KanbanHistory;
import com.ant.myagile.model.KanbanIssue;

@Repository("KanbanHistoryDao")
@Transactional
public class KanbanHistoryDaoImpl implements KanbanHistoryDao {
	
	private static final Logger logger = Logger.getLogger(KanbanHistoryDaoImpl.class);
	
	@Autowired
	private SessionFactory sessionFactory;

	@Override
	public boolean save(KanbanHistory kanbanHistory) {
		try {
			this.sessionFactory.getCurrentSession().save(kanbanHistory);
			return true;
		} catch (HibernateException e) {
			e.printStackTrace();
			logger.error("error save ",e);
			return false;
		}
	}

	@Override
	public boolean update(KanbanHistory kanbanHistory) {
		try {
			this.sessionFactory.getCurrentSession().update(kanbanHistory);
			return true;
		} catch (HibernateException e) {
			e.printStackTrace();
			logger.error("error update ",e);
			return false;
		}
	}

	@Override
	public boolean delete(KanbanHistory kanbanHistory) {
		try {
			Session session = this.sessionFactory.getCurrentSession();
			session.delete(kanbanHistory);
			return true;
		} catch (HibernateException e) {
			e.printStackTrace();
			logger.error("error delete ",e);
			return false;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<KanbanHistory> getByKanbanIssue(KanbanIssue kanbanIssue) {
		try {
			return sessionFactory.getCurrentSession().createCriteria(KanbanHistory.class)
					.add(Restrictions.eq("containerId", kanbanIssue.getIssueId()))
					.add(Restrictions.eq("containerType", KanbanHistory.ContainerType.KanbanIssue))
					.addOrder(Order.desc("dateCreated"))
					.list();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("error getByKanbanIssue ",e);
			return new ArrayList<KanbanHistory>();
		}
		
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<KanbanHistory> findByTeamAndFilter(long teamId, int start,
			int endrow, Date dateStart, Date dateEnd) {
		try {
			String hql = "SELECT kh FROM KanbanHistory kh, KanbanIssue ki"
					+ " WHERE kh.containerType = :type"
					+ " AND kh.containerId = ki.issueId"
					+ " AND ki.team.teamId = :teamId"
					+ " AND kh.dateCreated >= :startDate"
					+ " AND kh.dateCreated <= :endDate"
					+ " ORDER BY kh.dateCreated DESC";
			List<KanbanHistory> kanbanHistories = sessionFactory.getCurrentSession().createQuery(hql)
					.setParameter("type", KanbanHistory.ContainerType.KanbanIssue)
					.setParameter("teamId", teamId)
					.setParameter("startDate", dateStart)
					.setParameter("endDate", dateEnd)
					.setFirstResult(start)
					.setMaxResults(endrow)
					.list();
			return kanbanHistories;
			
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("error findByTeamAndFilter ",e);
			return new ArrayList<KanbanHistory>();
		}
		
	}

	@Override
	public List<Date> findDateRangeOfKanbanIssueHistoryByTeamId(long teamId) {
		try {
			Session session = sessionFactory.getCurrentSession();
			List<Date> kanbanHistories = new ArrayList<Date>();
			String queryStatementDateEnd = "SELECT kh.dateCreated FROM KanbanHistory kh, KanbanIssue ki"
					+ " WHERE kh.containerType = :type"
					+ " AND kh.containerId = ki.issueId"
					+ " AND ki.team.teamId = :teamId"
					+ " ORDER BY kh.dateCreated DESC";
			
			String queryStatementDateStart = "SELECT kh.dateCreated FROM KanbanHistory kh, KanbanIssue ki"
					+ " WHERE kh.containerType = :type"
					+ " AND kh.containerId = ki.issueId"
					+ " AND ki.team.teamId = :teamId"
					+ " ORDER BY kh.dateCreated ASC";
			
			Query query = session.createQuery(queryStatementDateStart);
			query.setLong("teamId", teamId);
			query.setParameter("type", KanbanHistory.ContainerType.KanbanIssue);
			query.setFirstResult(0);
			query.setMaxResults(1);
			kanbanHistories = query.list();
			
			Query query1 = session.createQuery(queryStatementDateEnd);
			query1.setLong("teamId", teamId);
			query1.setParameter("type", KanbanHistory.ContainerType.KanbanIssue);
			query1.setFirstResult(0);
			query1.setMaxResults(1);
			List<Date> histories1 = query1.list();
			kanbanHistories.addAll(histories1);
			return kanbanHistories;
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("error findDateRangeOfKanbanIssueHistoryByTeamId ",e);
			return new ArrayList<Date>();
		}
	}

}
