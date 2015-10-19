package com.ant.myagile.dao.impl;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.ant.myagile.dao.StatusDao;
import com.ant.myagile.model.Issue;
import com.ant.myagile.model.Sprint;
import com.ant.myagile.model.Status;
@Repository("statusDao")
@Transactional
public class StatusDaoImpl implements StatusDao {
	@Autowired
	private SessionFactory sessionFactory;

	@Override
	public boolean save(Status status) {
		try {
			sessionFactory.getCurrentSession().save(status);
			return true;
		} catch (HibernateException e) {
			return false;
		}
	}

	@Override
	public boolean delete(Status status) {
		try {
			sessionFactory.getCurrentSession().delete(status);
			return true;
		} catch (HibernateException e) {
			return false;
		}
	}

	@Override
	public boolean update(Status status) {
		try {
			sessionFactory.getCurrentSession().update(status);
			return true;
		} catch (HibernateException e) {
			return false;
		}
	}

	@Override
	public Status findStatusById(Long statusId) {
		try {
			return (Status) sessionFactory.getCurrentSession().createCriteria(Status.class).add(Restrictions.eq("statusId", statusId)).setCacheable(true).list().get(0);
		} catch (HibernateException e) {
			return null;
		}
	}

	@Override
	public Status findStatusStartBySprintId(long sprintId) {
		Status statusStart = (Status) sessionFactory.getCurrentSession().createCriteria(Status.class).add(Restrictions.eq("type", Status.StatusType.START)).add(Restrictions.eq("sprint.sprintId", sprintId)).setCacheable(true).uniqueResult();
		return statusStart;
	}

	@Override
	public Status findStatusDoneBySprintId(long sprintId) {
		Status statusDone = (Status) sessionFactory.getCurrentSession().createCriteria(Status.class).add(Restrictions.eq("type", Status.StatusType.DONE)).add(Restrictions.eq("sprint.sprintId", sprintId)).setCacheable(true).uniqueResult();
		return statusDone;
	}

	@Override
	public Status findStatusAcceptedBySprintId(long sprintId) {
		Status statusAccepted = (Status) sessionFactory.getCurrentSession().createCriteria(Status.class).add(Restrictions.or(Restrictions.eq("type", Status.StatusType.ACCEPTED_HIDE), Restrictions.eq("type", Status.StatusType.ACCEPTED_SHOW))).add(Restrictions.eq("sprint.sprintId", sprintId)).setCacheable(true).uniqueResult();
		return statusAccepted;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Status> findStatusBySprintId(long sprintId) {
		try {
			Status statusStart = findStatusStartBySprintId(sprintId);
			Status statusDone = findStatusDoneBySprintId(sprintId);
			Status statusAccepted = findStatusAcceptedBySprintId(sprintId);
			List<Status> listStatusInProgress = sessionFactory.getCurrentSession().createCriteria(Status.class).add(Restrictions.eq("sprint.sprintId", sprintId)).add(Restrictions.eq("type", Status.StatusType.IN_PROGRESS)).setCacheable(true).addOrder(Order.asc("statusId")).list();
			List<Status> listCombine = new ArrayList<Status>(0);
			listCombine.add(statusStart);
			listCombine.addAll(listStatusInProgress);
			listCombine.add(statusDone);
			listCombine.add(statusAccepted);
			return listCombine;
		} catch (HibernateException e) {
			return null;
		}
	}

	@SuppressWarnings({ "unchecked" })
	@Override
	public void updateIssueBySprintId(long sprintId, long statusId) {
		try {
			Session session = sessionFactory.getCurrentSession();
			Criteria cri = session.createCriteria(Issue.class);
			cri.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
			List<Issue> list = cri.add(Restrictions.eq("sprint.sprintId", sprintId)).add(Restrictions.eq("status.statusId", statusId)).setCacheable(true).list();
			List<Status> listStatus = this.findStatusBySprintId(sprintId);
			if (list.size() != 0) {
				for (Issue is : list) {
					is.setStatus(listStatus.get(listStatus.indexOf(findStatusById(statusId)) - 1));
					session.update(is);
				}
			}
		} catch (HibernateException e) {
		}
	}

	@Override
	public List<Status> getAllStatusBySprint(Sprint sprint) {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Status.class);
		criteria.add(Restrictions.eq("sprint", sprint));
		
		List<Status> statusList = new ArrayList<Status>();
		statusList = criteria.list();
		return statusList;
	}
}
