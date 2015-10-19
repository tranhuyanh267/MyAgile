package com.ant.myagile.dao.impl;

import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.ant.myagile.dao.HistoryFieldChangeDao;
import com.ant.myagile.model.History;
import com.ant.myagile.model.HistoryFieldChange;

@Repository("HistoryFieldChangeDao")
@Transactional
public class HistoryFieldChangeDaoImpl implements HistoryFieldChangeDao {
    @Autowired
    private SessionFactory sessionFactory;

    @Override
    public boolean save(HistoryFieldChange fieldChange) {
	try {
	    sessionFactory.getCurrentSession().save(fieldChange);
	    return true;
	} catch (HibernateException e) {
	    return false;
	}
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<HistoryFieldChange> findUpdateHistoryOfPointRemainByIssueId(long issueId) {
	try {
	    Session session = sessionFactory.getCurrentSession();
	    String q = "SELECT hfc FROM History hs " + "INNER JOIN hs.historyFieldChanges hfc " + "WHERE hs.actionType = :actionType " + "AND hs.containerType = :containerType " + "AND hs.containerId = :containerId " + "AND hfc.fieldName = :fieldName " + "ORDER BY hs.createdOn DESC";
	    Query query = session.createQuery(q);
	    query.setString("actionType", History.UPDATE_ACTION);
	    query.setString("containerType", History.ISSUE_HISTORY);
	    query.setLong("containerId", issueId);
	    query.setCacheable(true);
	    query.setString("fieldName", "Remain point");
	    List<HistoryFieldChange> historyList = query.list();
	    return historyList;
	} catch (HibernateException e) {
	    return null;
	}

    }

    @SuppressWarnings("unchecked")
    @Override
    public List<HistoryFieldChange> findUpdateHistoryOfProject(long projectId, String fieldName) {
	try {
	    Session session = sessionFactory.getCurrentSession();
	    String q = "SELECT hfc FROM History hs " + "INNER JOIN hs.historyFieldChanges hfc " + "WHERE hs.actionType = :actionType " + "AND hs.containerType = :containerType " + "AND hs.containerId = :containerId " + "AND hfc.fieldName = :fieldName " + "ORDER BY hs.createdOn DESC";
	    Query query = session.createQuery(q);
	    query.setString("actionType", History.UPDATE_ACTION);
	    query.setString("containerType", History.PROJECT_HISTORY);
	    query.setLong("containerId", projectId);
	    query.setCacheable(true);
	    query.setString("fieldName", fieldName);
	    List<HistoryFieldChange> historyList = query.list();
	    return historyList;
	} catch (HibernateException e) {
	    return null;
	}
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<HistoryFieldChange> findUpdateHistoryOfTeam(long teamId, String fieldName) {
	try {
	    Session session = sessionFactory.getCurrentSession();
	    String q = "SELECT hfc FROM History hs " + "INNER JOIN hs.historyFieldChanges hfc " + "WHERE hs.actionType = :actionType " + "AND hs.containerType = :containerType " + "AND hs.containerId = :containerId " + "AND hfc.fieldName = :fieldName " + "ORDER BY hs.createdOn DESC";
	    Query query = session.createQuery(q);
	    query.setString("actionType", History.UPDATE_ACTION);
	    query.setString("containerType", History.TEAM_HISTORY);
	    query.setLong("containerId", teamId);
	    query.setCacheable(true);
	    query.setString("fieldName", fieldName);
	    List<HistoryFieldChange> historyList = query.list();
	    return historyList;
	} catch (HibernateException e) {
	    return null;
	}
    }
}
