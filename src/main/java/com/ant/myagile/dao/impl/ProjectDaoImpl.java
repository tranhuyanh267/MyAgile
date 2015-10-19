package com.ant.myagile.dao.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.ListUtils;
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

import com.ant.myagile.dao.ProjectDao;
import com.ant.myagile.dto.StateLazyLoading;
import com.ant.myagile.model.Member;
import com.ant.myagile.model.Project;
import com.ant.myagile.model.Team;
import com.ant.myagile.model.TeamProject;

@Repository("ProjectDao")
public class ProjectDaoImpl implements ProjectDao, Serializable {
    private static final long serialVersionUID = 1L;
    @Autowired
    private SessionFactory sessionFactory;

    @Override
    public boolean save(Project project) {
	try {
	    sessionFactory.getCurrentSession().save(project);
	    return true;
	} catch (HibernateException e) {
	    return false;
	}
    }

    @Override
    public boolean update(Project project) {
	try {
	    sessionFactory.getCurrentSession().update(project);
	    return true;
	} catch (HibernateException e) {
	    return false;
	}
    }

    @Override
    @Transactional
    public boolean delete(Project project) {
	try {
	    sessionFactory.getCurrentSession().delete(project);
	    return true;
	} catch (HibernateException e) {
	    return false;
	}
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Project> listAllproject() {
	try {
	    Criteria c = sessionFactory.getCurrentSession().createCriteria(Project.class);
	    c.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
	    c.setCacheable(true);
	    c.addOrder(Order.asc("projectId"));
	    return c.list();
	} catch (HibernateException e) {

	    return null;
	}
    }

    @Override
    public Project findById(Long id) {
	try {
	    return (Project) sessionFactory.getCurrentSession().get(Project.class, id);
	} catch (HibernateException e) {
	    return null;
	}
    }

    @SuppressWarnings({ "unchecked" })
    @Override
    public List<Project> findByOwnerId(Long ownerId) {
	try {
	    Session session = sessionFactory.getCurrentSession();
	    Criteria cr = session.createCriteria(Project.class);
	    cr.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
	    cr.addOrder(Order.desc("projectId"));
	    List<Project> list = cr.add(Restrictions.eq("owner.memberId", ownerId)).add(Restrictions.eq("isArchived", false)).list();
	    return list;
	} catch (HibernateException e) {
	    return null;
	}
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Project> findByMemberId(Long memberId) {
	try {
	    Session session = sessionFactory.getCurrentSession();
	    String hql = "select distinct p " + "from TeamMember tm inner join tm.team t "
	    			+ "inner join t.projects tp "
	    			+ "inner join tp.project p "
	    			+ "where p.isArchived = false and tm.member = :memberId"
	    			+ " and tm.isAccepted=true";
	    Query query = session.createQuery(hql);
	    query.setLong("memberId", memberId);
	    List<Project> list = query.list();
	    return list;
	} catch (HibernateException e) {
		e.printStackTrace();
	    return null;
	}
    }

    @Override
    public Project checkExistProject(String projectName, long ownId) {
	try {
	    Session session = sessionFactory.getCurrentSession();
	    Criteria cr = session.createCriteria(Project.class);
	    Project project = (Project) cr.add(Restrictions.like("projectName", projectName)).setCacheable(true).add(Restrictions.eq("owner.memberId", ownId)).uniqueResult();
	    return project;
	} catch (HibernateException e) {
	    return null;
	}
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Project> findProjectsByTeam(long teamId) {
	try {
	    Criteria teamProjectCriteria = sessionFactory.getCurrentSession().createCriteria(TeamProject.class);
	    teamProjectCriteria.add(Restrictions.eq("team.teamId", teamId));
	    teamProjectCriteria.setCacheable(true);
	    List<TeamProject> teamProjectList = teamProjectCriteria.list();
	    List<Project> teams = new ArrayList<Project>();
	    for (TeamProject tp : teamProjectList) {
		teams.add(tp.getProject());
	    }
	    if (teams.size() == 0) {
		return null;
	    }
	    return teams;
	} catch (Exception e) {
	    return null;
	}
    }

    @SuppressWarnings("unchecked")
	@Override
    public List<Project> findAllProjectsByTeam(Team t) {
	try {
		String getProjectFromTeam = "SELECT DISTINCT p FROM TeamProject tp"
				+ " INNER JOIN tp.project p"
				+ " WHERE tp.team.teamId = :idTeam"
				+ " AND tp.project.isArchived IS false"
				+ " ORDER BY p.projectName ASC";
		return sessionFactory.getCurrentSession()
				.createQuery(getProjectFromTeam)
				.setParameter("idTeam", t.getTeamId())
				.list();
		
	} catch (HibernateException e) {
	    return null;
	}
    }

    @Override
    public List<Project> findProjectsAssignedToTeam(Team t) {
	try {
	    Criteria criteria = sessionFactory.getCurrentSession()
	    					.createCriteria(TeamProject.class, "teamProject")
	    					.createAlias("teamProject.project", "project")
	    					.setCacheable(true)
	    					.add(Restrictions.eq("team", t))
	    					.add(Restrictions.eq("project.isArchived", false));
	    criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
	    @SuppressWarnings("unchecked")
	    List<TeamProject> teamProjectList = (List<TeamProject>) criteria.list();
	    List<Project> projects = new ArrayList<Project>();
	    for (TeamProject tp : teamProjectList) {
		if ((tp.getEndDate() == null) || (tp.getEndDate().toString().equals("")))
		    projects.add(tp.getProject());
	    }
	    sessionFactory.getCurrentSession().flush();
	    return projects;
	} catch (HibernateException e) {
	    return null;
	}
    }

	@Override
	public List<Project> findAllByMemberId(Long memberId) {
		try {
		    Session session = sessionFactory.getCurrentSession();
		    
		    String hql = "select p " + "from TeamMember tm inner join tm.team t " + "inner join t.projects tp " + "inner join tp.project p " + "where tm.member = :memberId and tm.isAccepted=true and p.owner.memberId != :memberId";
		    Query query = session.createQuery(hql);
		    query.setLong("memberId", memberId);
		    List<Project> list = query.list();
		    
		    /*String hql2 = "select p from Project p where p.owner.memberId = :memberId and p.projectId not in (select tp.project.projectId from TeamProject tp)";
		    Query query2 = session.createQuery(hql2);
		    query2.setLong("memberId", memberId);
		    List<Project> list2 = query2.list();*/
		    
		    String hql2 = "select p from Project p where p.owner.memberId = :memberId";
		    Query query2 = session.createQuery(hql2);
		    query2.setLong("memberId", memberId);
		    List<Project> list2 = query2.list();
		    
		    list.addAll(list2);
		    return list;
		} catch (HibernateException e) {
		    return null;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Project> findLazyProjects(StateLazyLoading lazyLoadingProject) {
		String statementInit = "SELECT p FROM Project p";
		
		String statementFilter = "";
		for(String field : lazyLoadingProject.getFilters().getField()) {
			statementFilter = statementFilter
					+ " lower(p.{field}) like lower(:valueFilter) or".replace("{field}", field);
		}
		//remove or
		statementFilter = statementFilter.substring(0,statementFilter.length() - 3);
		
		String statementOrder = "";
		if(lazyLoadingProject.getSorters().getField() != null) {
			statementOrder = "ORDER BY p.{field} {value}";
			statementOrder = statementOrder.replace("{field}", lazyLoadingProject.getSorters().getField())
					.replace("{value}", lazyLoadingProject.getSorters().getValue().toString());
		}
		
		String finalStatement = statementInit + " WHERE (" + statementFilter + ") " + statementOrder;
		
		List<Project> projects = sessionFactory.getCurrentSession()
				.createQuery(finalStatement)
				.setParameter("valueFilter", "%" + lazyLoadingProject.getFilters().getValue() + "%")
				.setFirstResult(lazyLoadingProject.getStart())
				.setMaxResults(lazyLoadingProject.getLimit())
				.list();
		return projects;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Project> findLazyByMemberId(Long idUserForProject,
			StateLazyLoading lazyLoadingUserProject) {
		try {
			
		
		 Session session = sessionFactory.getCurrentSession();
		    
		    String hqlInit = "SELECT varProject FROM Project varProject"
		    		+ " WHERE varProject IN "
		    		+ "("
			    		+ "SELECT DISTINCT p FROM TeamMember tm "
			    		+ " INNER JOIN tm.team t"
			    		+ " INNER JOIN t.projects tp"
			    		+ " INNER JOIN tp.project p"
			    		+ " WHERE"
			    		+ " ( "
				    		+ "tm.member.memberId = :memberId"
				    		+ " AND tm.isAccepted=true"
				    		+ " AND p.owner.memberId != :memberId"
				    		+ " AND ({filterProject})"
			    		+ " ) "
			    		+ " OR p IN (SELECT projectOwner"
					    				+ " FROM Project projectOwner"
					    				+ " WHERE projectOwner.owner.memberId = :memberId AND ({filterProjectOwner})"
					    			+ ")"
				    + ")"
				    + " {sortProject}";
		    
		    String hqlFilterProject = "";
		    String hqlFilterProjectOwner = "";
			for(String field : lazyLoadingUserProject.getFilters().getField()) {
				hqlFilterProject = hqlFilterProject
						+ " lower(p.{field}) like lower(:valueFilter) or".replace("{field}", field);
				hqlFilterProjectOwner = hqlFilterProjectOwner + " lower(projectOwner.{field}) like lower(:valueFilter) or".replace("{field}", field);
			}
			//remove or
			hqlFilterProject = hqlFilterProject.substring(0,hqlFilterProject.length() - 3);
			hqlFilterProjectOwner = hqlFilterProjectOwner.substring(0,hqlFilterProjectOwner.length() - 3);
			
			String statementOrder = "";
			if(lazyLoadingUserProject.getSorters().getField() != null) {
				statementOrder = "ORDER BY varProject.{field} {value}";
				statementOrder = statementOrder.replace("{field}", lazyLoadingUserProject.getSorters().getField())
						.replace("{value}", lazyLoadingUserProject.getSorters().getValue().toString());
			}
		    
			hqlInit = hqlInit.replace("{filterProject}", hqlFilterProject)
					.replace("{filterProjectOwner}", hqlFilterProjectOwner)
					.replace("{sortProject}", statementOrder);
			
		    Query query = session.createQuery(hqlInit);
		    query.setLong("memberId", idUserForProject);
		    query.setParameter("valueFilter","%" + lazyLoadingUserProject.getFilters().getValue() + "%");
		    query.setFirstResult(lazyLoadingUserProject.getStart());
		    query.setMaxResults(lazyLoadingUserProject.getLimit());
		    List<Project> projects = query.list();
		    
		    return projects;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public int countTotalLazyProjects(StateLazyLoading lazyLoadingProject) {
		String statementInit = "SELECT count(*) FROM Project p";
		
		String statementFilter = "";
		for(String field : lazyLoadingProject.getFilters().getField()) {
			statementFilter = statementFilter
					+ " lower(p.{field}) like lower(:valueFilter) or".replace("{field}", field);
		}
		//remove or
		statementFilter = statementFilter.substring(0,statementFilter.length() - 3);
		
		
		String finalStatement = statementInit + " WHERE (" + statementFilter + ") ";
		
		Long totalRow = (Long) sessionFactory.getCurrentSession()
				.createQuery(finalStatement)
				.setParameter("valueFilter", "%" + lazyLoadingProject.getFilters().getValue() + "%")
				.uniqueResult();
		return totalRow.intValue();
	}

	@Override
	public int countTotalLazyByMemberId(Long idUserForProject,
			StateLazyLoading lazyLoadingUserProject) {
		Session session = sessionFactory.getCurrentSession();
	    
	    String hqlInit = "SELECT count(DISTINCT p) FROM TeamMember tm "
	    		+ " INNER JOIN tm.team t"
	    		+ " INNER JOIN t.projects tp"
	    		+ " INNER JOIN tp.project p"
	    		+ " WHERE"
	    		+ " ( "
		    		+ "tm.member.memberId = :memberId"
		    		+ " AND tm.isAccepted=true"
		    		+ " AND p.owner.memberId != :memberId"
		    		+ " AND ({filterProject})"
	    		+ " ) "
	    		+ " OR p IN (SELECT projectOwner"
			    				+ " FROM Project projectOwner"
			    				+ " WHERE projectOwner.owner.memberId = :memberId AND ({filterProjectOwner})"
			    			+ ")";
	    
	    String hqlFilterProject = "";
	    String hqlFilterProjectOwner = "";
		for(String field : lazyLoadingUserProject.getFilters().getField()) {
			hqlFilterProject = hqlFilterProject
					+ " lower(p.{field}) like lower(:valueFilter) or".replace("{field}", field);
			hqlFilterProjectOwner = hqlFilterProjectOwner + " lower(projectOwner.{field}) like lower(:valueFilter) or".replace("{field}", field);
		}
		//remove or
		hqlFilterProject = hqlFilterProject.substring(0,hqlFilterProject.length() - 3);
		hqlFilterProjectOwner = hqlFilterProjectOwner.substring(0,hqlFilterProjectOwner.length() - 3);
		
		hqlInit = hqlInit.replace("{filterProject}", hqlFilterProject)
				.replace("{filterProjectOwner}", hqlFilterProjectOwner);
		
	    Query query = session.createQuery(hqlInit);
	    query.setLong("memberId", idUserForProject);
	    query.setParameter("valueFilter","%" + lazyLoadingUserProject.getFilters().getValue() + "%");
	    //List<Project> projects = query.list();
		Long totalRow = (Long) query.uniqueResult();
	    return totalRow.intValue();
	}

	@Override
	public List<Project> findByProductOwnerId(Long productOwnerId) 
	{
	    try {
		    Session session = sessionFactory.getCurrentSession();
		    String hql = "select p from Project p"
		    			+ " where p.isArchived = false and p.productOwnerIds like  :productOwnerId";
		    			//+ " :productOwnerId = any(string_to_array(p.productOwnerIds, ','))";
		    
		    Query query = session.createQuery(hql);
		    query.setString("productOwnerId", "%"+productOwnerId.longValue()+"%");
		    
		    List<Project> list = query.list();
		    return list;
		} catch (HibernateException e) {
			e.printStackTrace();
		    return null;
		}
	}

}
