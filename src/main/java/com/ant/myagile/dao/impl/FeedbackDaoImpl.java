package com.ant.myagile.dao.impl;

import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.ant.myagile.dao.FeedbackDao;
import com.ant.myagile.model.Feedback;

@Repository("feedbackDao")
@Transactional
public class FeedbackDaoImpl implements FeedbackDao {

    @Autowired
    private SessionFactory sessionFactory;

    @Override
    public boolean saveFeedback(Feedback feedback) {
	try {
	    this.sessionFactory.getCurrentSession().save(feedback);
	    return true;
	} catch (HibernateException e) {
	    return false;
	}
    }

    @Override
    public boolean updateFeedback(Feedback feedback) {
	try {
	    this.sessionFactory.getCurrentSession().update(feedback);
	    return true;
	} catch (HibernateException e) {
	    return false;
	}
    }

    @Override
    @Transactional
    public boolean deleteFeedback(Feedback feedback) {
	try {
	    this.sessionFactory.getCurrentSession().delete(feedback);
	    return true;
	} catch (HibernateException e) {
	    return false;
	}
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Feedback> findFeedbacksBySprintIdAndProjectId(Long projectId, Long sprintId) {
	try {
	    Session session = sessionFactory.getCurrentSession();
	    String q = "FROM Feedback fb WHERE fb.project.projectId = :projectId AND fb.sprint.sprintId = :sprintId ORDER BY fb.createdDate ASC";
	    Query query = session.createQuery(q);
	    query.setLong("projectId", projectId);
	    query.setLong("sprintId", sprintId);
	    return query.list();
	} catch (HibernateException e) {
	    return null;
	}
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Feedback> findFeedbacksByProjectId(long projectId) {
        try {
            Session session = sessionFactory.getCurrentSession();
            String q = "FROM Feedback fb WHERE fb.project.projectId = :projectId ORDER BY fb.createdDate ASC";
            Query query = session.createQuery(q);
            query.setLong("projectId", projectId);
            return query.list();
        } catch (HibernateException e) {
            return null;
        }
    }

}
