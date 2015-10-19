package com.ant.myagile.dao.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.ant.myagile.dao.HolidayDao;
import com.ant.myagile.model.Holiday;
import com.ant.myagile.model.Member;
import com.ant.myagile.model.Sprint;

@Repository("HolidayDao")
@Transactional
public class HolidayDaoImpl implements HolidayDao{
	
    @Autowired
    private SessionFactory sessionFactory;

	@Override
	public Holiday findHolidayByDateAndMemberInSprint(Date date, Member member,Sprint sprint) {
		try {
		    Criteria criteria = this.sessionFactory.getCurrentSession().createCriteria(Holiday.class);
		    criteria.add(Restrictions.eq("member", member));
		    criteria.add(Restrictions.eq("leaveDate", date));
		    criteria.add(Restrictions.eq("sprint", sprint));
		    criteria.setCacheable(true);
		    criteria.setCacheable(true);
		    return (Holiday) criteria.uniqueResult();
		} catch (HibernateException e) {
		    return null;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Holiday> findHolidayBySprint(Sprint sprint) {
		try {
		    Criteria criteria = this.sessionFactory.getCurrentSession().createCriteria(Holiday.class);
		    List<Holiday> holidayList = new ArrayList<Holiday>();
		    criteria.add(Restrictions.eq("sprint", sprint));
		    criteria.setCacheable(true);
		    criteria.addOrder(Order.desc("leaveDate"));
		    criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
		    holidayList = criteria.list();
		    return holidayList;
		} catch (HibernateException e) {
		    return null;
		}
	}

	@Override
	public boolean save(Holiday holiday) {
		try {
		    this.sessionFactory.getCurrentSession().saveOrUpdate(holiday);
		    return true;
		} catch (HibernateException e) {
		    return false;
		}
	}

	@Override
	public boolean delete(Holiday holiday) {
		try {
		    Session session = this.sessionFactory.getCurrentSession();
		    session.delete(holiday);
		    return true;
		} catch (HibernateException e) {
		    return false;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Holiday> findHolidayByMemberInSprint(Member member,
			Sprint sprint) {
		try {
		    Criteria criteria = this.sessionFactory.getCurrentSession().createCriteria(Holiday.class);
		    List<Holiday> holidayList = new ArrayList<Holiday>();
		    criteria.add(Restrictions.eq("sprint", sprint));
		    criteria.add(Restrictions.eq("member", member));
		    criteria.setCacheable(true);
		    criteria.addOrder(Order.desc("leaveDate"));
		    criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
		    holidayList = criteria.list();
		    return holidayList;
		} catch (HibernateException e) {
		    return null;
		}
	}

}
