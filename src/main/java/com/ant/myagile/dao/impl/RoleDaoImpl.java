package com.ant.myagile.dao.impl;

import java.util.List;
import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import com.ant.myagile.dao.RoleDao;
import com.ant.myagile.model.Role;

@Repository("roleDao")
@Transactional
public class RoleDaoImpl implements RoleDao{
	
	@Autowired
	SessionFactory sessionFactory;
	
	@SuppressWarnings("unchecked")
	@Override
	public Role getRoleByObject(String strProperty, Object obj) {
		try {
			List<Role> lRoles = sessionFactory.getCurrentSession().createCriteria(Role.class)
					.add(Restrictions.eq(strProperty,obj)).list();
			return lRoles.get(0);
		} catch (Exception e) {
			return new Role();
		}
		
		
	}

	@Override
	public Long save(Role role) {
		try {
		    Long result =  (Long) sessionFactory.getCurrentSession().save(role);
		    return result;
		} catch (HibernateException e) {
		    return 0L;
		}
	}

	

}
