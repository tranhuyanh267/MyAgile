package com.ant.myagile.dao.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.ant.myagile.dao.TopicDao;
import com.ant.myagile.managedbean.SharingTopicBean;
import com.ant.myagile.model.Topic;
import com.ant.myagile.model.Wiki;

@Repository
@Transactional
public class TopicDaoImpl implements TopicDao {
	private static final Logger LOGGER = Logger
			.getLogger(TopicDaoImpl.class);
	@Autowired
	SessionFactory sessionFactory;

	@Override
	public Topic getTopicByName(String name,long wikiId) {
		return (Topic) sessionFactory.getCurrentSession()
				.createCriteria(Topic.class)
				.add(Restrictions.eq("title", name))
				.add(Restrictions.eq("wiki.wikiId", wikiId)).uniqueResult();
	}
	
	@Override
	public int add(Topic topic) {
		try{
			sessionFactory.getCurrentSession().save(topic);
			return 1;
		}catch(Exception e){
			LOGGER.debug(e.getMessage());
			return -1;
		}
	}

	@Override
	public Boolean delete(Topic topic) {
		try{
			sessionFactory.getCurrentSession().delete(topic);
			if(this.getTopicById(topic.getTopicId()) == null){
				return true;
			}
		}catch(Exception e){
			LOGGER.debug(e.getMessage());
		}
		return false;
	}

	@Override
	public int update(Topic topic) {
		try{
			sessionFactory.getCurrentSession().update(topic);
			return 1;
		}catch(Exception e){
			LOGGER.debug(e.getMessage());
		}
		return -1;
	}
	
	@Override
	public Topic getTopicById(long id) {
		Session session = sessionFactory.getCurrentSession();
		try{
			Topic topic = (Topic)session.get(Topic.class, id);
			return topic;
		} catch(Exception e){
			LOGGER.debug(e.getMessage());
		}
		return null;
	}

	@Override
	public List<Topic> getListTopicByWiki(Wiki wiki) {
		List<Topic> topics = new ArrayList<Topic>();
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Topic.class);
		criteria.add(Restrictions.eq("wiki", wiki));
		
		topics = criteria.list();
		
		return topics;
		
	}
}
