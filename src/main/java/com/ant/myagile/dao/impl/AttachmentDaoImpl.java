package com.ant.myagile.dao.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.ant.myagile.dao.AttachmentDao;
import com.ant.myagile.model.Attachment;
import com.ant.myagile.model.Issue;
import com.ant.myagile.model.KanbanIssue;
import com.ant.myagile.model.Topic;
import com.ant.myagile.model.UserStory;
import com.ant.myagile.model.Wiki;

@Repository("attachmentDao")
@Transactional
public class AttachmentDaoImpl implements AttachmentDao 
{
    	private static final Logger logger = Logger.getLogger(AttachmentDaoImpl.class);
    	
	@Autowired
	private SessionFactory sessionFactory;

	@Override
	public boolean save(Attachment attachment) 
	{
		try {
			Session session = sessionFactory.getCurrentSession();
			session.save(attachment);
			return true;
		} catch (HibernateException ex) {
		    logger.error("save attachment error: " + ex.getMessage());
		    return false;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Attachment> findAll() 
	{
		try 
		{
			List<Attachment> attachmentList = new ArrayList<Attachment>();
			attachmentList = sessionFactory.getCurrentSession()
					.createCriteria(Attachment.class).list();
			if (attachmentList.size() != 0) {
				return attachmentList;
			}
			return null;
		} catch (HibernateException ex) {
		    	logger.error("find all attachment error: " + ex.getMessage());
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Attachment> findAttachmentNotAddByIssue(Issue issue) 
	{
		try 
		{
			Criteria criteria = sessionFactory.getCurrentSession() .createCriteria(Attachment.class);
			criteria.add(Restrictions.eq("containerId", issue.getIssueId()));
			criteria.add(Restrictions.eq("containerType", Attachment.ISSUE_ATTACHMENT));
			criteria.add(Restrictions.eq("temp", true));
			List<Attachment> attachmentNotAddList = new ArrayList<Attachment>();
			attachmentNotAddList = criteria.list();
			return attachmentNotAddList;
		} catch (Exception ex) {
		    	logger.error("findAttachmentNotAddByIssue error: " + ex.getMessage());
			return null;
		}
	}

	@Override
	public boolean update(Attachment attachment) 
	{
		try 
		{
			Session session = sessionFactory.getCurrentSession();
			session.update(attachment);
			return true;
		} catch (HibernateException ex) {
		    logger.error("update attachment error: " + ex.getMessage());
		    return false;
		}
	}

	@Override
	public boolean delete(Attachment attachment) 
	{
		try 
		{
			Session session = sessionFactory.getCurrentSession();
			session.delete(attachment);
			return true;
		} catch (HibernateException ex) {
		    logger.error("delete attachment error: " + ex.getMessage());
		    return false;
		}
	}

	@Override
	public List<Attachment> findAttachmentNotAdd() 
	{
		try 
		{
			List<Attachment> attachmentList = findAll();
			List<Attachment> attachmentNotAddList = new ArrayList<Attachment>();
			if (attachmentList != null) {
				for (int i = 0; i < attachmentList.size(); i++) {
					if (attachmentList.get(i).getContainerId() == null) {
						attachmentNotAddList.add(attachmentList.get(i));
					}
				}
				return attachmentNotAddList;
			}
			return attachmentNotAddList;
		} catch (Exception ex) {
		    logger.error("findAttachmentNotAdd error: " + ex.getMessage());
		    return null;

		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Attachment> findAttachmentByIssue(Issue issue) 
	{
		try 
		{
			Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Attachment.class);
			criteria.add(Restrictions.eq("containerId", issue.getIssueId()));
			criteria.add(Restrictions.eq("containerType", Attachment.ISSUE_ATTACHMENT));
			List<Attachment> attachmentListByIssue = new ArrayList<Attachment>();
			attachmentListByIssue = criteria.list();
			return attachmentListByIssue;
		} catch (HibernateException ex) {
		    logger.error("findAttachmentByIssue error: " + ex.getMessage());
		    return null;
		}
	}

	@Override
	public Attachment findAttachmentById(long attachmentId) 
	{
		try 
		{
			return (Attachment) sessionFactory.getCurrentSession().get( Attachment.class, attachmentId);
		} catch (Exception ex) {
		    	logger.error("findAttachmentById error: " + ex.getMessage());
			return new Attachment();
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Attachment> findAttachmentByUserStory(UserStory userStory) 
	{
		try 
		{
			Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Attachment.class);
			criteria.add(Restrictions.eq("containerId", userStory.getUserStoryId()));
			criteria.add(Restrictions.eq("containerType", Attachment.USERSTORY_ATTACHMENT));
			List<Attachment> attachmentListByUserStory = new ArrayList<Attachment>();
			if (criteria.list().size() > 0) {
				attachmentListByUserStory = criteria.list();
				return attachmentListByUserStory;
			}
		} catch (Exception e) {
		    logger.error("findAttachmentByUserStory error: " + e.getMessage());
		}
		
		return new ArrayList<Attachment>();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Attachment> findAttachmentNotAddedByContainerType( String containerType) 
	{
	    List<Attachment> attachmentNotAddList = new ArrayList<Attachment>();
	    try
	    {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria( Attachment.class);
		criteria.add(Restrictions.eq("containerType", containerType));
		criteria.add(Restrictions.isNull("containerId"));
		attachmentNotAddList = criteria.list();
	    } catch (Exception e) {
		    logger.error("findAttachmentNotAddedByContainerType error: " + e.getMessage());
	    }
		return attachmentNotAddList;
		
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Attachment> findAttachmentByWiki(Wiki wiki) 
	{
		try {
			Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Attachment.class);
			criteria.add(Restrictions.eq("containerId", wiki.getWikiId()));
			criteria.add(Restrictions.eq("containerType", Attachment.WIKI_ATTACHMENT));
			List<Attachment> attachmentListWiki = new ArrayList<Attachment>();
			attachmentListWiki = criteria.list();
			
			if (criteria.list().size() > 0) {
				attachmentListWiki = criteria.list();
				return attachmentListWiki;
			}
		} catch (Exception e) {
		    logger.error("findAttachmentByWiki error: " + e.getMessage());
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Attachment> findAttachmentByTopic(Topic topic) 
	{
		try 
		{
			Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Attachment.class);
			criteria.add(Restrictions.eq("containerId", topic.getTopicId()));
			criteria.add(Restrictions.eq("containerType", Attachment.TOPIC_ATTACHMENT));
			List<Attachment> attachmentListWiki = new ArrayList<Attachment>();
			attachmentListWiki = criteria.list();
			
			if (criteria.list().size() > 0) 
			{
				attachmentListWiki = criteria.list();
				return attachmentListWiki;
			}
		} catch (Exception e) {
		    logger.error("findAttachmentByTopic error: " + e.getMessage());
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Attachment> findAttachmentByKanbanIssue(KanbanIssue kanbanIssue) 
	{
	    List<Attachment> attachments = new ArrayList<Attachment>();
	    try
	    {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Attachment.class);
		criteria.add(Restrictions.eq("containerId", kanbanIssue.getIssueId()));
		criteria.add(Restrictions.eq("containerType", Attachment.KANBAN_ISSUE_ATTACHMENT));
		criteria.add(Restrictions.eq("temp", false));
		attachments = criteria.list();
	    } catch (Exception e) {
		    logger.error("findAttachmentByKanbanIssue error: " + e.getMessage());
	    }
	    
	    return attachments;
		
	}

	@Override
	public Attachment findAttachmentByIssueAnDiskFileName(Issue issue, String diskFileName) 
	{
	    Attachment attachment = new Attachment();
	    try
	    {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Attachment.class);
		criteria.add(Restrictions.eq("containerId", issue.getIssueId()))
		.add(Restrictions.eq("containerType", Attachment.ISSUE_ATTACHMENT))
		.add(Restrictions.eq("diskFilename", diskFileName));
		
		if(criteria.list().size() > 0){
			attachment = (Attachment) criteria.uniqueResult();
		}
	    } catch (Exception e) {
		    logger.error("findAttachmentByIssueAnDiskFileName error: " + e.getMessage());
	    }
		return attachment;
	}
}
