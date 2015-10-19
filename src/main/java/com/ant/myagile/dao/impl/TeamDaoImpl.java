package com.ant.myagile.dao.impl;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Property;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.ant.myagile.dao.MemberDao;
import com.ant.myagile.dao.ProjectDao;
import com.ant.myagile.dao.TeamDao;
import com.ant.myagile.model.Member;
import com.ant.myagile.model.Project;
import com.ant.myagile.model.Sprint;
import com.ant.myagile.model.Team;
import com.ant.myagile.model.TeamMember;
import com.ant.myagile.model.TeamProject;

@Repository("teamDao")
public class TeamDaoImpl implements TeamDao 
{
    private static final Logger logger = Logger.getLogger(TeamDaoImpl.class);

    @Autowired
    private ProjectDao projectDao;
    @Autowired
    private MemberDao memberDao;

    @Autowired
    private SessionFactory sessionFactory;

    @Override
    public boolean save(Team team) {
	try {
	    sessionFactory.getCurrentSession().save(team);
	    return true;
	} catch (HibernateException e) {
	    return false;
	}
    }

    @Override
    public boolean delete(Team team) {
	try {
	    sessionFactory.getCurrentSession().delete(team);
	    return true;
	} 
	catch (HibernateException ex) 
	{
	    logger.error("delete team error: " + ex.getMessage());
	    return false;
	}
    }

    @Override
    public Team findTeamByTeamId(long teamId) {
	try {
	    Criteria crit = sessionFactory.getCurrentSession().createCriteria(Team.class);
	    crit.add(Restrictions.eq("teamId", teamId));
	    crit.setCacheable(true);
	    Team team = (Team) crit.uniqueResult();
	    return team;
	} 
	catch (HibernateException ex) 
	{
	    logger.error("findTeamByTeamId error: " + ex.getMessage());
	    return null;
	}
    }

    @Override
    public boolean update(Team team) 
    {
	try {
	    sessionFactory.getCurrentSession().update(team);
	    return true;
	} 
	catch (HibernateException ex) 
	{
	    logger.error("update team error: " + ex.getMessage());
	    return false;
	}
    }

    @SuppressWarnings("unchecked")
    @Override
    @Deprecated
    public List<Team> findTeamsbyProjectIdAndMemberId(long pid, long uid) 
    {
	Project project = projectDao.findById(pid);
	Member projectOwner = project.getOwner();
	Member member = memberDao.findMemberById(uid);
	List<Team> teams = findTeamsByProject(project);
	if (projectOwner.getMemberId().equals(member.getMemberId())) {
	    return teams;
	} else {
	    String hql = "select distinct t " + "from TeamProject tp " + "inner join tp.team t " + "inner join t.members tm " + "inner join tp.project p " + "where p = :projectId and " + "(p.owner = :memberId or tm.member = :memberId)";
	    Session session = sessionFactory.getCurrentSession();
	    Query query = session.createQuery(hql);
	    query.setLong("projectId", pid);
	    query.setLong("memberId", uid);
	    List<Team> list = new ArrayList<Team>();
	    list = query.list();
	    return list;
	}
    }

    @Override
    public List<Team> findTeamsByProjectIdAndMemberId(long pid, long uid) 
    {
	try 
	{
	    Project project = projectDao.findById(pid);
	    
	    if(null == project)
	    {
	    	return null;
	    }
	    
	    Member projectOwner = project.getOwner();
	    Member member = memberDao.findMemberById(uid);
	    List<Team> teams = findTeamsByProject(project);

	    if (projectOwner.getMemberId().equals(member.getMemberId()))
	    {
		return teams;
	    } else {
		List<Team> teamsAssignedToMember = findTeamsByMember(member);
		List<Team> teamsOfOwner = findTeamsByOwner(member);
		Set<Team> teamsRelatedToMember = new LinkedHashSet<Team>();
		for (Team i : teams) {
		    for (Team j : teamsAssignedToMember) {
			if (i.getTeamId().equals(j.getTeamId())) {
			    teamsRelatedToMember.add(i);
			}
		    }
		    for (Team k : teamsOfOwner) {
			if (i.getTeamId().equals(k.getTeamId())) {
			    teamsRelatedToMember.add(i);
			}
		    }
		}
		return new ArrayList<Team>(teamsRelatedToMember);
	    }
	} catch (HibernateException ex) {
	    logger.error("findTeamsByProjectIdAndMemberId error: " + ex.getMessage());
	    return null;
	} catch (IndexOutOfBoundsException ex) {
	    logger.error("findTeamsByProjectIdAndMemberId error: " + ex.getMessage());
	    return null;
	} catch (Exception ex) {
	    logger.error("findTeamsByProjectIdAndMemberId error: " + ex.getMessage());
	    return null;
	}
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Team> findTeamsByMember(Member member) 
    {
	try {
		DetachedCriteria detachedCriteria = DetachedCriteria.forClass(Team.class);
		detachedCriteria.setProjection(Projections.property("id"));
		detachedCriteria.add(Restrictions.eq("owner", member));
		
	    Criteria criteria = sessionFactory.getCurrentSession().createCriteria(TeamMember.class).add(Restrictions.eq("member", member)).add(Restrictions.eq("isAccepted", true));
	    criteria.add(Property.forName("team.id").notIn(detachedCriteria));
	    List<TeamMember> teamMemberList = criteria.list();
	    List<Team> teams = new ArrayList<Team>();
	    for (TeamMember tm : teamMemberList) {
		teams.add(tm.getTeam());
	    }
	    sessionFactory.getCurrentSession().flush();
	    return teams;
	} catch (HibernateException ex) {
	    logger.error("findTeamsByMember error: " + ex.getMessage());
	    return null;
	}
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Team> findTeamsByOwner(Member owner) 
    {
	try {
	    Session session = sessionFactory.getCurrentSession();
	    Criteria criteria = session.createCriteria(Team.class);
	    criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
	    List<Team> list = criteria.add(Restrictions.eq("owner", owner)).addOrder(Order.asc("teamId")).list();
	    return list;
	} catch (HibernateException ex) {
	    logger.error("findTeamsByOwner error: " + ex.getMessage());
	    return null;
	}
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Team> findTeamsbyProjectId(long projectId) 
    {
	try 
	{
	    Session session = sessionFactory.getCurrentSession();
	    String hql = "select tp.team " + "from TeamProject tp inner join tp.project p " + "where p.projectId = :projectId";
	    Query query1 = session.createQuery(hql);
	    query1.setLong("projectId", projectId);
	    query1.setCacheable(true);
	    
	    List<Team> list = query1.list();
	    List<Team> resultList = new ArrayList<Team>();
	    
	    if (list != null && list.size() > 0)
	    {
		for (Team team : list) 
		{
		    if (team.getValidTo() == null) //validTo = null => team is not hidden. 
		    {
			resultList.add(team);
		    }
		}
	    }
	    
	    return resultList;
	} catch (HibernateException ex) {
	    logger.error("findTeamsbyProjectId error: " + ex.getMessage());
	    return null;
	}
    }

    @Override
    public List<Team> findTeamsByProject(Project p) 
    {
	try 
	{
	    Criteria criteria = sessionFactory.getCurrentSession().createCriteria(TeamProject.class).add(Restrictions.eq("project", p));
	    @SuppressWarnings("unchecked")
	    List<TeamProject> teamProjectList = criteria.list();
	    List<Team> teams = new ArrayList<Team>();
	    
	    for (TeamProject tp : teamProjectList) 
	    {
		if (tp.getEndDate() == null && tp.getTeam() != null && tp.getTeam().getValidTo() == null) //validTo = null => team is not hidden. 
		{
		    //team is not hidden
		    teams.add(tp.getTeam());
		}
	    }
	    sessionFactory.getCurrentSession().flush();
	    return teams;
	} catch (HibernateException ex) {
	    logger.error("findTeamsByProject error: " + ex.getMessage());
	    return null;
	}
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean checkMailGroupExisted(Team newTeam) 
    {
	try 
	{
	    Session session = sessionFactory.getCurrentSession();
	    Criteria cr = session.createCriteria(Team.class);
	    List<Team> list = cr.add(Restrictions.eq("mailGroup", newTeam.getMailGroup())).list();
	    if (list.isEmpty()) {
		return true;
	    }
	    return false;
	} catch (HibernateException ex) {
	    logger.error("checkMailGroupExisted error: " + ex.getMessage());
	    return false;
	}
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean checkTeamNameExisted(Team newTeam) 
    {
	try 
	{
	    Session session = sessionFactory.getCurrentSession();
	    Criteria cr = session.createCriteria(Team.class);
	    List<Team> list = cr.add(Restrictions.eq("teamName", newTeam.getTeamName())).add(Restrictions.eq("owner", newTeam.getOwner())).list();
	    if (list.isEmpty()) {
		return true;
	    }
	    return false;
	} catch (HibernateException ex) {
	    logger.error("checkTeamNameExisted error: " + ex.getMessage());
	    return false;
	}
    }

    @SuppressWarnings("unchecked")
    @Override
    public int countTeamByProject(Long projectId) 
    {
	try 
	{
	    Session session = sessionFactory.getCurrentSession();
	    String hql = "select tp.team " + "from TeamProject tp inner join tp.project p " + "where p.projectId = :projectId";
	    Query query1 = session.createQuery(hql);
	    query1.setLong("projectId", projectId);
	    List<Team> list = query1.list();
	    return list.size();
	} catch (HibernateException ex) {
	    logger.error("countTeamByProject error: " + ex.getMessage());
	    return 0;
	}
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Team> findAllTeam() 
    {
	try 
	{
	    Criteria crit = sessionFactory.getCurrentSession().createCriteria(Team.class).add(Restrictions.isNull("validTo")); //validTo = null => team is not hidden. 
	    crit.setCacheable(true);
	    return crit.list();
	} catch (HibernateException ex) {
	    logger.error("findAllTeam error: " + ex.getMessage());
	    return null;
	}

    }

    @Override
    public Team findTeamByGroupMail(String mailGroup) 
    {
	try {
	    Criteria crit = sessionFactory.getCurrentSession().createCriteria(Team.class);
	    crit.add(Restrictions.eq("mailGroup", mailGroup));
	    crit.setCacheable(true);
	    return (Team) crit.uniqueResult();
	} catch (HibernateException ex) {
	    logger.error("findTeamByGroupMail error: " + ex.getMessage());
	    return null;
	}
    }

	@Override
	public Sprint getSprintOfTeamInCurrentTime(long teamId) 
	{
	    try 
	    {
		String hql = "select s from Sprint s where s.team.teamId = :teamId and :now between s.dateStart and s.dateEnd";
		DateFormat dateFormatCurrentDate = new SimpleDateFormat("yyyy-MM-dd");
		Date date = new Date();
		Calendar c = Calendar.getInstance(); 
		c.setTime(date); 
		c.add(Calendar.DATE, 1);
		date = c.getTime();
		
		date = dateFormatCurrentDate.parse(dateFormatCurrentDate.format(date));
		
		//add date plus one, because the last day is the date preview
		Sprint sprint = (Sprint) sessionFactory.getCurrentSession().createQuery(hql).setParameter("now",date).setParameter("teamId", teamId).uniqueResult();
		return sprint;
	    } catch (Exception ex) {
		    logger.error("getSprintOfTeamInCurrentTime error: " + ex.getMessage());
	    }
	    
	    return null;
	}

	@Override
	public List<Team> findAllTeamsbyProjectId(long projectId) 
	{
	    try 
		{
		    Session session = sessionFactory.getCurrentSession();
		    String hql = "select tp.team from TeamProject tp inner join tp.project p " + "where p.projectId = :projectId and tp.endDate is null";
		    Query query1 = session.createQuery(hql);
		    query1.setLong("projectId", projectId);
		    query1.setCacheable(true);
		    
		    @SuppressWarnings("unchecked")
		    List<Team> list = query1.list();
		    return list;
		} catch (HibernateException ex) {
		    logger.error("findTeamsbyProjectId error: " + ex.getMessage());
		    return null;
		}
	}
}
