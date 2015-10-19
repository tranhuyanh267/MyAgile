package com.ant.myagile.dao.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.ant.myagile.dao.KanbanSwimlineDao;
import com.ant.myagile.model.KanbanIssue;
import com.ant.myagile.model.KanbanSwimline;
import com.ant.myagile.model.Team;

@Repository
public class KanbanSwimlineDaoImpl implements KanbanSwimlineDao
{
    	private static final Logger logger = Logger.getLogger(KanbanSwimlineDaoImpl.class);
	
    	@Autowired
	SessionFactory sessionFactory;

	@Override
	public KanbanSwimline getById(long id) 
	{
		return (KanbanSwimline) sessionFactory.getCurrentSession().get(KanbanSwimline.class, id);
	}

	@Override
	public boolean update(KanbanSwimline kanbanSwimline) 
	{
		try {
			sessionFactory.getCurrentSession().update(kanbanSwimline);
			return true;
		} catch (Exception e) {
		    logger.error("update KanbanSwimline error: " + e.getMessage());
			return false;
		}
	}

	@Override
	public long add(KanbanSwimline kanbanSwimline) 
	{
		return (Long) sessionFactory.getCurrentSession().save(kanbanSwimline);
	}

	@Override
	public boolean delete(long id) 
	{
		try {
			sessionFactory.getCurrentSession().delete(getById(id));
			return true;
		} catch (Exception e) {
		    logger.error("delete KanbanSwimline error: " + e.getMessage());
			return false;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<KanbanSwimline> findKanbanSwimlineByTeamId(long id) 
	{
		try {
			return sessionFactory.getCurrentSession()
					.createCriteria(KanbanSwimline.class)
					.add(Restrictions.eq("team.teamId", id))
					.addOrder(Order.asc("swimlineId"))
					.list();
		} catch (HibernateException e) {
		    logger.error("findKanbanSwimlineByTeamId error: " + e.getMessage());
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void updateIssueByTeamId(Long teamId, Long swimlineId) 
	{
		try {
			Session session = sessionFactory.getCurrentSession();
			Criteria cri = session.createCriteria(KanbanIssue.class);
			cri.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
			List<KanbanIssue> list = cri.add(Restrictions.eq("team.teamId", teamId)).add(Restrictions.eq("kanbanSwimline.swimlineId", swimlineId)).list();
			List<KanbanSwimline> listStatus = this.findKanbanSwimlineByTeamId(teamId);
			if (list.size() != 0) {
				for (KanbanIssue is : list) {
					is.setKanbanSwimline(listStatus.get(listStatus.indexOf(getById(swimlineId)) - 1));
					session.update(is);
				}
			}
		} catch (Exception e) {
		    logger.error("findKanbanSwimlineByTeamId error: " + e.getMessage());
			e.printStackTrace();
		}
		
	}

	@Override
	public KanbanSwimline firstSwimlineByTeamId(long teamId) 
	{
		try {
			KanbanSwimline firstSwimline = (KanbanSwimline) sessionFactory.getCurrentSession()
					.createCriteria(KanbanSwimline.class)
					.add(Restrictions.eq("team.teamId", teamId))
					.addOrder(Order.asc("swimlineId"))
					.setMaxResults(1)
					.uniqueResult();
			return firstSwimline;
		} catch (Exception e) {
		    	logger.error("firstSwimlineByTeamId error: " + e.getMessage());
			return null;
		}
		
	}

	@Override
	public List<KanbanSwimline> getAllKanbanSwimlinesByTeam(Team team) 
	{
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(KanbanSwimline.class);
		criteria.add(Restrictions.eq("team", team));
		
		List<KanbanSwimline> kanbanSwimlines = new ArrayList<KanbanSwimline>();
		kanbanSwimlines = criteria.list();
		return kanbanSwimlines;
	}
	

}
