package com.ant.myagile.dao.impl;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.ant.myagile.dao.SwimlineDao;
import com.ant.myagile.model.Sprint;
import com.ant.myagile.model.Status;
import com.ant.myagile.model.Swimline;
import com.itextpdf.text.pdf.PdfStructTreeController.returnType;

@Repository("swimlineDao")
@Transactional
public class SwimlineDaoImpl implements SwimlineDao{
	
	@Autowired
    private SessionFactory sessionFactory;
	
	@Override
	public long save(Swimline swimline) {
		try {
		    long idAfterSaveSwimline = (Long) sessionFactory.getCurrentSession().save(swimline);
		    return idAfterSaveSwimline;
		} catch (HibernateException e) {
		    return 0L;
		}
	}

	@Override
	public boolean delete(Swimline swimline) {
		try {
		    sessionFactory.getCurrentSession().delete(swimline);
		    return true;
		} catch (HibernateException e) {
		    return false;
		}
	}

	@Override
	public boolean update(Swimline swimline) {
		try {
		    sessionFactory.getCurrentSession().update(swimline);
		    return true;
		} catch (HibernateException e) {
		    return false;
		}
	}

	@Override
	public Swimline getSwimlineBySprint(long sprintId) {
		try {
			Swimline swimline = (Swimline) sessionFactory.getCurrentSession()
					.createCriteria(Swimline.class)
					.add(Restrictions.eq("sprint.sprintId",sprintId))
					.uniqueResult();
			return swimline;
		} catch (HibernateException e) {
			return null;
		}
	}

	@Override
	public List<Swimline> getAllSwimlineBySprint(Sprint sprint) {
		Criteria  criteria = sessionFactory.getCurrentSession().createCriteria(Swimline.class);
		 criteria.add(Restrictions.eq("sprint", sprint));
		 List<Swimline> swimlines = new ArrayList<Swimline>();
		 swimlines = criteria.list();
		 return swimlines;
	}

	

}
