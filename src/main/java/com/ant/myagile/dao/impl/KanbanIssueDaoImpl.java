package com.ant.myagile.dao.impl;

import java.util.ArrayList;
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
import org.springframework.transaction.annotation.Transactional;

import com.ant.myagile.dao.IssueDao;
import com.ant.myagile.dao.KanbanIssueDao;
import com.ant.myagile.dao.MemberIssueDao;
import com.ant.myagile.model.Issue;
import com.ant.myagile.model.KanbanIssue;
import com.ant.myagile.model.KanbanStatus;
import com.ant.myagile.model.Team;
import com.ant.myagile.model.UserStory;

@Repository
@Transactional
public class KanbanIssueDaoImpl implements KanbanIssueDao
{
	private static final Logger logger = Logger.getLogger(KanbanIssueDaoImpl.class);
	
	@Autowired
	private SessionFactory sessionFactory;
	@Autowired
	private MemberIssueDao memberIssueDao;
	@Autowired
	private IssueDao issueDao;

	@Override
	public boolean saveKanbanIssue(KanbanIssue issue) 
	{
		try 
		{
			sessionFactory.getCurrentSession().save(issue);
			return true;
		} catch (HibernateException e) {
			e.printStackTrace();
			logger.error("save kanban issue unsuccessful error: " + e.getMessage());
		}
		
		return false;
	}

	@Override
	public boolean updateKanbanIssue(KanbanIssue issue) 
	{
		try 
		{
			sessionFactory.getCurrentSession().update(issue);
			return true;
		} catch (HibernateException e) {
			e.printStackTrace();
			logger.error("update kanban issue unsuccessful",e);
		}
		return false;
	}

	@Override
	public void deleteKanbanIssue(KanbanIssue issue) 
	{
		try {
			memberIssueDao.deleteAllByIssue(issue.getIssueId());
			sessionFactory.getCurrentSession().delete(issue);
		} catch (HibernateException e) {
			e.printStackTrace();
			logger.error("delete kanban issue unsuccessful",e);
		}
	}

	@Override
	public long saveKanbanIssueAndGetId(KanbanIssue issue) 
	{
		return (Long) sessionFactory.getCurrentSession().save(issue);
	}

	@Override
	public KanbanIssue findKanbanIssueById(long issueId)
	{
		return (KanbanIssue) sessionFactory.getCurrentSession().get(KanbanIssue.class, issueId);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<KanbanIssue> findKanbanIssuesByUserStory(Long userStoryId) 
	{
		try {
			String hql = "select distinct ki from KanbanIssue as ki where ki.userStory.userStoryId=:idUs";
			return sessionFactory.getCurrentSession().createQuery(hql).setParameter("idUs", userStoryId).list();
		} catch (HibernateException e) {
		    logger.error("findKanbanIssuesByUserStory error: " + e.getMessage());
			return null;
		}
	}

	@Override
	public KanbanIssue findKanbanIssueParentByIssueChild(KanbanIssue issue) 
	{
		return null;
	}

	@Override
	public List<KanbanIssue> getListKanbanIssueByUserstory(long idUserstory) 
	{
		try {
		    Criteria criteria = this.sessionFactory.getCurrentSession().createCriteria(KanbanIssue.class)
		    		.add(Restrictions.eq("userStory.userStoryId", idUserstory))
		    		.addOrder(Order.asc("issueId"));
		    List<KanbanIssue> kabanIssueList = new ArrayList<KanbanIssue>();
		   
		    criteria.add(Restrictions.isNull("kanbanStatus"));
		    criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
		    kabanIssueList = criteria.list();
		    return kabanIssueList;
		} catch (HibernateException e) {
		    logger.error("getListKanbanIssueByUserstory error: " + e.getMessage());
		    return null;
		}
	}

	@Override
	public KanbanIssue checkDuplicate(String subject, long userStoryId, long id) 
	{
		try {
			Session session = sessionFactory.getCurrentSession();
			Criteria cr = session.createCriteria(KanbanIssue.class);
			KanbanIssue us;
			
			if(id != -1){
				 us = (KanbanIssue) cr.add(Restrictions.like("subject", subject)).setCacheable(true).add(Restrictions.eq("userStory.userStoryId", userStoryId)).add(Restrictions.ne("issueId", id)).uniqueResult();
			}else {
				us = (KanbanIssue) cr.add(Restrictions.like("subject", subject)).setCacheable(true).add(Restrictions.eq("userStory.userStoryId", userStoryId)).uniqueResult();
			}
			return us;
		} catch (HibernateException e) {
		    	logger.error("checkDuplicate error: " + e.getMessage());
			return null;
		}
		
	}

	@Override
	public List<KanbanIssue> getListKanbanIssueNotNullByUserstory( long idUserstory) 
	{
		try {
		    Criteria criteria = this.sessionFactory.getCurrentSession().createCriteria(KanbanIssue.class)
		    		.add(Restrictions.eq("userStory.userStoryId", idUserstory))
		    		.addOrder(Order.asc("issueId"));
		    List<KanbanIssue> kabanIssueList = new ArrayList<KanbanIssue>();
		    kabanIssueList = criteria.list();
		    return kabanIssueList;
		} catch (HibernateException e) {
		    logger.error("getListKanbanIssueNotNullByUserstory error: " + e.getMessage());
		    return null;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean checkAllKanbanIssueDoneOfUserstory(long idUserstory) 
	{
		try {
			String hql = "select ki from KanbanIssue as ki "
					+ "where ki.userStory.userStoryId=:idUserstory and (ki.kanbanStatus.type=:statusDone or ki.kanbanStatus.type=:statusAccept)";
			Query query = sessionFactory.getCurrentSession()
					.createQuery(hql);
			query.setParameter("idUserstory", idUserstory);
			query.setParameter("statusDone",KanbanStatus.StatusType.DONE);
			query.setParameter("statusAccept",KanbanStatus.StatusType.ACCEPTED_SHOW);
			List<KanbanIssue> lKanbanIssues = query.list();
			
			//get all ki from us
			List<KanbanIssue> lKi = this.findKanbanIssuesByUserStory(idUserstory);
			if(lKi.size() == lKanbanIssues.size() && !lKi.isEmpty()){
				return true;
			}else{
				return false;
			}
		} catch (HibernateException e) {
			logger.error("checkAllKanbanIssueDoneOfUserstory error: " + e.getMessage());
			return false;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public int calculateProgressUSForKanbanissue(UserStory us)
	{
		try {
			//get all kanban issue of us
			List<KanbanIssue> totalKanbanIssue = this.findKanbanIssuesByUserStory(us.getUserStoryId());
			//get all kanban issue done or accpet
			String hql = "select i from KanbanIssue as i where i.userStory.userStoryId=:idUs and (i.kanbanStatus.type=:statusDone or i.kanbanStatus.type=:statusAcceptShow)";
			List<KanbanIssue> totalKanbanIssueDone = sessionFactory.getCurrentSession()
					.createQuery(hql)
					.setParameter("idUs",us.getUserStoryId())
					.setParameter("statusDone",KanbanStatus.StatusType.DONE)
					.setParameter("statusAcceptShow",KanbanStatus.StatusType.ACCEPTED_SHOW)
					.list();
			if(!totalKanbanIssue.isEmpty() && !totalKanbanIssue.isEmpty()){
				return (int) Math.ceil((totalKanbanIssueDone.size()*100)/totalKanbanIssue.size());
			}else{
				return 0;
			}
		} catch (HibernateException e) {
			logger.error("calculate progress error",e);
			return 0;
		}
	}

	@Override
	public List<KanbanIssue> getAllKanbanIssuesByTeam(Team team) 
	{
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(KanbanIssue.class);
		criteria.add(Restrictions.eq("team", team));
		List<KanbanIssue> kanbanIssues = new ArrayList<KanbanIssue>();
		kanbanIssues = criteria.list();
		return  kanbanIssues;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean existKanbanIssueByUserStoryAndSubject(UserStory userStory, String subject) 
	{
		try {
			List<KanbanIssue> kanbanIssuesFromUserStoryAndSubject = sessionFactory.getCurrentSession()
					.createCriteria(KanbanIssue.class)
					.add(Restrictions.eq("userStory.userStoryId", userStory.getUserStoryId()))
					.add(Restrictions.eq("subject", subject))
					.list();
			if(!kanbanIssuesFromUserStoryAndSubject.isEmpty()) 
			{
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("check exist kanban issue by userstory and subject unsuccessful",e);
		}
		return false;
	}

	@Override
	public void updateKanbanIssueByUserStoryAndSubjectAndIssue( UserStory userStory, String subject, Issue issue) 
	{
		try {
			List<KanbanIssue> kanbanIssuesFromUserStoryAndSubject = sessionFactory.getCurrentSession()
					.createCriteria(KanbanIssue.class)
					.add(Restrictions.eq("userStory.userStoryId", userStory.getUserStoryId()))
					.add(Restrictions.eq("subject", subject))
					.list();
			if(!kanbanIssuesFromUserStoryAndSubject.isEmpty()) {
				for(KanbanIssue kanbanIssueItem : kanbanIssuesFromUserStoryAndSubject) {
					kanbanIssueItem.setSubject(issue.getSubject());
					kanbanIssueItem.setDescription(issue.getDescription());
					kanbanIssueItem.setNote(issue.getNote());
					kanbanIssueItem.setRemain(issue.getRemain());
					kanbanIssueItem.setEstimate(issue.getEstimate());
					updateKanbanIssue(kanbanIssueItem);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("update kanban issue by userstory and subject unsuccessful",e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void deleteAllKanbanIssueByUserStoryAndSubject(UserStory userStory, String subject) 
	{
		try {
			List<KanbanIssue> listKanbanIssueNeedDelete = sessionFactory.getCurrentSession()
					.createCriteria(KanbanIssue.class)
					.add(Restrictions.eq("userStory.userStoryId", userStory.getUserStoryId()))
					.add(Restrictions.eq("subject", subject))
					.list();
			
			for(KanbanIssue kanbanIssueItem : listKanbanIssueNeedDelete)
			{
				deleteKanbanIssue(kanbanIssueItem);
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("delete all kanban issue by userstory and subject unsuccessful",e);
		}
	}

	@Override
	public void deleteKanbanIssueByIssueId(Long issueId) 
	{
		try {
			KanbanIssue kanbanIssueDelete = (KanbanIssue) sessionFactory.getCurrentSession().createCriteria(KanbanIssue.class)
					.add(Restrictions.eq("issueOfLastSprint", issueId))
					.uniqueResult();
			if(kanbanIssueDelete != null) 
			{
				deleteKanbanIssue(kanbanIssueDelete);
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("delete kanban issue by issue id unsuccessful",e);
		}
	}

	@Override
	public void updateKanbanIssueByIssueId(Long issueId)
	{
		try {
			KanbanIssue kanbanIssueUpdate = (KanbanIssue) sessionFactory.getCurrentSession().createCriteria(KanbanIssue.class)
					.add(Restrictions.eq("issueOfLastSprint", issueId))
					.uniqueResult();
			Issue issueById = issueDao.findIssueById(issueId);
			if(kanbanIssueUpdate != null) {
				kanbanIssueUpdate.setSubject(issueById.getSubject());
				kanbanIssueUpdate.setDescription(issueById.getDescription());
				kanbanIssueUpdate.setNote(issueById.getNote());
				kanbanIssueUpdate.setPriority(issueById.getPriority());
				kanbanIssueUpdate.setType(issueById.getType());
				updateKanbanIssue(kanbanIssueUpdate);
			} else {
				//add new kanban issue
				KanbanIssue addKanbanIssue = new KanbanIssue();
				if(issueById.getParent() != null){
					addKanbanIssue.setUserStory(issueById.getParent().getUserStory());
				}else{
					addKanbanIssue.setUserStory(issueById.getUserStory());
				}
				addKanbanIssue.setSubject(issueById.getSubject());
				addKanbanIssue.setDescription(issueById.getDescription());
				addKanbanIssue.setNote(issueById.getNote());
				addKanbanIssue.setColumnDone(false);
				addKanbanIssue.setRemain(issueById.getRemain());
				addKanbanIssue.setEstimate(issueById.getEstimate());
				addKanbanIssue.setPointFormat("1");
				addKanbanIssue.setType("Task");
				addKanbanIssue.setIsSubIssue(true);
				addKanbanIssue.setIssueOfLastSprint(issueById.getIssueId());
				saveKanbanIssue(addKanbanIssue);
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("update kanban issue by issue id unsuccessful",e);
		}
	}

	@Override
	public List<KanbanIssue> getKanbanIssueProgressByUserStory( UserStory userStoryOfKanbanIssue)
	{
		try {
			List<KanbanIssue> kanbanIssues = sessionFactory.getCurrentSession()
					.createCriteria(KanbanIssue.class)
					.add(Restrictions.eq("userStory.userStoryId", userStoryOfKanbanIssue.getUserStoryId()))
					.add(Restrictions.not(Restrictions.isNull("kanbanSwimline")))
					.add(Restrictions.not(Restrictions.isNull("kanbanStatus")))
					.list();
			return kanbanIssues;
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("error getKanbanIssueProgressByUserStory ",e);
		}
		return new ArrayList<KanbanIssue>();
	}

	@Override
	public KanbanIssue findKanbanIssueIsNotSubIssueOfUserStory( UserStory userStory) 
	{
		KanbanIssue kanbanIssue = null;
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(KanbanIssue.class);
		criteria.add(Restrictions.eq("userStory.userStoryId", userStory.getUserStoryId()))
		.add(Restrictions.eq("isSubIssue", false));
		
		if(criteria.list().size() > 0){
			kanbanIssue = (KanbanIssue) criteria.uniqueResult();
		}
		return kanbanIssue;
	}

	@Override
	public void deleteAllKanbanIssueByUserStoryID(Long userStoryId) 
	{
		String hql = "DELETE"
				+ " FROM KanbanIssue ki"
				+ " WHERE ki.userStory.id = :userStoryId";
		sessionFactory.getCurrentSession().createQuery(hql).setParameter("userStoryId", userStoryId).executeUpdate();
	}
	
	@Override
	public void setVoidAllKanbanIssueWhenSetVoidUserStory(Long userStoryId)
	{
		List<KanbanIssue> listKanbanIssueNeedToSetVoid = findKanbanIssuesByUserStory(userStoryId);
		if(listKanbanIssueNeedToSetVoid.size() > 0){
			setVoidAllKanbanIssue(listKanbanIssueNeedToSetVoid);
		}
	}
	
	@Override
	public void setVoidAllKanbanIssue(List<KanbanIssue> listKanbanIssue)
	{
		try {
			String hql = "UPDATE KanbanIssue ki"
					+ " SET ki.isVoid = true"
					+ " WHERE ki IN :kiList";
			sessionFactory.getCurrentSession().createQuery(hql)
				.setParameterList("kiList",listKanbanIssue).executeUpdate();
		} catch (Exception e) {
			logger.error("Error at setVoidAllKanbanIssue: " + listKanbanIssue, e);
		}
	}
	
	@Override
	public void destroyVoidAllKanbanIssueWhenDestroyVoidUserStory(Long userStoryId)
	{
		List<KanbanIssue> listKanbanIssueNeedToDestroyVoid = findKanbanIssuesByUserStory(userStoryId);
		if(listKanbanIssueNeedToDestroyVoid.size() > 0){
			destroyVoidAllKanbanIssue(listKanbanIssueNeedToDestroyVoid);
		}
	}
	
	@Override
	public void destroyVoidAllKanbanIssue(List<KanbanIssue> listKanbanIssue)
	{
		try {
			String hql = "UPDATE KanbanIssue ki"
					+ " SET ki.isVoid = false"
					+ " WHERE ki IN :kiList";
			sessionFactory.getCurrentSession().createQuery(hql)
				.setParameterList("kiList", listKanbanIssue).executeUpdate();
		} catch (Exception e) {
			logger.error("Error at destroyVoidAllKanbanIssue: " + listKanbanIssue, e);
		}
	}
}
