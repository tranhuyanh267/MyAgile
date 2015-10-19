package com.ant.myagile.dao.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.ant.myagile.dao.SprintDao;
import com.ant.myagile.model.Issue;
import com.ant.myagile.model.Sprint;
import com.ant.myagile.model.UserStory;

@Repository
@Transactional
public class SprintDaoImpl implements SprintDao {

    @Autowired
    private SessionFactory sessionFactory;

    private static final Logger LOGGER = Logger.getLogger(SprintDaoImpl.class);

    @Override
    public boolean save(Sprint sprint) {
	try {
	    sessionFactory.getCurrentSession().save(sprint);
	    return true;
	} catch (HibernateException e) {
	    return false;
	}
    }

    @Override
    public boolean update(Sprint sprint) {
	try {
	    sessionFactory.getCurrentSession().update(sprint);
	    return true;
	} catch (HibernateException e) {
	    return false;
	}
    }

    @Override
    public boolean delete(Sprint sprint) {
	try {
	    sessionFactory.getCurrentSession().delete(sprint);
	    return true;
	} catch (HibernateException e) {
	    return false;
	}
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Sprint> findSprintsByTeamId(Long teamId) {
	try {
	    return sessionFactory.getCurrentSession().createCriteria(Sprint.class).add(Restrictions.eq("team.teamId", teamId)).addOrder(Order.desc("sprintId")).list();
	} catch (HibernateException e) {
	    return null;
	}
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Sprint> findSprintsByTeamIdOrderByAsc(Long teamId) {
	try {
	    return sessionFactory.getCurrentSession().createCriteria(Sprint.class).add(Restrictions.eq("team.teamId", teamId)).setCacheable(true).addOrder(Order.asc("dateStart")).list();
	} catch (HibernateException e) {
	    return null;
	}
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Sprint> findSprintsAreOpen(Long teamId) {
        try {
            Session session = sessionFactory.getCurrentSession();
            List<Sprint> sprintListOpen = new ArrayList<Sprint>();
            sprintListOpen = session.createCriteria(Sprint.class).add(Restrictions.and(
                                Restrictions.eq("team.teamId", teamId), 
                                Restrictions.eq("status", "open"))
                             ).setCacheable(true).addOrder(Order.desc("dateStart")).list();
            return sprintListOpen;
        } catch (HibernateException e) {
            LOGGER.error(e.getMessage());
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Sprint findLastSprintByTeamId(Long teamId) {
	try {
	    Session session = sessionFactory.getCurrentSession();
	    Sprint lastSprint = null;
	    List<Sprint> sprintList = new ArrayList<Sprint>();
	    sprintList = session.createCriteria(Sprint.class).add(Restrictions.eq("team.teamId", teamId)).setCacheable(true).addOrder(Order.desc("dateStart")).list();
	    if (sprintList.size() > 0)
		lastSprint = sprintList.get(0);
	    return lastSprint;
	} catch (HibernateException e) {
	    return null;
	}
    }

    @Override
    public Sprint findSprintById(Long sprintId) {
	try {
	    return (Sprint) sessionFactory.getCurrentSession().get(Sprint.class, sprintId);
	} catch (HibernateException e) {
	    return null;
	}
    }

    @Override
    public int countSprintsByTeamId(Long teamId) {
	try {
	    return sessionFactory.getCurrentSession().createCriteria(Sprint.class).add(Restrictions.eq("team.teamId", teamId)).addOrder(Order.desc("sprintId")).list().size();
	} catch (HibernateException e) {
	    return 0;
	}
    }

	@SuppressWarnings("unchecked")
	@Override
	public boolean checkUserstoryInSprint(long userstoryId, Sprint sprint) {
		List<Issue> issueTemp = sessionFactory.getCurrentSession().createCriteria(Issue.class)
				.add(Restrictions.eq("userStory.userStoryId", userstoryId))
				.add(Restrictions.eq("sprint.sprintId", sprint.getSprintId()))
				.list();
		if(!issueTemp.isEmpty()){
			return true;
		}
		return false;
	}
}
