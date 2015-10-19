package com.ant.myagile.dao.impl;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.ant.myagile.dao.FacebookNewsDao;
import com.ant.myagile.model.FacebookNews;

@Repository("FacebookNewsDao")
@Transactional
public class FacebookNewsDaoImpl implements FacebookNewsDao {
	
	private static final Logger LOGGER = Logger.getLogger(FacebookNewsDaoImpl.class);
	@Autowired
	private SessionFactory sessionFactory;
	
	@Override
	public boolean saveFacebookNews(FacebookNews news) {
		try {
			this.sessionFactory.getCurrentSession().save(news);
			return true;
		} catch (HibernateException e) {
			LOGGER.error("Save facebooknews not successful");
			e.printStackTrace();
			return false;
		}

	}

	@Override
	public boolean deleteFacebookNews(int idFacebookNews) {
		try {
			this.sessionFactory.getCurrentSession().delete(
					this.getFacebookNewsById(idFacebookNews));
			return true;
		} catch (HibernateException e) {
			LOGGER.error("Delete facebooknews not successful");
			return false;
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public List<FacebookNews> getTenLatestFacebookNews() {
		return sessionFactory.getCurrentSession()
				.createCriteria(FacebookNews.class)
				.addOrder(Order.desc("id")).setMaxResults(10).list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean checkExistFacebookNewsFromPhotoId(String photoId) {
		List<FacebookNews> facebookNews = sessionFactory.getCurrentSession()
				.createCriteria(FacebookNews.class)
				.add(Restrictions.eq("photoId", photoId)).list();
		if (!facebookNews.isEmpty()) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public FacebookNews getFacebookNewsById(int idFacebookNews) {
		return (FacebookNews) sessionFactory.getCurrentSession().get(
				FacebookNews.class, idFacebookNews);
	}

}
