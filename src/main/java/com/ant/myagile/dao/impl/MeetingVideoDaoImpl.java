package com.ant.myagile.dao.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.jruby.compiler.ir.operands.Array;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.ant.myagile.dao.MeetingVideoDao;
import com.ant.myagile.model.MeetingVideo;
import com.ant.myagile.model.Sprint;

@Repository("meetingVideoDao")
@Transactional
public class MeetingVideoDaoImpl implements MeetingVideoDao {

    @Autowired
    private SessionFactory sessionFactory;

    @Override
    public MeetingVideo save(MeetingVideo meetingVideo) {

	try {
	    sessionFactory.getCurrentSession().save(meetingVideo);
	    return meetingVideo;
	} catch (HibernateException e) {
	    return null;
	}
    }

    @Override
    public MeetingVideo findMeetingVideoByDateAndSprintId(Date date, Long sprintId) {
	try {
	    Session session = sessionFactory.getCurrentSession();
	    String q = "FROM MeetingVideo mv WHERE mv.meetingDate = :meetingDate AND mv.sprint.sprintId = :sprintId";
	    Query query = session.createQuery(q);
	    query.setDate("meetingDate", date);
	    query.setCacheable(true);
	    query.setLong("sprintId", sprintId);
	    query.setMaxResults(1);
	    MeetingVideo mv = (MeetingVideo) query.uniqueResult();
	    return mv;
	} catch (HibernateException e) {
	    return null;
	}
    }

    @Override
    public MeetingVideo update(MeetingVideo meetingVideo) {
	try {
	    sessionFactory.getCurrentSession().update(meetingVideo);
	    return meetingVideo;
	} catch (HibernateException e) {
	    return null;
	}
    }

    public SessionFactory getSessionFactory() {
	return sessionFactory;
    }

    public void setSessionFactory(SessionFactory sessionFactory) {
	this.sessionFactory = sessionFactory;
    }

	@Override
	public List<MeetingVideo> getAllMeetingVideoInSprint(Sprint sprint) {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(MeetingVideo.class);
		criteria.add(Restrictions.eq("sprint", sprint));
		List<MeetingVideo>  meetingVideos = new ArrayList<MeetingVideo>();
		meetingVideos = criteria.list();
		return meetingVideos;
	}

	@Override
	public boolean delete(MeetingVideo meetingVideo) {
		try {
		    sessionFactory.getCurrentSession().delete(meetingVideo);
		    return true;
		} catch (HibernateException e) {
		    return false;
		
	}
	}

}
