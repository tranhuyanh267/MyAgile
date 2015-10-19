package com.ant.myagile.dao.impl;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.ant.myagile.dao.MemberIssueDao;
import com.ant.myagile.model.Member;
import com.ant.myagile.model.MemberIssue;

@Repository
public class MemberIssueDaoImpl implements MemberIssueDao {

	@Autowired
	private SessionFactory sessionFactory;

	@Override
	public boolean save(MemberIssue memberIssue) {
		try {
			sessionFactory.getCurrentSession().save(memberIssue);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public boolean delete(MemberIssue memberIssue) {
		try {
			sessionFactory.getCurrentSession().delete(memberIssue);
			return true;
		} catch (HibernateException e) {
			return false;
		}
	}

	@Override
	public boolean update(MemberIssue memberIssue) {
		try {
			sessionFactory.getCurrentSession().update(memberIssue);
			return true;
		} catch (HibernateException e) {
			return false;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean checkExist(long memberId, long issueId) {
		try {
			Criteria criteria = sessionFactory.getCurrentSession()
					.createCriteria(MemberIssue.class, "memberIssue");
			criteria.createAlias("memberIssue.member", "member");
			criteria.createAlias("memberIssue.kanbanIssue", "issue");
			criteria.add(Restrictions.eq("member.memberId", memberId));
			criteria.add(Restrictions.eq("issue.issueId", issueId));
			criteria.setCacheable(true);
			List<MemberIssue> memberIssueList = new ArrayList<MemberIssue>();
			memberIssueList = criteria.list();
			if (memberIssueList.size() > 0) {
				return true;
			} else {
				return false;
			}
		} catch (Exception exception) {
			return false;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean checkExistIssue(long issueId) {
		try {
			Criteria criteria = sessionFactory.getCurrentSession()
					.createCriteria(MemberIssue.class, "memberIssue");
			criteria.createAlias("memberIssue.kanbanIssue", "issue");
			criteria.add(Restrictions.eq("issue.issueId", issueId));
			criteria.setCacheable(true);
			List<MemberIssue> memberIssueList = new ArrayList<MemberIssue>();
			memberIssueList = criteria.list();
			if (memberIssueList.size() > 0) {
				return true;
			} else {
				return false;
			}
		} catch (Exception exception) {
			return false;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Member> getMembersByIssue(long issueId) {
		try {
			Session session = sessionFactory.getCurrentSession();
			String hql = "select distinct m "
					+ "from MemberIssue mi inner join mi.member m "
					+ "where mi.kanbanIssue.issueId = :issueId ";
					//+ "order by mi.memberissueId desc";
			Query query = session.createQuery(hql);
			query.setLong("issueId", issueId);
			List<Member> memberList = query.list();
			return memberList;
		} catch (Exception exception) {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<MemberIssue> getMemberIssueByMemberIdAndIssueId(long memberId,
			long issueId) {
		try {
			String hql = "SELECT DISTINCT mi FROM MemberIssue mi"
					+ " WHERE mi.member.memberId = :memberId"
					+ " AND mi.kanbanIssue.issueId = :kanbanIssueId";
			return sessionFactory.getCurrentSession()
					.createQuery(hql)
					.setParameter("memberId", memberId)
					.setParameter("kanbanIssueId", issueId)
					.list();
		} catch (Exception exception) {
			return null;
		}

	}

	@Override
	public boolean deleteAllByIssue(long idIssue) {
		String hql = "Delete from MemberIssue mi where mi.kanbanIssue.issueId=:idIssue";
		int effect = sessionFactory.getCurrentSession()
				.createQuery(hql)
				.setParameter("idIssue",idIssue)
				.executeUpdate();
		if(effect == -1){
			return false;
		}
		return true;
	}
}
