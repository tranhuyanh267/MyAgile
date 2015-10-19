package com.ant.myagile.dao.impl;

import java.io.Serializable;
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
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ant.myagile.dao.AttachmentDao;
import com.ant.myagile.dao.IssueDao;
import com.ant.myagile.dao.KanbanIssueDao;
import com.ant.myagile.dao.UserStoryDao;
import com.ant.myagile.dto.LazyFilter;
import com.ant.myagile.dto.StateLazyLoading;
import com.ant.myagile.model.Attachment;
import com.ant.myagile.model.Issue;
import com.ant.myagile.model.KanbanIssue;
import com.ant.myagile.model.KanbanStatus;
import com.ant.myagile.model.UserStory;
import com.ant.myagile.model.UserStory.StatusType;
import com.ant.myagile.service.IssueService;
import com.ant.myagile.utils.Utils;

@Repository("userStoryDao")
@Transactional
public class UserStoryDaoImpl implements UserStoryDao, Serializable {
	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = Logger.getLogger(UserStoryDaoImpl.class);

	@Autowired
	private SessionFactory sessionFactory;
	@Autowired
	private IssueDao issueDao;
	@Autowired
	private IssueService issueService;
	@Autowired
	private KanbanIssueDao kanbanIssueDao;
	@Autowired
	private AttachmentDao attachmentDao;
	@Autowired
	private Utils utils;

	@SuppressWarnings("unchecked")
	@Override
	public List<UserStory> findAllUserStoryByProjectId(long projectId) {
		try {
			Session session = sessionFactory.getCurrentSession();
			Criteria cri = session.createCriteria(UserStory.class);
			cri.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
			cri.addOrder(Order.desc("value"));
			cri.addOrder(Order.desc("risk"));
			cri.setCacheable(true);
			cri.add(Restrictions.eq("project.projectId", projectId));
			return cri.list();
		} catch (HibernateException e) {
			return null;
		}
	}

	@Override
	public boolean create(UserStory userStory) {
		try {
			Session session = sessionFactory.getCurrentSession();
			session.save(userStory);
			return true;
		} catch (HibernateException e) {
			return false;
		}
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
	public boolean updateUserStory(UserStory userStory) {
		try {
			Session session = sessionFactory.getCurrentSession();
			session.merge(userStory);
			return true;
		} catch (HibernateException e) {
			LOGGER.error("Error: " + e.getMessage());
			return false;
		}

	}

	@Override
	@Transactional
	public boolean deleteUserStory(long userStoryId) {
		try {
			UserStory userStory = (UserStory) sessionFactory.getCurrentSession().get(UserStory.class, userStoryId);
			if (userStory != null) {
				sessionFactory.getCurrentSession().delete(userStory);
			}
			return true;
		} catch (HibernateException e) {
			return false;
		}
	}

	@Override
	public UserStory checkExistUserStory(String name, long projectId) {
		try {
			Session session = sessionFactory.getCurrentSession();
			Criteria cr = session.createCriteria(UserStory.class);
			UserStory us = (UserStory) cr.add(Restrictions.like("name", name)).setCacheable(true).add(Restrictions.eq("project.projectId", projectId)).uniqueResult();
			return us;
		} catch (HibernateException e) {
			return null;
		}
	}

	@Override
	public UserStory findUserStoryById(long userStoryId) {
		try {
			return (UserStory) sessionFactory.getCurrentSession().get(UserStory.class, userStoryId);
		} catch (HibernateException e) {
			return null;
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public List<UserStory> findUserStoriesByProjectId(Long projectId) {
		try {
			Session session = sessionFactory.getCurrentSession();
			Criteria cr = session.createCriteria(UserStory.class);
			cr.setCacheable(true);
			cr.add(Restrictions.eq("project.projectId", projectId));
			return cr.list();
		} catch (HibernateException e) {
			return null;
		}

	}

	@Override
	public UserStory findLastUserStoryInProject(Long projectId) {
		try {
			Session session = sessionFactory.getCurrentSession();
			Criteria cri = session.createCriteria(UserStory.class);
			cri.add(Restrictions.eq("project.projectId", projectId));
			cri.setCacheable(true);
			cri.setMaxResults(1);
			cri.addOrder(Order.desc("userStoryId"));
			return (UserStory) cri.uniqueResult();
		} catch (HibernateException e) {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<UserStory> findUserStoriesByCriteria(List<UserStory.StatusType> selectedFilter, long projectId, String sortField, int typeOfSort, String searchedKeyWord) {
		List<UserStory> listUs = new ArrayList<UserStory>();
		if (selectedFilter.size() == 0) {
			return listUs;
		}
		try {
			Criteria cri = sessionFactory.getCurrentSession().createCriteria(UserStory.class);
			cri.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
			cri.setCacheable(true);
			cri.add(Restrictions.eq("project.projectId", projectId));
			cri.add(Restrictions.in("status", selectedFilter));
			if (typeOfSort == 0) {
				cri.addOrder(Order.desc(sortField));
			} else {
				cri.addOrder(Order.asc(sortField));
			}
			if (!searchedKeyWord.equals("")) {
				String sql = "to_char(this_.id, 'FM9999999MI') LIKE '" + searchedKeyWord + "%' OR name ILIKE '%" + searchedKeyWord + "%'";
				cri.add(Restrictions.sqlRestriction(sql));
			}
			listUs.addAll(cri.list());
		} catch (HibernateException e) {
			LOGGER.error("Error: " + e.getMessage());
		}
		return listUs;
	}

	@Override
	public UserStory findUserStoryByIssue(Issue issue) {
		Session session = sessionFactory.getCurrentSession();
		Criteria cri = session.createCriteria(UserStory.class);
		try {
			if (issue.getParent() == null) {
				cri.add(Restrictions.eq("userStoryId", issue.getUserStory().getUserStoryId()));
			} else {
				Issue issueParent = issueDao.findIssueParentByIssueChild(issue);
				cri.add(Restrictions.eq("userStoryId", issueParent.getUserStory().getUserStoryId()));
			}

			UserStory userStory = (UserStory) cri.uniqueResult();
			return userStory;

		} catch (HibernateException e) {
			LOGGER.error("Error: " + e.getMessage());
			return null;
		}
	}

	@Override
	public List<UserStory> findAllUserStoryToDoByProjectId(long projectId) {
		try {
			Session session = sessionFactory.getCurrentSession();
			Criteria cri = session.createCriteria(UserStory.class);
			cri.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
			cri.addOrder(Order.desc("userStoryId"));
			cri.setCacheable(true);
			cri.add(Restrictions.eq("project.projectId", projectId));
			cri.add(Restrictions.eq("status", UserStory.StatusType.TODO));
			return cri.list();
		} catch (HibernateException e) {
			return null;
		}
	}

	@Override
	public List<UserStory> findAllUserStoryByIdList(List<Long> userStoryIdList) {
		try {
			Session session = sessionFactory.getCurrentSession();
			Criteria criUS = session.createCriteria(UserStory.class);
			criUS.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
			criUS.addOrder(Order.desc("userStoryId"));
			criUS.setCacheable(true);
			criUS.add(Restrictions.in("id", userStoryIdList));
			return criUS.list();
		} catch (HibernateException e) {
			return null;
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<UserStory> findAllUserStoryToDoAndProgress(long idProject) {
		try {
			String hql = "select u from UserStory as u inner join fetch u.project as p where p.projectId =:idProject and (u.status =:todo or u.status =:progress) order by u.value desc";
			Query query = sessionFactory.getCurrentSession().createQuery(hql);
			query.setParameter("idProject",idProject);
			query.setParameter("todo",UserStory.StatusType.TODO);
			query.setParameter("progress",UserStory.StatusType.IN_PROGRESS);
			return query.list();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
	}

	@Override
	public UserStory checkUserStoryForEdit(String name, long projectId,
			long userStoryID) {
		try {
			Session session = sessionFactory.getCurrentSession();
			Criteria cr = session.createCriteria(UserStory.class);
			UserStory us = (UserStory) cr.add(Restrictions.like("name", name)).setCacheable(true).add(Restrictions.eq("project.projectId", projectId)).add(Restrictions.ne("userStoryId", userStoryID)).uniqueResult();
			return us;
		} catch (HibernateException e) {
			return null;
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<UserStory> findRemainingUserStoriesByProject(long selectedProjectId) {
		List<UserStory> remainingUserStoriesList = new ArrayList<UserStory>();
		try {
			remainingUserStoriesList = sessionFactory.getCurrentSession()
					.createQuery("SELECT userStory FROM UserStory userStory"
							+ " WHERE userStory.project.projectId = :selectedProjectId"
							+ " AND (userStory.status = :statusToDo OR userStory.status = :statusInProgress)")
					.setParameter("selectedProjectId", selectedProjectId)
					.setParameter("statusToDo", UserStory.StatusType.TODO)
					.setParameter("statusInProgress", UserStory.StatusType.IN_PROGRESS)
					.list();
		} catch (Exception exception) {
			LOGGER.error("Error at findRemainingUserStoriesByProject", exception);
		}
		
		return remainingUserStoriesList;
	}

	@SuppressWarnings({ "unchecked" })
	@Override
	public List<UserStory> loadLazyUserstoryProjectId(long projectId,
			StateLazyLoading stateUserstoryList) {
		
		String hql = "SELECT userStory FROM UserStory userStory"
				+ " WHERE userStory.project.projectId = :projectId";
		
		//create fielter statement
		String statementFilter = "";
		for(String field : stateUserstoryList.getFilters().getField()){
			statementFilter = statementFilter 
					+ " lower( cast(userStory.{field} as text )) like lower(cast(:valueFilter as text)) or ".replace("{field}", field);
		}
		//remove or
		statementFilter = statementFilter.substring(0,statementFilter.length() - 3);
		
		//crate order statement
		String statementOrder = "";
		if(stateUserstoryList.getSorters().getField() != null){
			statementOrder = " ORDER BY userStory.{field} {value} ";
			statementOrder = statementOrder.replace("{field}", stateUserstoryList.getSorters().getField())
					.replace("{value}", stateUserstoryList.getSorters().getValue().toString());
		}
		//create status in list
		String statementStatusInList = "";
		if(stateUserstoryList.getFilterInList() != null){
			statementStatusInList = " userStory.{field} IN (:listStatus) ".replace("{field}", stateUserstoryList.getFilterInList().getField());
		}
		List<UserStory.StatusType> userStoryStatus = new ArrayList<UserStory.StatusType>();
		List<Object> objectStatus = (List<Object>) stateUserstoryList.getFilterInList().getValues();
		for(Object objectItem : objectStatus){
			userStoryStatus.add(UserStory.StatusType.valueOf(objectItem.toString()));
		}
		if(userStoryStatus.size() <= 0){
			return new ArrayList<UserStory>();
		}
		String finalHql = " " + hql + " AND ("  + statementFilter + ") " + " AND " + statementStatusInList + statementOrder +" ";
		List<UserStory> lazyUserstories = sessionFactory.getCurrentSession()
				.createQuery(finalHql)
				.setParameter("projectId", projectId)
				.setParameter("valueFilter", "%" + stateUserstoryList.getFilters().getValue().toString().trim() + "%")
				.setParameterList("listStatus", userStoryStatus)
				.setFirstResult(stateUserstoryList.getStart())
				.setMaxResults(stateUserstoryList.getLimit())
				.list();
		return lazyUserstories;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<UserStory> findLazyUserStoryByProjectNotInSprint(
			long projectId, long sprintId,
			StateLazyLoading lazyLoadingProductBacklogs) {
		try {
			String hqlExceptUserStories = "SELECT userStory FROM Issue issue"
					+ " INNER JOIN issue.userStory userStory"
					+ " INNER JOIN issue.sprint sprint"
					+ " WHERE sprint.sprintId = :sprintId";
			
			List<UserStory> exceptUserStories = sessionFactory.getCurrentSession()
					.createQuery(hqlExceptUserStories)
					.setParameter("sprintId", sprintId)
					.list();
			
			
			
			String hqlInit = "SELECT userStory FROM UserStory userStory"
					+ " INNER JOIN userStory.project project"
					+ " WHERE project.projectId = :projectId";
			
			//create fielter statement
			String statementFilter = "";
			for(String field : lazyLoadingProductBacklogs.getFilters().getField()){
				statementFilter = statementFilter 
						+ " lower( cast(userStory.{field} as text )) like lower(cast(:valueFilter as text)) or ".replace("{field}", field);
		
			}
			//remove or
			statementFilter = statementFilter.substring(0,statementFilter.length() - 4);
			
			//create in list in statement
			String statementStatusInList = "";
			if(lazyLoadingProductBacklogs.getFilterInList() != null) {
				statementStatusInList = " userStory.{field} IN (:listStatus)".replace("{field}", lazyLoadingProductBacklogs.getFilterInList().getField());
			}
			List<UserStory.StatusType> userStoryStatus = new ArrayList<UserStory.StatusType>();
			List<Object> objectStatus = (List<Object>) lazyLoadingProductBacklogs.getFilterInList().getValues();
			for(Object objectItem : objectStatus){
				userStoryStatus.add(UserStory.StatusType.valueOf(objectItem.toString()));
			}
			//create statement except userstories
			String statementExceptUserStories = " userStory NOT IN (:exceptUserStories) ";
			//create order by value
			String statementOrder = " ORDER BY userStory.value DESC,userStory.name ASC ";
			
			String finalStatement = hqlInit + " AND (" + statementFilter + ") " + " AND " + statementStatusInList + " " + statementOrder;
			if(!exceptUserStories.isEmpty()) {
				finalStatement = hqlInit + " AND (" + statementFilter + ") " + " AND " + statementStatusInList + " AND " + statementExceptUserStories + " " + statementOrder;
			}
			Query query = sessionFactory.getCurrentSession()
					.createQuery(finalStatement)
					.setParameter("projectId", projectId)
					.setParameter("valueFilter","%" + lazyLoadingProductBacklogs.getFilters().getValue() + "%")
					.setParameterList("listStatus", userStoryStatus)
					.setFirstResult(lazyLoadingProductBacklogs.getStart())
					.setMaxResults(lazyLoadingProductBacklogs.getLimit());
			if(!exceptUserStories.isEmpty()) {
				query.setParameterList("exceptUserStories", exceptUserStories);
			}
			
			List<UserStory> userStories = query.list();
			return userStories;
		} 
		catch (Exception ex) 
		{
			LOGGER.error("findLazyUserStoryByProjectNotInSprint error: " + ex);
		}
		
		return new ArrayList<UserStory>();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<UserStory> findLazyUserStoryToDoAndProgress(
			Long currentProjectId, StateLazyLoading lazyLoadingProductBacklogs) {
		String hql = "SELECT u FROM UserStory u"
				+ " LEFT JOIN FETCH u.kanbanIssues issueKanban"
				+ " WHERE u.project.projectId =:idProject"
				+ " AND (u.status =:todo or u.status =:progress)"
				+ " ORDER BY u.value DESC,u.name ASC";
		List<UserStory> userStories = sessionFactory.getCurrentSession()
				.createQuery(hql)
				.setParameter("idProject",currentProjectId)
				.setParameter("todo",UserStory.StatusType.TODO)
				.setParameter("progress",UserStory.StatusType.IN_PROGRESS)
				.setFirstResult(lazyLoadingProductBacklogs.getStart())
				.setMaxResults(lazyLoadingProductBacklogs.getLimit())
				.list();
		return userStories;
	}

	@SuppressWarnings("unchecked")
	@Override
	public int countTotalRowloadLazyUserstoryProjectId(long projectId,
			StateLazyLoading stateUserstoryList) {
		String hql = "SELECT count(*) as total FROM UserStory userStory"
				+ " WHERE userStory.project.projectId = :projectId ";
		
		//create fielter statement
		String statementFilter = "";
		for(String field : stateUserstoryList.getFilters().getField()){
			statementFilter = statementFilter 
					+ " lower( cast(userStory.{field} as text )) like lower(cast(:valueFilter as text)) or ".replace("{field}", field);
		}
		//remove or
		statementFilter = statementFilter.substring(0,statementFilter.length() - 3);
		
		//crate order statement
		String statementOrder = "";
		if(stateUserstoryList.getSorters().getField() != null){
			statementOrder = " ORDER BY userStory.{field} {value} ";
			statementOrder = statementOrder.replace("{field}", stateUserstoryList.getSorters().getField())
					.replace("{value}", stateUserstoryList.getSorters().getValue().toString());
		}
		//create status in list
		List<UserStory.StatusType> userStoryStatus = new ArrayList<UserStory.StatusType>();
		List<Object> objectStatus = (List<Object>) stateUserstoryList.getFilterInList().getValues();
		for(Object objectItem : objectStatus){
			userStoryStatus.add(UserStory.StatusType.valueOf(objectItem.toString()));
		}
		String statementStatusInList = "";
		String finalHql =" "+ hql + " AND ("  + statementFilter + ") ";
		if(userStoryStatus.size() > 0){
			statementStatusInList = " userStory.{field} IN (:listStatus)".replace("{field}", stateUserstoryList.getFilterInList().getField());
			finalHql += " AND " + statementStatusInList +" ";
		}
		Query query = sessionFactory.getCurrentSession()
				.createQuery(finalHql)
				.setParameter("projectId", projectId)
				.setParameter("valueFilter", "%" + stateUserstoryList.getFilters().getValue().toString().trim() + "%");
		if(userStoryStatus.size() > 0){
			query.setParameterList("listStatus", userStoryStatus);
		}
		
		Long totalRow = (Long)query.uniqueResult();
		return totalRow.intValue();
	}

	@Override
	public int countTotalLazyUserStoryByProjectNotInSprint(long projectId,
			long sprintId, StateLazyLoading lazyLoadingProductBacklogs) {
		String hqlExceptUserStories = "SELECT userStory FROM Issue issue"
				+ " INNER JOIN issue.userStory userStory"
				+ " INNER JOIN issue.sprint sprint"
				+ " WHERE sprint.sprintId = :sprintId";
		
		List<UserStory> exceptUserStories = sessionFactory.getCurrentSession()
				.createQuery(hqlExceptUserStories)
				.setParameter("sprintId", sprintId)
				.list();
		

		
		
		String hqlInit = "SELECT count(*) FROM UserStory userStory"
				+ " INNER JOIN userStory.project project"
				+ " WHERE project.projectId = :projectId";
		
		//create fielter statement
		String statementFilter = "";
		for(String field : lazyLoadingProductBacklogs.getFilters().getField()){
			statementFilter = statementFilter 
					+ " lower( cast(userStory.{field} as text )) like lower(cast(:valueFilter as text)) or ".replace("{field}", field);
	
			
		}
		//remove or
		statementFilter = statementFilter.substring(0,statementFilter.length() - 4);
		
		//create in list in statement
		String statementStatusInList = "";
		if(lazyLoadingProductBacklogs.getFilterInList() != null) {
			statementStatusInList = " userStory.{field} IN (:listStatus)".replace("{field}", lazyLoadingProductBacklogs.getFilterInList().getField());
		}
		List<UserStory.StatusType> userStoryStatus = new ArrayList<UserStory.StatusType>();
		List<Object> objectStatus = (List<Object>) lazyLoadingProductBacklogs.getFilterInList().getValues();
		for(Object objectItem : objectStatus){
			userStoryStatus.add(UserStory.StatusType.valueOf(objectItem.toString()));
		}
		//create statement except userstories
		String statementExceptUserStories = " userStory NOT IN (:exceptUserStories)";
		
		String finalStatement = hqlInit + " AND (" + statementFilter + ") " + " AND " + statementStatusInList;
		if(!exceptUserStories.isEmpty()) {
			finalStatement = hqlInit + " AND (" + statementFilter + ") " + " AND " + statementStatusInList + " AND " + statementExceptUserStories;
		}
		Query query = sessionFactory.getCurrentSession()
				.createQuery(finalStatement)
				.setParameter("projectId", projectId)
				.setParameter("valueFilter","%" + lazyLoadingProductBacklogs.getFilters().getValue() + "%")
				.setParameterList("listStatus", userStoryStatus);
				
		if(!exceptUserStories.isEmpty()) {
			query.setParameterList("exceptUserStories", exceptUserStories);
		}
		Long totalRow = (Long) query.uniqueResult();
		return totalRow.intValue();
	}

	@Override
	public void updateAllIssueOfUserStoryHaveTheSameContent(UserStory userStoryOfIssue) {
		String hql = "UPDATE Issue issue"
				+ " SET issue.subject = :newSubject,"
				+ " issue.description = :newDescription,"
				+ " issue.note = :newNote,"
				+ " issue.priority = :newPriority"
				+ " WHERE issue.userStory.userStoryId = :idUserStory";
		sessionFactory.getCurrentSession().createQuery(hql)
			.setParameter("newSubject", userStoryOfIssue.getName())
			.setParameter("newDescription", userStoryOfIssue.getDescription())
			.setParameter("newNote", userStoryOfIssue.getNote())
			.setParameter("newPriority", userStoryOfIssue.getPriority().toString())
			.setParameter("idUserStory", userStoryOfIssue.getUserStoryId())
			.executeUpdate();
		
	}

	@Override
	public int countTotalUserStoryToDoAndProgress(Long currentProjectId) {
		String hql = "SELECT count(u) FROM UserStory u"
				+ " WHERE u.project.projectId =:idProject"
				+ " AND (u.status =:todo or u.status =:progress)";
		Long totalUserStory = (Long) sessionFactory.getCurrentSession()
				.createQuery(hql)
				.setParameter("idProject",currentProjectId)
				.setParameter("todo",UserStory.StatusType.TODO)
				.setParameter("progress",UserStory.StatusType.IN_PROGRESS)
				.uniqueResult();
		return totalUserStory.intValue();
	}

	@Override
	public void updateSubjectDescriptionNotePriorityOfAllIssueOfUserStory(
			UserStory userStoryOfIssue) {
				String hql = "UPDATE Issue issue"
						+ " SET issue.subject = :newSubject,"
						+ " issue.description = :newDescription,"
						+ " issue.note = :newNote,"
						+ " issue.priority = :newPriority"
						+ " WHERE issue.userStory.userStoryId = :idUserStory";
				sessionFactory.getCurrentSession().createQuery(hql)
					.setParameter("newSubject", userStoryOfIssue.getName())
					.setParameter("newDescription", userStoryOfIssue.getDescription())
					.setParameter("newNote", userStoryOfIssue.getNote())
					.setParameter("newPriority", userStoryOfIssue.getPriority().toString())
					.setParameter("idUserStory", userStoryOfIssue.getUserStoryId())
					.executeUpdate();
		
	}

	@Override
	public List<UserStory> loadAllLazyUserstoryProjectId(long projectId,
			StateLazyLoading stateUserstoryList) {
		String hql = "SELECT userStory FROM UserStory userStory"
				+ " WHERE userStory.project.projectId = :projectId";
		
		//create fielter statement
		String statementFilter = "";
		for(String field : stateUserstoryList.getFilters().getField()){
			statementFilter = statementFilter 
					+ " lower( cast(userStory.{field} as text )) like lower(cast(:valueFilter as text)) or ".replace("{field}", field);
		}
		//remove or
		statementFilter = statementFilter.substring(0,statementFilter.length() - 3);
		
		//crate order statement
		String statementOrder = "";
		if(stateUserstoryList.getSorters().getField() != null){
			statementOrder = " ORDER BY userStory.{field} {value} ";
			statementOrder = statementOrder.replace("{field}", stateUserstoryList.getSorters().getField())
					.replace("{value}", stateUserstoryList.getSorters().getValue().toString());
		}
		//create status in list
		String statementStatusInList = "";
		if(stateUserstoryList.getFilterInList() != null){
			statementStatusInList = " userStory.{field} IN (:listStatus) ".replace("{field}", stateUserstoryList.getFilterInList().getField());
		}
		List<UserStory.StatusType> userStoryStatus = new ArrayList<UserStory.StatusType>();
		List<Object> objectStatus = (List<Object>) stateUserstoryList.getFilterInList().getValues();
		for(Object objectItem : objectStatus){
			userStoryStatus.add(UserStory.StatusType.valueOf(objectItem.toString()));
		}
		if(userStoryStatus.size() <= 0){
			return new ArrayList<UserStory>();
		}
		String finalHql = " " + hql + " AND ("  + statementFilter + ") " + " AND " + statementStatusInList + statementOrder +" ";
		List<UserStory> lazyUserstories = sessionFactory.getCurrentSession()
				.createQuery(finalHql)
				.setParameter("projectId", projectId)
				.setParameter("valueFilter", "%" + stateUserstoryList.getFilters().getValue().toString().trim() + "%")
				.setParameterList("listStatus", userStoryStatus)
				.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
				.list();
		return lazyUserstories;
	}

}
