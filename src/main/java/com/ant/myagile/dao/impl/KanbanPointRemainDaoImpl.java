package com.ant.myagile.dao.impl;

import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.ant.myagile.dao.KanbanPointRemainDao;
import com.ant.myagile.model.KanbanPointRemain;

@Repository
public class KanbanPointRemainDaoImpl implements KanbanPointRemainDao{
	 @Autowired
	    private SessionFactory sessionFactory;

	@Override
	public boolean save(KanbanPointRemain kanbanPointRemain) {
		try {
			sessionFactory.getCurrentSession().save(kanbanPointRemain);
			return true;
		} catch (HibernateException e) {
			return false;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<KanbanPointRemain> getKanbanPointRemainByKanbanIssue(
			long idKanbanIssue) {
		return sessionFactory.getCurrentSession().createCriteria(KanbanPointRemain.class)
				.add(Restrictions.eq("kanbanissue.issueId", idKanbanIssue)).list();
	}

	
}
