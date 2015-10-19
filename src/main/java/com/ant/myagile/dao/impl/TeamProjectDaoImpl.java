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
import org.springframework.transaction.annotation.Transactional;

import com.ant.myagile.dao.TeamProjectDao;
import com.ant.myagile.model.Project;
import com.ant.myagile.model.Team;
import com.ant.myagile.model.TeamProject;
import com.itextpdf.text.pdf.PdfStructTreeController.returnType;

@Repository("TeamProjectDao")
public class TeamProjectDaoImpl implements TeamProjectDao {
    @Autowired
    private SessionFactory sessionFactory;

    @Override
    public boolean create(TeamProject tp) {
	try {
	    sessionFactory.getCurrentSession().save(tp);
	    return true;
	} catch (Exception e) {
	    return false;
	}
    }

    @Override
    public boolean update(TeamProject teamPro) {
	try {
	    sessionFactory.getCurrentSession().update(teamPro);
	    return true;
	} catch (Exception e) {
	    return false;
	}
    }

    @Override
    public TeamProject findTeamProjectById(long id) {
	try {
	    return (TeamProject) sessionFactory.getCurrentSession().get(TeamProject.class, id);
	} catch (HibernateException e) {
	    return null;
	}
    }

    @Override
    public TeamProject findTeamProjectByProjectAndTeam(Project p, Team t) {
	try {
	    Criteria criteria = sessionFactory.getCurrentSession().createCriteria(TeamProject.class);
	    criteria.add(Restrictions.eq("project", p));
	    criteria.add(Restrictions.eq("team", t));
	    criteria.setCacheable(true);
	    TeamProject teamProject = null;
	    if (criteria.list().size() > 0)
		teamProject = (TeamProject) criteria.list().get(0);
	    sessionFactory.getCurrentSession().flush();
	    return teamProject;
	} catch (HibernateException e) {
	    return null;
	}
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<TeamProject> findAllTeamProject() {
	try {
	    return sessionFactory.getCurrentSession().createCriteria(TeamProject.class).setCacheable(true).list();
	} catch (HibernateException e) {
	    return null;
	}
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<TeamProject> findTeamsProjectsByProjectId(long id) {
	try {
	    Session session = sessionFactory.getCurrentSession();
	    Criteria cr = session.createCriteria(TeamProject.class);
	    List<TeamProject> list = cr.add(Restrictions.eq("project.projectId", id)).setCacheable(true).list();
	    return list;
	} catch (HibernateException e) {
	    return null;
	}
    }

    @Override
    @Transactional
    public boolean delete(TeamProject teamProject) {
        try {
            sessionFactory.getCurrentSession().delete(teamProject);
            return true;
        } catch (HibernateException e) {
            return false;
        }
    }

	@Override
	public List<TeamProject> getTeamProjectsByTeam(Team team) {
		List<TeamProject> teamProjects = new ArrayList<TeamProject>();
		
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(TeamProject.class);
		criteria.add(Restrictions.eq("team", team));
		
		teamProjects = criteria.list();
		return teamProjects;
		
	}

}
