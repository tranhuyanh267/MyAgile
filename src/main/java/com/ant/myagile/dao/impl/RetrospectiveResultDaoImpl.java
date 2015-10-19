package com.ant.myagile.dao.impl;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.ant.myagile.dao.RetrospectiveResultDao;
import com.ant.myagile.model.Project;
import com.ant.myagile.model.RetrospectiveResult;
import com.ant.myagile.model.Sprint;
import com.ant.myagile.model.Team;
import com.itextpdf.text.pdf.PdfStructTreeController.returnType;

@Repository("retrospectiveResultDao")
public class RetrospectiveResultDaoImpl implements RetrospectiveResultDao {
    @Autowired
    private SessionFactory sessionFactory;

    @SuppressWarnings("unchecked")
    @Override
    public List<RetrospectiveResult> findAllRetrospective() {
	try {
	    return sessionFactory.getCurrentSession().createCriteria(RetrospectiveResult.class).setCacheable(true).list();
	} catch (HibernateException e) {
	    return null;
	}
    }

    @Override
    public boolean addRetrospective(RetrospectiveResult retro) {
	try {
	    sessionFactory.getCurrentSession().save(retro);
	    return true;
	} catch (HibernateException e) {
	    return false;
	}
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<RetrospectiveResult> findRetrospectiveByTeamAndProject(Team team, Project project) {
	try {
	    Criteria criteria = sessionFactory.getCurrentSession().createCriteria(RetrospectiveResult.class);
	    criteria.createAlias("sprint.team", "team");
	    criteria.add(Restrictions.eq("project", project));
	    criteria.add(Restrictions.eq("team.teamName", team.getTeamName()));
	    criteria.addOrder(Order.desc("status"));
	    criteria.addOrder(Order.desc("date"));
	    criteria.setCacheable(true);
	    criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
	    criteria.setMaxResults(15 * 3);
	    List<RetrospectiveResult> list = criteria.list();
	    return list;
	} catch (HibernateException e) {
	    return null;
	}
    }

    @Override
    public boolean updateRetrospective(RetrospectiveResult retro) {
	try {
	    sessionFactory.getCurrentSession().update(retro);
	    return true;
	} catch (HibernateException e) {
	    return false;
	}
    }

    @Override
    public boolean updateStatus(RetrospectiveResult retro) {
	try {
	    retro.setStatus("Solved");
	    sessionFactory.getCurrentSession().update(retro);
	    return true;
	} catch (HibernateException e) {
	    return false;
	}
    }

    @SuppressWarnings("unchecked")
    @Override
    public RetrospectiveResult getRetrospectiveById(long id) {
	try {
	    List<RetrospectiveResult> retros = new ArrayList<RetrospectiveResult>();
	    Criteria crit = sessionFactory.getCurrentSession().createCriteria(RetrospectiveResult.class);
	    crit.add(Restrictions.eq("id", id));
	    crit.setCacheable(true);
	    retros = crit.list();
	    return retros.get(0);
	} catch (HibernateException e) {
	    return null;
	}
    }

    @Override
    @Transactional
    public boolean deleteRetrospective(Long id) {
	try {
	    Session session = sessionFactory.getCurrentSession();
	    String hql = "delete from RetrospectiveResult re where re.id = :id";
	    Query query = session.createQuery(hql);
	    query.setLong("id", id);
	    query.executeUpdate();
	    return true;
	} catch (HibernateException e) {
	    return false;
	}
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<RetrospectiveResult> findRetrospectiveByProject(long projectId) {
        try {
            Session session = sessionFactory.getCurrentSession();
            String q = "FROM RetrospectiveResult re WHERE re.project.projectId = :projectId";
            Query query = session.createQuery(q);
            query.setLong("projectId", projectId);
            return query.list();
        } catch (HibernateException e) {
            return null;
        }
    }

	@Override
	public List<RetrospectiveResult> getRetrospectiveResultBySprint(
			Sprint sprint) {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(RetrospectiveResult.class);
		criteria.add(Restrictions.eq("sprint", sprint));
		
		List<RetrospectiveResult> retrospectiveResults = new ArrayList<RetrospectiveResult>();
		
		retrospectiveResults = criteria.list();
		return retrospectiveResults;
	}

}
