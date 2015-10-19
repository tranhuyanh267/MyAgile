package com.ant.myagile.dao.impl;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.ant.myagile.dao.WikiDao;
import com.ant.myagile.model.Team;
import com.ant.myagile.model.Wiki;

@Repository
public class WikiDaoImpl implements WikiDao{

	@Autowired 
	SessionFactory sessionFactory;
	
	@Override
	public Wiki getByTeamId(long teamId) {
		Session session= sessionFactory.getCurrentSession();
		Team team = (Team) session.get(Team.class, teamId);
		return (Wiki) sessionFactory.getCurrentSession().createCriteria(Wiki.class,"wiki").add(Restrictions.eq("wiki.team",team )).uniqueResult();
	}

	@Override
	public int add(Wiki wiki) {
		
		try{
			sessionFactory.getCurrentSession().save(wiki);
			return 1;
		}catch(Exception e){
			return -1;
		}
	}

	@Override
	public int update(Wiki wiki) {
		try{
			sessionFactory.getCurrentSession().update(wiki);
			return 1;
		}catch(Exception e){
			return -1;
		}
		
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean checkTeamExistWiki(Long teamId) {
		try{
			List<Wiki> lWiki = sessionFactory.getCurrentSession()
					.createCriteria(Wiki.class)
					.add(Restrictions.eq("team.teamId", teamId))
					.list();
			if(!lWiki.isEmpty())
				return true;
			else
				return false;
		}catch(Exception e){
			return false;
		}
	}

	@Override
	public boolean delete(Wiki wiki) {
		try{
			sessionFactory.getCurrentSession().delete(wiki);
			return true;
		}catch(Exception e){
			return false;
		}
	}

}
