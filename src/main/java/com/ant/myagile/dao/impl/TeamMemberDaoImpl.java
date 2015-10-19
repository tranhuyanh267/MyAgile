package com.ant.myagile.dao.impl;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.ant.myagile.dao.TeamMemberDao;
import com.ant.myagile.model.Member;
import com.ant.myagile.model.Team;
import com.ant.myagile.model.TeamMember;

@Repository("TeamMemberDao")
public class TeamMemberDaoImpl implements TeamMemberDao {

    @Autowired
    private SessionFactory sessionFactory;

    @Override
    public boolean checkMemberInTeam(Member mem, Team team) {
	try {
	    Criteria criteria = sessionFactory.getCurrentSession().createCriteria(TeamMember.class);
	    criteria.add(Restrictions.eq("member", mem));
	    criteria.setCacheable(true);
	    criteria.add(Restrictions.eq("team", team));
	    if (criteria.list().size() > 0) {
		return true;
	    }
	    return false;
	} catch (HibernateException e) {
	    return false;
	}
    }

    @Override
    public TeamMember findTeamMemberByMemberAndTeam(Member member, Team team) {
	try {
	    Criteria criteria = sessionFactory.getCurrentSession().createCriteria(TeamMember.class);
	    criteria.add(Restrictions.eq("member", member));
	    criteria.setCacheable(true);
	    criteria.add(Restrictions.eq("team", team));
	    TeamMember rs = null;
	    rs = (TeamMember) criteria.uniqueResult();
	    return rs;
	} catch (HibernateException e) {
	    return null;
	}
    }

    @Override
    public boolean save(TeamMember teamMember) {
	try {
	    sessionFactory.getCurrentSession().save(teamMember);
	    return true;
	} catch (HibernateException e) {
	    return false;
	}
    }

    @Override
    public TeamMember findTeamMemberByToken(String token) {
	try {
	    Criteria criteria = sessionFactory.getCurrentSession().createCriteria(TeamMember.class);
	    criteria.add(Restrictions.eq("token", token));
	    criteria.setCacheable(true);
	    TeamMember rs = null;
	    rs = (TeamMember) criteria.uniqueResult();
	    return rs;
	} catch (HibernateException e) {
	    return null;
	}
    }

    @Override
    public boolean delete(TeamMember teamMember) {
	try {
	    sessionFactory.getCurrentSession().delete(teamMember);
	    return true;
	} catch (HibernateException e) {
	    return false;
	}
    }

    @Override
    public boolean update(TeamMember teamMember) {
	try {
	    sessionFactory.getCurrentSession().update(teamMember);
	    return true;
	} catch (HibernateException e) {
	    return false;
	}
    }

    @Override
    public TeamMember findTeamMemberById(Long id) {
	try {
	    return (TeamMember) sessionFactory.getCurrentSession().get(TeamMember.class, id);
	} catch (HibernateException e) {
	    return null;
	}
    }

    @Override
    public void changePositionMember(TeamMember teamMember, String position) {
	TeamMember temp = teamMember;
	Session session = sessionFactory.getCurrentSession();
	temp.setPosition(position);
	session.update(temp);
    }

	@Override
	public  List<TeamMember> getTeamMembersInTeam(Team team) {
		List<TeamMember> teamMembers = new ArrayList<TeamMember>();
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(TeamMember.class);
		criteria.add(Restrictions.eq("team.id",team.getTeamId()));
		teamMembers = criteria.list();
		return teamMembers;
	}
}
