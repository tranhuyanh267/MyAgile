package com.ant.myagile.dao.impl;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.ant.myagile.dao.MessageDao;
import com.ant.myagile.model.Member;
import com.ant.myagile.model.Message;

@Repository("messageDao")
public class MessageDaoImpl implements MessageDao {

    @Autowired
    private SessionFactory sessionFactory;

    @SuppressWarnings("unchecked")
    @Override
    public List<Message> findAll() {
	Session session = sessionFactory.getCurrentSession();
	Criteria criteria = session.createCriteria(Message.class);
	criteria.addOrder(Order.asc("messageId"));
	criteria.setCacheable(true);
	criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
	List<Message> list = criteria.list();
	return list;
    }

    @Override
    public boolean saveMessage(Message message) {
	try {
	    this.sessionFactory.getCurrentSession().save(message);
	    return true;
	} catch (HibernateException e) {
	    return false;
	}
    }

    @Override
    public boolean updateMessage(Message message) {
	try {
	    this.sessionFactory.getCurrentSession().update(message);
	    return true;
	} catch (HibernateException e) {
	    return false;
	}
    }

    @Override
    public Message findMessageById(long messageId) {
	Session session = sessionFactory.getCurrentSession();
	Criteria criteria = session.createCriteria(Message.class);
	criteria.addOrder(Order.asc("messageId"));
	criteria.setCacheable(true);
	criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
	criteria.add(Restrictions.eq("messageId", messageId));
	Message msg = (Message) criteria.uniqueResult();
	return msg;
    }

    @Override
    public boolean deleteMessage(Message message) {
	try {
	    sessionFactory.getCurrentSession().delete(message);
	    return true;
	} catch (HibernateException e) {
	    return false;
	}
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Message> findMessageByMember(long memberId) {
	Session session = sessionFactory.getCurrentSession();
	String hql = "select ms " + "from Message ms inner join ms.sender s " + "inner join ms.recipient r where " + "s.memberId = :memberId or " + "r.memberId = :memberId";
	Query query = session.createQuery(hql);
	query.setLong("memberId", memberId);
	List<Message> list = query.list();
	return list;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Message> findMessageBySenderAndRecipient(long senderId, long recipientId) {

	Session session = sessionFactory.getCurrentSession();
	String hql = "select ms " + "from Message ms " + "where ms.sender = :senderId and " + "ms.recipient = :recipientId";
	Query query = session.createQuery(hql);
	query.setLong("senderId", senderId);
	query.setLong("recipientId", recipientId);
	List<Message> list = query.list();
	return list;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Message> findMessageBySendDateNumberAndSenderIdAndRecipientId(String dateNumber, long senderId, long recipientId) {
	Session session = sessionFactory.getCurrentSession();
	Criteria criteria = session.createCriteria(Message.class);

	if (!dateNumber.equals("all")) {
	    Calendar calendar = Calendar.getInstance();
	    calendar.set(Calendar.HOUR_OF_DAY, 23);
	    calendar.set(Calendar.MINUTE, 59);
	    calendar.set(Calendar.SECOND, 59);
	    Date toDate = calendar.getTime();
	    calendar.add(Calendar.DAY_OF_MONTH, -Integer.valueOf(dateNumber));
	    Date fromDate = calendar.getTime();
	    criteria.add(Restrictions.between("sendDate", fromDate, toDate));
	}

	Criterion rest1 = Restrictions.and(Restrictions.eq("sender.memberId", senderId), Restrictions.eq("recipient.memberId", recipientId));
	Criterion rest2 = Restrictions.and(Restrictions.eq("sender.memberId", recipientId), Restrictions.eq("recipient.memberId", senderId));
	criteria.add(Restrictions.or(rest1, rest2));
	criteria.addOrder(Order.asc("messageId"));
	List<Message> list = criteria.list();
	return list;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Member> findMembersByTheirUnreadConversation(long memberId) {
	Session session = sessionFactory.getCurrentSession();
	String hql = "SELECT DISTINCT msg.sender " + "FROM Message msg " + "WHERE msg.recipient.memberId = :recipientId AND msg.isRead = :isRead ";
	Query query = session.createQuery(hql);
	query.setLong("recipientId", memberId);
	query.setBoolean("isRead", false);
	List<Member> senderList = query.list();
	return senderList;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Message> findMessageBySenderIdAndRecipientIdAndLimitNumber(long senderId, long recipientId, int limitNumber) {
	int messageSize = 20;
	int allMessage = findAll().size();
	int result = 0;
	if (allMessage > messageSize) {
	    result = allMessage - messageSize;
	}

	Session session = sessionFactory.getCurrentSession();
	Criteria criteria = session.createCriteria(Message.class);
	criteria.setFirstResult(result);
	criteria.setMaxResults(messageSize);
	Criterion rest1 = Restrictions.and(Restrictions.eq("sender.memberId", senderId), Restrictions.eq("recipient.memberId", recipientId));
	Criterion rest2 = Restrictions.and(Restrictions.eq("sender.memberId", recipientId), Restrictions.eq("recipient.memberId", senderId));
	criteria.add(Restrictions.or(rest1, rest2));
	criteria.addOrder(Order.asc("messageId"));
	List<Message> list = criteria.list();
	return list;
    }

}
