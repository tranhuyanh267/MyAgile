package com.ant.myagile.dao.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.ant.myagile.dao.KanbanStatusDao;
import com.ant.myagile.dao.SprintDao;
import com.ant.myagile.model.KanbanIssue;
import com.ant.myagile.model.KanbanStatus;
import com.ant.myagile.model.Team;
import com.ant.myagile.utils.Utils;

@Repository
public class KanbanStatusDaoImpl implements KanbanStatusDao 
{
    	private static final Logger logger = Logger.getLogger(KanbanStatusDaoImpl.class);
	@Autowired
	SessionFactory sessionFactory;
	@Autowired
	SprintDao sprintDao;

	@Override
	public KanbanStatus getById(long id) 
	{
		return (KanbanStatus) sessionFactory.getCurrentSession().get( KanbanStatus.class, id);
	}

	@Override
	public boolean update(KanbanStatus kanbanStatus) 
	{
		try {
			sessionFactory.getCurrentSession().update(kanbanStatus);
			return true;
		} catch (Exception e) {
		    logger.error("update kanban error: " + e.getMessage());
			return false;
		}
	}

	@Override
	public long add(KanbanStatus kanbanStatus) {
		return (Long) sessionFactory.getCurrentSession().save(kanbanStatus);

	}

	@Override
	public boolean delete(long id) {
		try {
			sessionFactory.getCurrentSession().delete(getById(id));
			return true;
		} catch (Exception e) {
		    logger.error("delete kanban status error: " + e.getMessage());
			return false;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<KanbanStatus> findKanbanStatusByTeamId(long idTeam) 
	{
		List<KanbanStatus> listCombine = new ArrayList<KanbanStatus>();
		try {
			KanbanStatus statusStart = findToDoStatusByTeamId(idTeam);
			KanbanStatus statusDone = findDoneStatusByTeamId(idTeam);
			KanbanStatus statusAccepted = findAcceptedStatusByTeamId(idTeam);
			List<KanbanStatus> listStatusInProgress = sessionFactory
					.getCurrentSession()
					.createCriteria(KanbanStatus.class)
					.add(Restrictions.eq("team.teamId", idTeam))
					.add(Restrictions.eq("type", KanbanStatus.StatusType.IN_PROGRESS))
					.setCacheable(true).addOrder(Order.asc("statusId")).list();
			if (statusStart != null)
				listCombine.add(statusStart);
			if (!listStatusInProgress.isEmpty())
				listCombine.addAll(listStatusInProgress);
			if (statusDone != null)
				listCombine.add(statusDone);
			if (statusAccepted != null)
				listCombine.add(statusAccepted);
			return listCombine;
		} catch (HibernateException e) {
		    logger.error("findKanbanStatusByTeamId error: " + e.getMessage());
			return listCombine;
		}
	}

	@Override
	public KanbanStatus findToDoStatusByTeamId(long teamId) 
	{
		return (KanbanStatus) sessionFactory.getCurrentSession()
				.createCriteria(KanbanStatus.class)
				.add(Restrictions.eq("team.teamId", teamId))
				.add(Restrictions.eq("type", KanbanStatus.StatusType.START)).uniqueResult();
	}

	@Override
	public KanbanStatus findDoneStatusByTeamId(long teamId) 
	{
		return (KanbanStatus) sessionFactory.getCurrentSession()
				.createCriteria(KanbanStatus.class)
				.add(Restrictions.eq("team.teamId", teamId))
				.add(Restrictions.eq("type",KanbanStatus.StatusType.DONE)).uniqueResult();
	}

	@Override
	public KanbanStatus findAcceptedStatusByTeamId(long teamId) 
	{
		return (KanbanStatus) sessionFactory
				.getCurrentSession()
				.createCriteria(KanbanStatus.class)
				.add(Restrictions.or(Restrictions.eq("type",
						KanbanStatus.StatusType.ACCEPTED_HIDE), Restrictions.eq(
						"type",KanbanStatus.StatusType.ACCEPTED_SHOW)))
				.add(Restrictions.eq("team.teamId", teamId))
				.setCacheable(true).uniqueResult();

	}

	@SuppressWarnings("unchecked")
	@Override
	/**
	 * ignore idteam
	 */
	public List<KanbanIssue> getAllIssueByKanbanStatus(long idTeam, long kanbanStatusId, long kanbanSwimlineId) 
	{
		try {
			String hql = "select distinct i from KanbanIssue as i inner join fetch i.userStory as us inner join fetch us.project as p left join fetch i.pointRemains where i.kanbanStatus.statusId = :idstatus and i.kanbanSwimline.swimlineId = :idswim and i.isVoid = false order by p.projectId asc";
			Query query = sessionFactory.getCurrentSession().createQuery(hql);
			
			query.setParameter("idstatus", kanbanStatusId);
			query.setParameter("idswim", kanbanSwimlineId);
			List<KanbanIssue> lissue = query.list();
			return lissue;
			
		} catch (HibernateException e) {
		    logger.error("getAllIssueByKanbanStatus error: " + e.getMessage());
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	/**
	 * ignore idTeam
	 */
	public List<KanbanIssue> getAllIssueChildProgressByKanbanStatus(long idTeam, long kanbanStatusId, long kanbanSwimlineId) 
	{
		try {
			String hql = "select distinct i from KanbanIssue as i inner join fetch i.userStory as us inner join fetch us.project as p left join fetch i.pointRemains where i.kanbanStatus.statusId = :idstatus and i.isVoid = false and i.kanbanSwimline.swimlineId = :idswim and  (i.columnDone = :done or i.columnDone is null) order by p.projectId asc";
			Query query = sessionFactory.getCurrentSession().createQuery(hql);
			
			query.setParameter("idstatus", kanbanStatusId);
			query.setParameter("idswim", kanbanSwimlineId);
			query.setParameter("done", Boolean.FALSE);
			List<KanbanIssue> lissue = query.list();
			return lissue;

		} catch (HibernateException e) {
		    logger.error("getAllIssueChildProgressByKanbanStatus error: " + e.getMessage());
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	/**
	 * ignore id team
	 */
	public List<KanbanIssue> getAllIssueChildDoneByKanbanStatus(long idTeam, long kanbanStatusId, long kanbanSwimlineId) 
	{
		String hql = "select distinct i from KanbanIssue as i inner join fetch i.userStory as us inner join fetch us.project as p left join fetch i.pointRemains where i.kanbanStatus.statusId = :idstatus and i.isVoid = false and i.kanbanSwimline.swimlineId = :idswim and i.columnDone = :done order by p.projectId asc";
		Query query = sessionFactory.getCurrentSession().createQuery(hql);
		query.setParameter("idstatus", kanbanStatusId);
		query.setParameter("idswim", kanbanSwimlineId);
		query.setParameter("done", Boolean.TRUE);
		List<KanbanIssue> lissue = query.list();
		return lissue;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void updateIssueByTeamId(Long teamId, Long statusId) 
	{
		try {
			Session session = sessionFactory.getCurrentSession();
			Criteria cri = session.createCriteria(KanbanIssue.class);
			cri.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
			List<KanbanIssue> list = cri.add(Restrictions.eq("team.teamId", teamId))
					.add(Restrictions.eq("kanbanStatus.statusId", statusId)).list();
			List<KanbanStatus> listStatus = this.findKanbanStatusByTeamId(teamId);
			
			if (list.size() != 0) 
			{
				for (KanbanIssue is : list) 
				{
					is.setKanbanStatus(listStatus.get(listStatus.indexOf(getById(statusId)) - 1));
					session.update(is);
				}
			}
		} catch (Exception e) {
		    logger.error("updateIssueByTeamId error: " + e.getMessage());
			e.printStackTrace();
		}
		
	}

	@Override
	public List<KanbanStatus> getAllKanbanStatusByTeam(Team team)
	{
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(KanbanStatus.class);
		criteria.add(Restrictions.eq("team", team));
		
		List<KanbanStatus> kanbanStatusList = criteria.list();
		return kanbanStatusList;
	}

	@Override
	/**
	 * ignore id team
	 */
	public List<KanbanIssue> getAcceptedIssueNotDisappear(long teamId, long kanbanStatusId, long kanbanSwimlineId) 
	{
	    try {
			String hql = "SELECT DISTINCT i "
				+ "FROM KanbanIssue as i inner join fetch i.userStory as us inner join fetch us.project as p left join fetch i.pointRemains "
				+ "WHERE i.kanbanStatus.statusId = :idstatus "
				+ "AND i.kanbanSwimline.swimlineId = :idswim "
				+ "AND i.isVoid = false "
				+ "AND (date_trunc('day',to_timestamp(:toDay,'yyyy/MM/dd')) < date_trunc('day',to_timestamp(i.disappearDate,'yyyy/MM/dd')) OR  i.disappearDate = null)"
				+ "order by p.projectId asc";
			Query query = sessionFactory.getCurrentSession().createQuery(hql);
			
			query.setParameter("idstatus", kanbanStatusId);
			query.setParameter("idswim", kanbanSwimlineId);
			query.setParameter("toDay", Utils.converDateToString(new Date()));
			List<KanbanIssue> lissue = query.list();
			return lissue;
			
		} catch (HibernateException e) {
		    logger.error("getAcceptedIssueNotDisappear error: " + e.getMessage());
			return null;
		}
	}

	@Override
	public KanbanStatus findAcceptedStatusShowByTeamId(Long teamId) 
	{
		return (KanbanStatus) sessionFactory
				.getCurrentSession()
				.createCriteria(KanbanStatus.class)
				.add(Restrictions.eq("type",KanbanStatus.StatusType.ACCEPTED_SHOW))
				.add(Restrictions.eq("team.teamId", teamId))
				.setCacheable(true).uniqueResult();
	}

	@Override
	public KanbanStatus getKanbanStatusDoneByTeamId(Long teamId) 
	{
		return (KanbanStatus) sessionFactory
				.getCurrentSession()
				.createCriteria(KanbanStatus.class)
				.add(Restrictions.eq("type",KanbanStatus.StatusType.DONE))
				.add(Restrictions.eq("team.teamId", teamId))
				.uniqueResult();
	}

	@Override
	public KanbanStatus getKanbanStatusStartByTeamId(Long teamId) 
	{
		return (KanbanStatus) sessionFactory
				.getCurrentSession()
				.createCriteria(KanbanStatus.class)
				.add(Restrictions.eq("type",KanbanStatus.StatusType.START))
				.add(Restrictions.eq("team.teamId", teamId))
				.uniqueResult();
	}
}
