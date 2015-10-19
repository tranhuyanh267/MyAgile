package com.ant.myagile.dao.impl;

import java.io.Serializable;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.ant.myagile.dao.LastUserStoryIndexDao;
import com.ant.myagile.model.LastUserStoryIndex;
import com.ant.myagile.model.Project;
import com.ant.myagile.model.UserStory;
import com.ant.myagile.service.UserStoryService;

@Repository("LastUserStoryIndexDao")
@Transactional
public class LastUserStoryIndexDaoImpl implements LastUserStoryIndexDao, Serializable 
{
    private static final long serialVersionUID = 1L;
    
    private static final Logger logger = Logger.getLogger(LastUserStoryIndexDaoImpl.class);
    
    @Autowired
    private SessionFactory sessionFactory;
    @Autowired
    UserStoryService userStoryService;

    @Override
    public Long findLastUserStoryIndex(long projectId) {
	try {
	    Criteria cri = sessionFactory.getCurrentSession().createCriteria(LastUserStoryIndex.class);
	    cri.add(Restrictions.eq("project.projectId", projectId));
	    cri.setCacheable(true);
	    LastUserStoryIndex us = null;
	    us = (LastUserStoryIndex) cri.uniqueResult();
	    if (us != null) {
		return us.getLastUserStorySortId();
	    } else {
		UserStory lastUs = new UserStory();
		lastUs = userStoryService.findLastUserStoryInProject(projectId);
		return (lastUs != null) ? lastUs.getSortId() : 0;
	    }
	} catch (HibernateException e) {
	    return null;
	}
    }

    @Override
    public boolean updateIndex(Project project, Long sortId) {
	try {
	    Session session = sessionFactory.getCurrentSession();
	    Criteria cri = sessionFactory.getCurrentSession().createCriteria(LastUserStoryIndex.class);
	    cri.add(Restrictions.eq("project", project));
	    cri.setCacheable(true);
	    LastUserStoryIndex us = null;
	    us = (LastUserStoryIndex) cri.uniqueResult();
	    if (us != null) {
		us.setLastUserStorySortId(sortId);
		session.update(us);
	    } else {
		us = new LastUserStoryIndex();
		us.setProject(project);
		us.setLastUserStorySortId(sortId);
		session.save(us);
	    }
	    return true;
	} catch (HibernateException e) {
	    return false;
	}
    }

    
    @Override
    @Transactional
    public LastUserStoryIndex findLastUserStoryIndexByProjectId(long projectId) 
    {
        try 
        {
            Criteria cri = sessionFactory.getCurrentSession().createCriteria(LastUserStoryIndex.class);
            cri.add(Restrictions.eq("project.projectId", projectId));
            cri.setCacheable(true);
            LastUserStoryIndex us = null;
            us = (LastUserStoryIndex) cri.uniqueResult();
            return us;
        } catch (HibernateException e) {
            logger.error("findLastUserStoryIndexByProjectId error: " + e.getMessage());
            return null;
        }
    }
    

    @Override
    public boolean delete(LastUserStoryIndex lastUserStoryIndex) {
        try {
            this.sessionFactory.getCurrentSession().delete(lastUserStoryIndex);
            return true;
        } catch (HibernateException e) {
            return false;
        }
    }

}
