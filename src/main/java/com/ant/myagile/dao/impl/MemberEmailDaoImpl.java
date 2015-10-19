package com.ant.myagile.dao.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.ant.myagile.dao.MemberEmailDao;
import com.ant.myagile.model.Member;
import com.ant.myagile.model.MemberEmail;

@Repository
@Transactional
public class MemberEmailDaoImpl implements MemberEmailDao, Serializable{

	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = Logger.getLogger(MemberEmailDaoImpl.class);
	
	@Autowired
	private SessionFactory sessionFactory;

	@SuppressWarnings("unchecked")
	@Override
	public List<MemberEmail> findMemberEmailByMember(Member member) throws HibernateException {
		List<MemberEmail> emailList = new ArrayList<MemberEmail>();
		try{
			Criteria cri = sessionFactory.getCurrentSession().createCriteria(MemberEmail.class);
			cri.add(Restrictions.eq("member", member));
			emailList.addAll(cri.list());
		}catch(Exception e){
			LOGGER.error(e.getMessage());
		}
		return emailList;
	}

	@Override
	public boolean create(MemberEmail memberEmail) {
		try{
			sessionFactory.getCurrentSession().save(memberEmail);
			return true;
		}catch(Exception e){
			LOGGER.error(e.getMessage());
		}
		return false;
	}

	@Override
	public boolean update(MemberEmail memberEmail) {
		try{
			sessionFactory.getCurrentSession().update(memberEmail);
			return true;
		}catch(Exception e){
			LOGGER.error(e.getMessage());
		}
		return false;
	}

	@Override
	public boolean delete(MemberEmail memberEmail) {
		try{
			sessionFactory.getCurrentSession().delete(memberEmail);
			return true;
		}catch(Exception e){
			LOGGER.error(e.getMessage());
		}
		return false;
	}

	@Override
	public MemberEmail findMemberEmailById(Long emailId) {
		MemberEmail result = null;
		try{
			result = (MemberEmail) sessionFactory.getCurrentSession().get(MemberEmail.class, emailId);
		}catch(Exception e){
			LOGGER.error(e.getMessage());
		}
		return result;
	}

	@Override
	public MemberEmail findMemberEmailByToken(String token) {
		MemberEmail result = null;
		try{
			Criteria criteria = sessionFactory.getCurrentSession().createCriteria(MemberEmail.class);
			criteria.add(Restrictions.eq("token", token));
			criteria.setCacheable(true);
			result = (MemberEmail) criteria.uniqueResult();
		}catch(Exception e){
			LOGGER.error(e.getMessage());
		}
		return result;
	}

	@Override
	public MemberEmail findMemberEmailByEmail(String email) {
		MemberEmail result = null;
		try{
			Criteria criteria = sessionFactory.getCurrentSession().createCriteria(MemberEmail.class);
			criteria.add(Restrictions.eq("email", email));
			criteria.setCacheable(true);
			result = (MemberEmail) criteria.uniqueResult();
		}catch(Exception e){
			LOGGER.error(e.getMessage());
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<MemberEmail> findActivatedMemberEmailByMember(Member member) {
		List<MemberEmail> emailList = new ArrayList<MemberEmail>();
		try{
			Criteria cri = sessionFactory.getCurrentSession().createCriteria(MemberEmail.class);
			cri.add(Restrictions.eq("member", member));
			cri.add(Restrictions.eq("isActive", true));
			emailList.addAll(cri.list());
		}catch(Exception e){
			LOGGER.error(e.getMessage());
		}
		return emailList;
	}

}
