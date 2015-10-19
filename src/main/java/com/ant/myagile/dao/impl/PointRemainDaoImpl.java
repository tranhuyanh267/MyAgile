package com.ant.myagile.dao.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.jfree.util.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.ant.myagile.dao.PointRemainDao;
import com.ant.myagile.model.PointRemain;
import com.ant.myagile.model.Sprint;
import com.ant.myagile.utils.Utils;

@Repository
@Transactional
public class PointRemainDaoImpl implements PointRemainDao {
    public static final int MAX_HOUR = 23;
    public static final int MAX_MINUTE_SECOND = 23;
    @Autowired
    private SessionFactory sessionFactory;

    @Override
    public boolean save(PointRemain pointRemain) {
	try {
	    sessionFactory.getCurrentSession().save(pointRemain);
	    return true;
	} catch (HibernateException e) {
	    return false;
	}
    }

    @Override
    public boolean update(PointRemain pointRemain) {
	try {
	    sessionFactory.getCurrentSession().update(pointRemain);
	    return true;
	} catch (HibernateException e) {
	    return false;
	}
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<PointRemain> findPointRemainByIssueId(long issueId) {
	List<PointRemain> pointRemains = new ArrayList<PointRemain>();
	try {
	    Criteria crit = sessionFactory.getCurrentSession().createCriteria(PointRemain.class);
	    crit.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
	    crit.addOrder(Order.desc("dateUpdate"));
	    crit.add(Restrictions.eq("issue.issueId", issueId));
	    crit.setCacheable(true);
	    if (crit.list().size() > 0) {
		pointRemains.addAll(crit.list());
	    }
	} catch (Exception e) {
	    Log.error(e.getMessage());
	}
	return pointRemains;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<PointRemain> findPointRemainByIssueIdAndSprint(long issueId, Sprint s) {
	try {
	    Date endDate = Utils.stringToDate(Utils.toShortDate(Utils.addDays(s.getDateEnd(), 1)), new SimpleDateFormat("MM/dd/yyyy"));
	    List<PointRemain> pointRemains = new ArrayList<PointRemain>();
	    Criteria crit = sessionFactory.getCurrentSession().createCriteria(PointRemain.class);
	    crit.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
	    crit.add(Restrictions.eq("issue.issueId", issueId));
	    crit.add(Restrictions.ge("dateUpdate", s.getDateStart()));
	    crit.add(Restrictions.lt("dateUpdate", endDate));
	    crit.setCacheable(true);
	    pointRemains = crit.list();
	    return pointRemains;
	} catch (HibernateException e) {
	    return null;
	}
    }

    @SuppressWarnings("unchecked")
    @Override
    public PointRemain findPointRemainByIssueIdAndNowDate(long issueId) {
	try {
	    Criteria cri = sessionFactory.getCurrentSession().createCriteria(PointRemain.class);
	    List<PointRemain> pointRemains = new ArrayList<PointRemain>();
	    cri.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
	    cri.add(Restrictions.eq("issue.issueId", issueId));
	    cri.setCacheable(true);
	    Calendar calendar = Calendar.getInstance();
	    calendar.set(Calendar.HOUR_OF_DAY, 0);
	    calendar.set(Calendar.MINUTE, 0);
	    calendar.set(Calendar.SECOND, 0);
	    Date fromDate = calendar.getTime();

	    calendar.set(Calendar.HOUR_OF_DAY, MAX_HOUR);
	    calendar.set(Calendar.MINUTE, MAX_MINUTE_SECOND);
	    calendar.set(Calendar.SECOND, MAX_MINUTE_SECOND);
	    Date toDate = calendar.getTime();

	    cri.add(Restrictions.between("dateUpdate", fromDate, toDate));
	    cri.add(Restrictions.eq("issue.issueId", issueId));
	    pointRemains = cri.list();
	    if (pointRemains.size() != 0) {
		return pointRemains.get(0);
	    }
	    return null;
	} catch (HibernateException e) {
	    return null;
	}
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<PointRemain> findAllIssueByDate(Date d) {
	try {
	    List<PointRemain> pointRemains = new ArrayList<PointRemain>();
	    Criteria crit = sessionFactory.getCurrentSession().createCriteria(PointRemain.class);
	    crit.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
	    crit.setCacheable(true);
	    Calendar startCale = Calendar.getInstance(TimeZone.getDefault());
	    startCale.setTime(d);
	    String sql = "EXTRACT(YEAR FROM date_update)::INTEGER =" + startCale.get(Calendar.YEAR);
	    sql += "and EXTRACT(MONTH FROM date_update)::INTEGER =" + (startCale.get(Calendar.MONTH) + 1);
	    sql += "and EXTRACT(DAY FROM date_update)::INTEGER =" + startCale.get(Calendar.DAY_OF_MONTH);
	    crit.add(Restrictions.sqlRestriction(sql));
	    pointRemains = crit.list();
	    return pointRemains;
	} catch (HibernateException e) {
	    return null;
	}
    }

    @Override
    public boolean delete(PointRemain pointRemain) {
	try {
	    sessionFactory.getCurrentSession().delete(pointRemain);
	    return true;
	} catch (HibernateException e) {
	    return false;
	}

    }
}
