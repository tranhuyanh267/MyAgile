package com.ant.myagile.dao.impl;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.ant.myagile.dao.MyTeamLinkedDao;
import com.ant.myagile.model.MyTeamLinked;

@Repository("myTeamLinkedDao")
@Transactional
public class MyTeamLinkedDaoImpl implements MyTeamLinkedDao {
	@Autowired
	private SessionFactory sessionFactory;

	@Override
	public boolean save(MyTeamLinked myteamlinked) {
		sessionFactory.getCurrentSession().save(myteamlinked);
		return false;
	}

	@Override
	public boolean delete(MyTeamLinked myteamlinked) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean update(MyTeamLinked myteamlinked) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String findIdByEmail(String email) {
		try {
			org.hibernate.Criteria criteria = this.sessionFactory.getCurrentSession().createCriteria(MyTeamLinked.class);
			criteria.add(Restrictions.eq("email", email));
			MyTeamLinked temp = (MyTeamLinked) criteria.uniqueResult();
			return temp.getMyTeamId();
		} catch (Exception e) {
			return "";
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<MyTeamLinked> findAll() {
		try {
			List<MyTeamLinked> list = new ArrayList<MyTeamLinked>();
			org.hibernate.Criteria criteria = this.sessionFactory.getCurrentSession().createCriteria(MyTeamLinked.class);
			list = criteria.list();
			return list;
		} catch (Exception e) {
			return null;
		}
	}

}
