package com.ant.myagile.dao.impl;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.ant.myagile.dao.MemberDao;
import com.ant.myagile.dao.ProjectDao;
import com.ant.myagile.dao.TeamDao;
import com.ant.myagile.dao.TeamMemberDao;
import com.ant.myagile.dto.StateLazyLoading;
import com.ant.myagile.model.Member;
import com.ant.myagile.model.MemberRole;
import com.ant.myagile.model.Project;
import com.ant.myagile.model.Team;
import com.ant.myagile.model.TeamMember;
@Repository("MemberDao")
@Transactional
public class MemberDaoImpl implements MemberDao, Serializable 
{
	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = Logger.getLogger(MemberDaoImpl.class);
	
	@Autowired
	private SessionFactory sessionFactory;
	@Autowired
	private TeamDao teamDao;
	@Autowired
	private ProjectDao projectDao;
	@Autowired
	private TeamMemberDao teamMemberDao;

	@SuppressWarnings("unchecked")
	@Override
	@Transactional
	public List<TeamMember> findTeamMemberListByTeamId(Long id) {
		try {
			Criteria criteria = sessionFactory.getCurrentSession().createCriteria(TeamMember.class, "teamMember");
			criteria.createAlias("teamMember.member", "member");
			criteria.add(Restrictions.eq("member.accountLocked", false));
			criteria.add(Restrictions.eq("team", sessionFactory.getCurrentSession().get(Team.class, id)));
			criteria.addOrder(Order.asc("teammemberId"));
			criteria.setCacheable(true);
			List<TeamMember> memberList = new ArrayList<TeamMember>();
			memberList = criteria.list();
			return memberList;
		} catch (HibernateException e) {
			return null;
		}
	}

	@Override
	public void removeMemberFromTeam(Long teamMemberId) {
		try {
			Session session = sessionFactory.getCurrentSession();
			String hql = "delete from TeamMember tm where tm.teammemberId = :teamMemberId";
			Query query = session.createQuery(hql);
			query.setCacheable(true);
			query.setLong("teamMemberId", teamMemberId);
		} catch (HibernateException e) {
		}
	}

	@Override
	public Member getMemberByUsername(String username) {
		try {
			Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Member.class);
			criteria.add(Restrictions.eq("username", username));
			criteria.setCacheable(true);
			return (Member) criteria.uniqueResult();
		} catch (HibernateException e) {
			return null;
		}
	}

	@Override
	public boolean save(Member mem) {
		try {
			sessionFactory.getCurrentSession().save(mem);
			return true;
		} catch (HibernateException e) {
			return false;
		}
	}

	@Override
	public boolean delete(Member member) {
		try {
			MemberRole memberRole = new MemberRole(1l, member.getMemberId());
			sessionFactory.getCurrentSession().delete(memberRole);
			sessionFactory.getCurrentSession().delete(member);
			return true;
		} catch (HibernateException e) {
			return false;
		}
	}

	@Override
	public boolean update(Member member) {
		try {
			sessionFactory.getCurrentSession().update(member);
			return true;
		} catch (HibernateException e) {
			return false;
		}
	}

	@Override
	public String generatePasswordDefault() {
		try {
			return UUID.randomUUID().toString().split("-")[0];
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<Member> findMembersInScrumTeamByMemberId(long memberId) {
		Session session = sessionFactory.getCurrentSession();
		SQLQuery query = session.createSQLQuery("SELECT * FROM  myagile.getScrumTeamMember(:memberId);");
		query.setParameter("memberId", memberId);
		query.addEntity(Member.class);
		return query.list();
	}

	@Override
	public List<Member> findMembersForChatBoxByMemberId(long memberId) {
		try {
			Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Member.class);
			criteria.add(Restrictions.eq("accountLocked", false));
			criteria.setCacheable(true);
			List<Member> memberList = new ArrayList<Member>();
			memberList = criteria.list();
			return memberList;
		} catch (HibernateException e) {
			return null;
		}
	}

	@Override
	public boolean saveAndAddRoleMember(Member member, Long roleID) {
		try {
			sessionFactory.getCurrentSession().save(member);
			MemberRole mr = new MemberRole(roleID, member.getMemberId());
			sessionFactory.getCurrentSession().save(mr);
			return true;
		} catch (HibernateException e) {
			return false;
		}
	}

	@Override
	public List<Member> findMembersByTeam(Team team) {
		try {
			Criteria criteria = sessionFactory.getCurrentSession().createCriteria(TeamMember.class, "teamMember").createAlias("teamMember.member", "member").setCacheable(true).add(Restrictions.eq("team", team)).add(Restrictions.eq("member.accountLocked", false)).add(Restrictions.eq("member.isActive", true));
			@SuppressWarnings("unchecked")
			List<TeamMember> teamMemberList = criteria.list();
			List<Member> memberList = new ArrayList<Member>();
			if (teamMemberList.size() != 0) {
				for (TeamMember tm : teamMemberList) {
					memberList.add(tm.getMember());
				}
				return memberList;
			}
			return memberList;
		} catch (HibernateException e) {
			return null;
		}
	}

	@Override
	public Member findProjectOwner(Long projectId) {
		try {
			Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Project.class);
			criteria.add(Restrictions.eq("projectId", projectId));
			criteria.setCacheable(true);
			Project pro = (Project) criteria.list().get(0);
			return pro.getOwner();
		} catch (HibernateException e) {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Member findMemberById(long memberId) {
		try {
			Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Member.class).add(Restrictions.eq("memberId", memberId));
			List<Member> memberList = criteria.list();
			if (memberList.size() != 0) {
				return memberList.get(0);
			}
			return null;
		} catch (HibernateException e) {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Member> findDevelopmentMembersByTeamId(long teamId) {
		try {
			Session session = sessionFactory.getCurrentSession();
			String q = "SELECT m FROM Team t, Member m " + "INNER JOIN m.teamMembers tm " + "WHERE t.teamId = :teamId " + "AND tm.team.teamId = t.teamId " + "AND m.isActive = :isActive " + "AND m.accountLocked = :accountLocked " + "AND tm.isAccepted = :isAccepted";
			Query query = session.createQuery(q);
			query.setParameter("teamId", teamId);
			query.setParameter("isActive", true);
			query.setParameter("accountLocked", false);
			query.setParameter("isAccepted", true);
			query.setCacheable(true);
			List<Member> members = query.list();
			return members;
		} catch (HibernateException e) {
			return null;
		}
	}

	@Override
	public Member findMemberByToken(String token) {
		Member m = new Member();
		try {
			Session session = sessionFactory.getCurrentSession();
			Criteria cri = session.createCriteria(Member.class);
			m = (Member) cri.add(Restrictions.eq("token", token)).setCacheable(true).uniqueResult();
		} catch (Exception e) {
			return null;
		}
		return m;
	}

	@Override
	public List<Member> findMembersRelatedToMemberId(long memberId) {
		Member member = findMemberById(memberId);
		List<Long> teamIdList = new ArrayList<Long>();
		List<Long> projectIdList = new ArrayList<Long>();
		List<String> usernames = new ArrayList<String>(100);
		List<Member> members = new ArrayList<Member>();
		
		List<Team> teamsRelatedToMember = teamDao.findTeamsByMember(member);
		try {
			for (Team team : teamsRelatedToMember) {
				teamIdList.remove(team.getTeamId());
				teamIdList.add(team.getTeamId());
			}
		} catch (Exception e) {
			LOGGER.debug(e.getMessage());
		}
		
		List<Team> ownedTeams = teamDao.findTeamsByOwner(member);
		try {
			for (Team team : ownedTeams) {
				teamIdList.remove(team.getTeamId());
				teamIdList.add(team.getTeamId());
			}
		} catch (Exception e) {
			LOGGER.debug(e.getMessage());
		}
		
		for (Long teamId : teamIdList) {
			List<Project> projectsAssignedToTeam = projectDao.findProjectsByTeam(teamId);
			try {
				for (Project project : projectsAssignedToTeam) {
					projectIdList.remove(project.getProjectId());
					projectIdList.add(project.getProjectId());
				}
			} catch (Exception e) {
				LOGGER.debug(e.getMessage());
			}
		}
		
		for (Long projectId : projectIdList) {
			List<Team> teamsBelongToProject = teamDao.findTeamsbyProjectId(projectId);
			try {
				for (Team team : teamsBelongToProject) {
					teamIdList.remove(team.getTeamId());
					teamIdList.add(team.getTeamId());
				}
			} catch (Exception e) {
				LOGGER.debug(e.getMessage());
			}
		}
		
		for (Long teamId : teamIdList) {
			Member teamOwner = teamDao.findTeamByTeamId(teamId).getOwner();
			try {
				usernames.remove(teamOwner.getUsername());
				usernames.add(teamOwner.getUsername());
			} catch (Exception e) {
				LOGGER.debug(e.getMessage());
			}
		}
		for (Long teamId : teamIdList) {
			Team team = teamDao.findTeamByTeamId(teamId);
			List<Member> membersBelongToTeam = findMembersByTeam(team);
			try {
				for (Member m : membersBelongToTeam) {
					if (teamMemberDao.findTeamMemberByMemberAndTeam(m, team).getIsAccepted()) {
						usernames.remove(m.getUsername());
						usernames.add(m.getUsername());
					}
				}
			} catch (Exception e) {
				LOGGER.debug(e.getMessage());
			}
		}
		
		usernames.remove(member.getUsername());
		for (String username : usernames) {
			Member m = getMemberByUsername(username);
			if (m.isActive()) {
				members.add(m);
			}
		}
		return members;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Member findTeamOwnerByTeamId(long teamId) {
		try {
			Session session = sessionFactory.getCurrentSession();
			String queryTeamOwnerString = "SELECT o.username " + "from Team t " + "inner join t.owner o " + "where t.teamId = :teamId";
			Query queryTeamOwner = session.createQuery(queryTeamOwnerString);
			List<Member> result = queryTeamOwner.setCacheable(true).list();
			return result.get(0);
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	@Deprecated
	public List<Member> findMembersInTheSameTeamByMemberId(long memberId) {
		try {
			Member member = findMemberById(memberId);
			List<Long> projectIdList = new ArrayList<Long>();
			List<Long> teamIdList = new ArrayList<Long>();
			List<String> usernames = new ArrayList<String>();
			List<Project> projectsOwnedByMember = projectDao.findByOwnerId(memberId);
			try {
				for (Project project : projectsOwnedByMember) {
					projectIdList.remove(project.getProjectId());
					projectIdList.add(project.getProjectId());
				}
			} catch (Exception e) {
				LOGGER.debug(e.getMessage());
			}
			for (Long projectId : projectIdList) {
				List<Team> teamsBelongToProject = teamDao.findTeamsbyProjectId(projectId);
				try {
					for (Team team : teamsBelongToProject) {
						teamIdList.remove(team.getTeamId());
						teamIdList.add(team.getTeamId());
					}
				} catch (Exception e) {
					LOGGER.debug(e.getMessage());
				}
			}
			List<Team> teamsRelatedToMember = teamDao.findTeamsByMember(member);
			try {
				for (Team team : teamsRelatedToMember) {
					teamIdList.remove(team.getTeamId());
					teamIdList.add(team.getTeamId());
				}
			} catch (Exception e) {
				LOGGER.debug(e.getMessage());
			}
			List<Team> teamsOwnedByMember = teamDao.findTeamsByOwner(member);
			try {
				for (Team team : teamsOwnedByMember) {
					teamIdList.remove(team.getTeamId());
					teamIdList.add(team.getTeamId());
				}
			} catch (Exception e) {
				LOGGER.debug(e.getMessage());
			}
			for (Long teamId : teamIdList) {
				Team team = teamDao.findTeamByTeamId(teamId);
				List<Member> membersBelongToTeam = findMembersByTeam(team);
				try {
					for (Member m : membersBelongToTeam) {
						if (teamMemberDao.findTeamMemberByMemberAndTeam(m, team).getIsAccepted()) {
							usernames.remove(m.getUsername());
							usernames.add(m.getUsername());
						}
					}
				} catch (Exception e) {
					LOGGER.debug(e.getMessage());
				}
				usernames.remove(team.getOwner().getUsername());
				usernames.add(team.getOwner().getUsername());
			}
			usernames.remove(member.getUsername());
			List<Member> members = new ArrayList<Member>();
			for (String username : usernames) {
				Member m = getMemberByUsername(username);
				if (m.isActive()) {
					members.add(m);
				}
			}
			return members;
		} catch (Exception e) {
			LOGGER.debug(e.getMessage());
			return null;
		}
	}

	@Override
	public Member findMemberBySkype(String accountSkype) {
		Member m = new Member();
		try {
			Session session = sessionFactory.getCurrentSession();
			Criteria cri = session.createCriteria(Member.class);
			m = (Member) cri.add(Restrictions.eq("skype", accountSkype)).list().get(0);
		} catch (Exception e) {
			return null;
		}
		return m;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Member findMemberByEmail(String email) {
		try {
			Session session = sessionFactory.getCurrentSession();
			Criteria cri = session.createCriteria(Member.class);
			cri.add(Restrictions.eq("username", email.trim()));
			List<Member> memberList = cri.list();
			if (memberList.size() == 1) {
				return memberList.get(0);
			} else {
				return null;
			}
		} catch (Exception e) {
		}
		return null;
	}

	@Override
	public Member findMemberByLDapUsername(String lDapUsername) {
		try {
			Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Member.class);
			criteria.add(Restrictions.eq("lDapUsername", lDapUsername));
			criteria.setCacheable(true);
			return (Member) criteria.uniqueResult();
		} catch (HibernateException e) {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Member> findAll() {
		List<Member> result = new ArrayList<Member>();
		try {
			result = sessionFactory.getCurrentSession().createCriteria(Member.class).list();
		} catch (Exception e) {
			LOGGER.debug(e.getMessage());
		}
		return result;
	}

	@Override
	public List<Member> findDevelopeMembersByTeam(Team team) {
		try {
			Session session = sessionFactory.getCurrentSession();
			String q = "SELECT m FROM Team t, Member m " + "INNER JOIN m.teamMembers tm " + "WHERE t.teamId = :teamId " + "AND tm.team.teamId = t.teamId " + "AND m.isActive = :isActive " + "AND m.accountLocked = :accountLocked " + "AND tm.isAccepted = :isAccepted and tm.position='DEVELOPER'";
			Query query = session.createQuery(q);
			query.setParameter("teamId", team.getTeamId());
			query.setParameter("isActive", true);
			query.setParameter("accountLocked", false);
			query.setParameter("isAccepted", true);
			query.setCacheable(true);
			List<Member> members = query.list();
			return members;
		} catch (HibernateException e) {
			return new ArrayList<Member>();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Member> findMemberLazy(StateLazyLoading lazyLoadingMember) {
		String statementInit = "SELECT mem FROM Member mem";
		
		String statememberFilter = "";
		for(String field : lazyLoadingMember.getFilters().getField()){
			statememberFilter = statememberFilter 
					+ " lower(mem.{field}) like lower(:valueFilter) or".replace("{field}", field);
		}
		//remove or
		statememberFilter = statememberFilter.substring(0,statememberFilter.length() - 3);
		
		String statementOrder = "";
		if(lazyLoadingMember.getSorters().getField() != null){
			statementOrder = "ORDER BY mem.{field} {value}";
			statementOrder = statementOrder.replace("{field}", lazyLoadingMember.getSorters().getField())
					.replace("{value}", lazyLoadingMember.getSorters().getValue().toString());
		}
		
		String finalStatement = statementInit + " WHERE (" + statememberFilter + ") " + statementOrder;
		
		List<Member> members = sessionFactory.getCurrentSession()
				.createQuery(finalStatement)
				.setParameter("valueFilter", "%" + lazyLoadingMember.getFilters().getValue() + "%")
				.setFirstResult(lazyLoadingMember.getStart())
				.setMaxResults(lazyLoadingMember.getLimit())
				.list();
		
		return members;
	}

	@Override
	public int countTotalMemberLazy(StateLazyLoading lazyLoadingMember) {
		
		String statementInit = "SELECT count(*) FROM Member mem";
		
		String statememberFilter = "";
		for(String field : lazyLoadingMember.getFilters().getField()){
			statememberFilter = statememberFilter 
					+ " lower(mem.{field}) like lower(:valueFilter) or".replace("{field}", field);
		}
		//remove or
		statememberFilter = statememberFilter.substring(0,statememberFilter.length() - 3);
		
		String finalStatement = statementInit + " WHERE (" + statememberFilter + ") ";
		
		Long totalRow = (Long) sessionFactory.getCurrentSession()
				.createQuery(finalStatement)
				.setParameter("valueFilter", "%" + lazyLoadingMember.getFilters().getValue() + "%")
				.uniqueResult();
		return totalRow.intValue();
	}

	@Override
	public List<Member> getAllActiveMembers() 
	{
	    	try 
	    	{
        		Session session = sessionFactory.getCurrentSession();
        		String q = "SELECT m FROM Member m WHERE m.isActive = :isActive AND m.accountLocked = :accountLocked AND m.enabled = :isEnabled";
        		Query query = session.createQuery(q);
        		query.setParameter("isActive", true);
        		query.setParameter("accountLocked", false);
        		query.setParameter("isEnabled", true);
        		List<Member> members = query.list();
        		return members;
        	} catch (HibernateException e) {
        		return null;
        	}
	}
}
