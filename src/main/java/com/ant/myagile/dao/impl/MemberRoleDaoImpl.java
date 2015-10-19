package com.ant.myagile.dao.impl;

import java.util.ArrayList;
import java.util.List;

import javax.security.auth.Subject;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Property;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.ant.myagile.dao.MemberRoleDao;
import com.ant.myagile.model.MemberRole;
import com.ant.myagile.model.Role;

@Repository("MemberRoleDao")
public class MemberRoleDaoImpl implements MemberRoleDao {
	@Autowired
	private SessionFactory sessionFactory;

	@Override
	public void addRoleForMember(MemberRole memberRole) {
		try {
			sessionFactory.getCurrentSession().save(memberRole);
		} catch (HibernateException e) {
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<MemberRole> getRoleByIdMember(Long idMember) {
		try {
			List<MemberRole> memberRole = sessionFactory.getCurrentSession()
					.createCriteria(MemberRole.class)
					.add(Restrictions.eq("memberRoleId.memberId", idMember))
					.addOrder(Order.desc("memberRoleId.roleId")).list();
			return memberRole;
		} catch (Exception e) {
			return new ArrayList<MemberRole>();
			// return null;
		}
	}

	@Override
	public void updateRoleForMember(MemberRole memberRole) {
		Session session = sessionFactory.getCurrentSession();
		session.createQuery(
				"update MemberRole mr set mr.memberRoleId.roleId=:role where mr.memberRoleId.memberId=:member")
				.setLong("role", memberRole.getRoleId())
				.setLong("member", memberRole.getMemberId()).executeUpdate();
	}

	public List<MemberRole> getAdminRole() {
		try {
			DetachedCriteria role = DetachedCriteria.forClass(Role.class);
			role.setProjection(Property.forName("roleId"));
			role.add(Restrictions.eq("authority", "ROLE_ADMIN"));
			Criteria criteria = sessionFactory.getCurrentSession().createCriteria(MemberRole.class);
			criteria.add(Property.forName("memberRoleId.roleId").eq(role));
			@SuppressWarnings("unchecked")
			List<MemberRole> adminRole = criteria.list();
			return adminRole;
		} catch (Exception e) {
			return new ArrayList<MemberRole>();
		}
	}
}
