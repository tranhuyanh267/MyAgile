package com.ant.myagile.dao.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.ant.myagile.dao.IssueDao;
import com.ant.myagile.dao.ProjectDao;
import com.ant.myagile.dao.SprintDao;
import com.ant.myagile.dao.UserStoryDao;
import com.ant.myagile.dto.IssueStateLazyLoading;
import com.ant.myagile.dto.LazyFilterInList;
import com.ant.myagile.dto.LazySorter.LAZYSORTER_VALUE;
import com.ant.myagile.dto.StateLazyLoading;
import com.ant.myagile.model.Issue;
import com.ant.myagile.model.Sprint;
import com.ant.myagile.model.Status;
import com.ant.myagile.model.UserStory;
import com.itextpdf.text.pdf.PdfStructTreeController.returnType;

@Repository("issueDao")
@Transactional
public class IssueDaoImpl implements IssueDao, Serializable {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(IssueDaoImpl.class);
    @Autowired
    private SessionFactory sessionFactory;
    @Autowired
    private SprintDao sprintDao;
    @Autowired
    private ProjectDao projectDao;
    @Autowired
    private UserStoryDao userStoryDao;

    @Override
    @Transactional
    @SuppressWarnings("unchecked")
    public Issue findIssueById(long issueId) {
	try {
	    Criteria cri = this.sessionFactory.getCurrentSession().createCriteria(Issue.class);
	    cri.add(Restrictions.eq("issueId", issueId));
	    cri.addOrder(Order.desc("issueId"));
	    cri.setCacheable(true);
	    List<Issue> issues = cri.list();
	    if (issues.isEmpty()) {
	    	return null;
	    }
	    return issues.get(0);
	} catch (HibernateException e) {
	    return null;
	}
    }

    @SuppressWarnings("unchecked")
    @Override
    @Transactional
    public List<Issue> findBugBySprintId(long sprintId) {
	try {
	    Session session = this.sessionFactory.getCurrentSession();
	    Criteria cri = session.createCriteria(Issue.class);
	    cri.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
	    cri.add(Restrictions.eq("type", "Bug"));
	    cri.setCacheable(true);
	    cri.add(Restrictions.eq("sprint", this.sprintDao.findSprintById(sprintId)));
	    return cri.list();
	} catch (HibernateException e) {
	    return null;
	}

    }

    @SuppressWarnings("unchecked")
    @Override
    @Transactional
    public List<Issue> findIssueBySprintId(long sprintId) {
	try {
	    Session session = this.sessionFactory.getCurrentSession();
	    Criteria cri = session.createCriteria(Issue.class);
	    cri.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
	    cri.setCacheable(true);
	    cri.add(Restrictions.eq("sprint.sprintId", sprintId));
	    cri.addOrder(Order.desc("issueId"));
	    return cri.list();
	} catch (HibernateException e) {
	    return null;
	}
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Issue> findIssueByStatus(Status status) {
	try {
	    return this.sessionFactory.getCurrentSession().createCriteria(Issue.class).setCacheable(true).add(Restrictions.eq("status", status)).list();
	} catch (HibernateException e) {
	    return null;
	}
    }

    @Override
    public boolean saveIssue(Issue issue) {
	try {
	    Session session = this.sessionFactory.getCurrentSession();
	    session.save(issue);
	    return true;
	} catch (HibernateException e) {
	    return false;
	}
    }

    @Override
    public void deleteIssue(Issue issue) {
	try {
	    this.sessionFactory.getCurrentSession().delete(issue);
	} catch (HibernateException e) {
	}
    }

    @Override
    public boolean updateIssue(Issue issue) {
	try {
	    Session session = this.sessionFactory.getCurrentSession();
	    session.update(issue);
	    return true;
	} catch (HibernateException e) {
	    return false;
	}
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Issue> findIssueByTeamAndProject(Long teamId, Long projectId) {
	try {
	    Criteria cri = this.sessionFactory.getCurrentSession().createCriteria(Issue.class);
	    cri.createAlias("sprint.team", "team");
	    cri.add(Restrictions.eq("team.teamId", teamId));
	    cri.createAlias("userStory.project", "project");
	    cri.setCacheable(true);
	    cri.add(Restrictions.eq("project.projectId", projectId));
	    cri.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
	    return cri.list();
	} catch (HibernateException e) {
	    return null;
	}

    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Issue> findIssueByTeamAndSprint(Long teamId, Long sprintId) {
	try {
	    Session session = this.sessionFactory.getCurrentSession();
	    Criteria issueCriteria = session.createCriteria(Issue.class, "issueAlias").createAlias("issueAlias.sprint", "sprintAlias").createAlias("sprintAlias.team", "teamAlias").add(Restrictions.eq("teamAlias.teamId", teamId)).add(Restrictions.eq("sprintAlias.sprintId", sprintId)).setCacheable(true).setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
	    return issueCriteria.list();
	} catch (HibernateException e) {
	    return null;
	}
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Issue> findIssueByParent(Issue parent) {
	try {
	    Criteria cri = this.sessionFactory.getCurrentSession().createCriteria(Issue.class);
	    cri.add(Restrictions.eq("parent.issueId", parent.getIssueId()));
	    cri.addOrder(Order.asc("issueId"));
	    cri.setCacheable(true);
	    cri.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
	    if (cri.list().size() > 0)
		return cri.list();
	    return new ArrayList<Issue>();
	} catch (Exception e) {
	    return new ArrayList<Issue>();
	}

    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Issue> findIssuesParent(long projectId, long sprintId) {
	try {
	    List<Issue> listIssue = new ArrayList<Issue>();
	    Criteria cri = this.sessionFactory.getCurrentSession().createCriteria(Issue.class, "issue");
	    cri.add(Restrictions.eq("sprint.sprintId", sprintId));
	    cri.createAlias("issue.userStory", "us");
	    cri.setCacheable(true);
	    cri.add(Restrictions.eq("us.project", this.projectDao.findById(projectId)));
	    cri.add(Restrictions.isNull("parent"));
	    cri.addOrder(Order.asc("issueId"));
	    listIssue = cri.list();
	    return listIssue;
	} catch (HibernateException e) {
	    return null;
	}
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Issue> findIssuesNotParent(long sprintId) {
	try {
	    List<Issue> listIssue = new ArrayList<Issue>();
	    Criteria cri = this.sessionFactory.getCurrentSession().createCriteria(Issue.class, "issue");
	    cri.add(Restrictions.eq("sprint.sprintId", sprintId));
	    cri.add(Restrictions.isNotNull("parent"));
	    cri.setCacheable(true);
	    cri.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY).list();
	    listIssue = cri.list();
	    return listIssue;
	} catch (HibernateException e) {
	    return null;
	}
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Issue> findIssuesIsTaskBySprintId(long sprintId) {
	List<Issue> issueList = new ArrayList<Issue>();
	try {
	    List<Long> list = new ArrayList<Long>();
	    list = this.sessionFactory.getCurrentSession().createCriteria(Issue.class).add(Restrictions.eq("sprint.sprintId", sprintId)).setCacheable(true).add(Restrictions.isNotNull("parent")).setProjection(Projections.distinct(Projections.property("parent.issueId"))).list();
	    if (list.size() > 0) {
		issueList = this.sessionFactory.getCurrentSession().createCriteria(Issue.class, "issue").add(Restrictions.eq("sprint.sprintId", sprintId)).add(Restrictions.not(Restrictions.in("issueId", list))).setCacheable(true).addOrder(Order.asc("issueId")).setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY).list();
	    } else {
		issueList = this.sessionFactory.getCurrentSession().createCriteria(Issue.class, "issue").add(Restrictions.eq("sprint.sprintId", sprintId)).add(Restrictions.isNull("parent")).setCacheable(true).addOrder(Order.asc("issueId")).setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY).list();
	    }
	} catch (Exception e) {
	}
	return issueList;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Issue> findIssuesByTypeAndSprint(long sprintId, String type) {
	List<Issue> issueList = new ArrayList<Issue>();
	try {
	    List<Long> list = new ArrayList<Long>();
	    list = this.sessionFactory.getCurrentSession().createCriteria(Issue.class).add(Restrictions.eq("sprint.sprintId", sprintId)).setCacheable(true).add(Restrictions.isNotNull("parent")).setProjection(Projections.distinct(Projections.property("parent.issueId"))).list();
	    if (list.size() > 0) {
		issueList = this.sessionFactory.getCurrentSession().createCriteria(Issue.class, "issue").add(Restrictions.eq("sprint.sprintId", sprintId)).setCacheable(true).add(Restrictions.eq("type", type)).add(Restrictions.not(Restrictions.in("issueId", list))).setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY).list();
	    } else {
		issueList = this.sessionFactory.getCurrentSession().createCriteria(Issue.class, "issue").setCacheable(true).add(Restrictions.eq("sprint.sprintId", sprintId)).add(Restrictions.eq("type", type)).add(Restrictions.isNull("parent")).setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY).list();
	    }
	} catch (Exception e) {
	}
	return issueList;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Issue> findIssuesSingle(long sprintId) {
	try {
	    Session session = this.sessionFactory.getCurrentSession();
	    Criteria issueCriteria = session.createCriteria(Issue.class, "issueAlias").createAlias("issueAlias.sprint", "sprintAlias").add(Restrictions.eq("sprintAlias.sprintId", sprintId)).add(Restrictions.eq("issueAlias.parent", null)).setCacheable(true).addOrder(Order.asc("issueId")).setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
	    return issueCriteria.list();
	} catch (HibernateException e) {
	    return null;
	}
    }

    @Override
    public boolean checkIssuesBelongUserStory(Long userStoryId) {
	try {
	    Criteria cri = this.sessionFactory.getCurrentSession().createCriteria(Issue.class);
	    cri.add(Restrictions.eq("userStory.userStoryId", userStoryId));
	    cri.setCacheable(true);
	    cri.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
	    if (cri.list().size() != 0) {
		return true;
	    }
	} catch (HibernateException e) {
	    LOGGER.error("Error: " + e.getMessage());
	}
	return false;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Issue> findIssuesByUserStory(Long userStoryId) {
	try {
	    Criteria cri = this.sessionFactory.getCurrentSession().createCriteria(Issue.class);
	    cri.add(Restrictions.eq("userStory.userStoryId", userStoryId));
	    cri.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
	    cri.setCacheable(true);
	    if (cri.list().size() > 0) {
		return cri.list();
	    }
	} catch (HibernateException e) {
	}
	return null;
    }

    @SuppressWarnings("unchecked")
	@Override
    public Issue findIssueByUserStoryAndSprint(long userStoryId, long sprintId) {
        try {
            Criteria cri = this.sessionFactory.getCurrentSession().createCriteria(Issue.class);
            cri.add(Restrictions.eq("userStory.userStoryId", userStoryId));
            cri.add(Restrictions.eq("sprint.sprintId", sprintId));
            cri.addOrder(Order.asc("issueId"));
            cri.setCacheable(true);
//            Issue result = (Issue) cri.uniqueResult();
            List<Issue> result = cri.list();
            if(!result.isEmpty()){
            	return result.get(0);
            }
            return null;
        } catch (HibernateException e) {
            LOGGER.error(e.getMessage());
            return null;
        }
    }

    @Override
    public Issue findIssueParentByIssueChild(Issue issue) {
	try {
	    Long issueId = issue.getIssueId();
	    Session session = sessionFactory.getCurrentSession();
	    String hql = "select issue.parent " + "from Issue issue " + "where issue.issueId = :issueId ";
	    Query query = session.createQuery(hql);
	    query.setLong("issueId", issueId);
	    Issue issueTemp = (Issue) query.uniqueResult();
	    return issueTemp;
	} catch (HibernateException e) {
	    return null;
	}
    }

	@Override
	public long saveIssueAndGetId(Issue issue) {
		try {
		    Session session = this.sessionFactory.getCurrentSession();
		    long idIssue =  (Long) session.save(issue);
		    return idIssue;
		} catch (HibernateException e) {
		    return 0L;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Issue> findParentIssuesBySprintAndExceptParentThis(
			long issueId, long sprintId) {
		List<Issue> parentIssues = new ArrayList<Issue>();
		try {
			Issue exceptIssue = this.findIssueById(issueId);
			parentIssues = sessionFactory.getCurrentSession()
					.createCriteria(Issue.class)
					.add(Restrictions.eq("sprint.sprintId", sprintId))
					.add(Restrictions.isNull("parent"))
					.add(Restrictions.not(Restrictions.eq("issueId", exceptIssue.getParent().getIssueId())))
					.list();
		} catch (Exception e) {
			LOGGER.error("-error can not get parent issue", e);
			return parentIssues;
		}
		return parentIssues;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Issue> findChildrenIssuesOfRemainingParent(long parentId) {
		List<Issue> notDoneChildrenIssuesList = new ArrayList<Issue>();
		try {
			notDoneChildrenIssuesList = sessionFactory.getCurrentSession()
					.createQuery("SELECT issue FROM Issue issue"
							+ " WHERE issue.parent.issueId = :parentId"
							+ " AND (issue.status.type = :statusStart OR issue.status.type = :statusInProgress)")
					.setParameter("parentId", parentId)
					.setParameter("statusStart", Status.StatusType.START)
					.setParameter("statusInProgress", Status.StatusType.IN_PROGRESS)
					.list();
		} catch (Exception exception) {
			LOGGER.error("Error at findNotDoneChildrenIssuesByParent", exception);
		}
		return notDoneChildrenIssuesList;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Issue> findRemainingTasksByProjectAndTeam(long projectId,
			long teamId) {
		List<Issue> remainingTasksList = new ArrayList<Issue>();
		try {
			remainingTasksList = sessionFactory.getCurrentSession()
					.createQuery("SELECT issue FROM Issue issue, UserStory userStory, Sprint sprint, Status status"
							+ " WHERE issue.userStory.userStoryId = userStory.userStoryId"
							+ " AND issue.sprint.sprintId = sprint.sprintId"
							+ " AND userStory.project.projectId = :selectedProjectId"
							+ " AND sprint.team.teamId = :selectedTeamId"
							+ " AND issue.status.statusId = status.statusId"
							+ " AND (status.type = :statusStart OR status.type = :statusInProgress)"
							+ " AND userStory.status <> :statusVoid"
							+ " ORDER BY issue.id ASC")
					.setParameter("selectedProjectId", projectId)
					.setParameter("selectedTeamId", teamId)
					.setParameter("statusStart", Status.StatusType.START)
					.setParameter("statusInProgress", Status.StatusType.IN_PROGRESS)
					.setParameter("statusVoid", UserStory.StatusType.VOID)
					.list();
			eliminateDuplicatedIssues(remainingTasksList);
		} catch (Exception exception) {
			LOGGER.error("Error at findRemainingUserStoriesByProject", exception);
		}
		
		return remainingTasksList;
	}
	
	private void eliminateDuplicatedIssues(List<Issue> remainingTasksList) {
		for (int i = 0; i < remainingTasksList.size()-1; i++) {
			for (int j = i + 1; j < remainingTasksList.size(); j++) {
				if (checkOldTaskEqualNewTask(remainingTasksList.get(i),remainingTasksList.get(j))){
					remainingTasksList.remove(i);
					i = i - 1;
					break;
				}
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private List<String> getChildSubjectNotDoneByIssueId(long issueId){
		String hql = "SELECT distinct issue.subject"
				+ "	FROM Issue issue"
				+ " WHERE issue.parent.issueId = :parentIssueId"
				+ " AND (issue.status.type = :startStatus OR issue.status.type = :inprogress)";
		List<String> childsSubject = sessionFactory.getCurrentSession().createQuery(hql)
				.setParameter("parentIssueId", issueId)
				.setParameter("startStatus", Status.StatusType.START)
				.setParameter("inprogress",  Status.StatusType.IN_PROGRESS)
				.list();
		return childsSubject;
	}
	@SuppressWarnings("unchecked")
	private List<String> getChildSubjectByIssueId(long issueId){
		String hql = "SELECT distinct issue.subject"
				+ "	FROM Issue issue"
				+ " WHERE issue.parent.issueId = :parentIssueId";
		List<String> childsSubject = sessionFactory.getCurrentSession().createQuery(hql)
				.setParameter("parentIssueId", issueId)
				.list();
		return childsSubject;
	}
	private boolean checkOldIssueListInNewIssueList(List<String> oldIssueList,List<String> newIssueList){
		int countTotalSubject = 0;
		for(String subjectItem : oldIssueList){
			if(newIssueList.contains(subjectItem)){
				countTotalSubject++;
			}
		}
		if(countTotalSubject == oldIssueList.size()){
			return true;
		}
		return false;
	}

	private boolean checkOldTaskEqualNewTask(Issue oldTask,Issue newTask){
		if(oldTask.getUserStory().getUserStoryId().equals(newTask.getUserStory().getUserStoryId())){
			if(oldTask.getChilds().isEmpty() && newTask.getChilds().isEmpty()){
				return true;
			}else if(oldTask.getChilds().isEmpty() && !newTask.getChilds().isEmpty()){
				return true;
			}else if(!oldTask.getChilds().isEmpty() && newTask.getChilds().isEmpty()){
				return false;
			}else{
				List<String> oldListSubject = getChildSubjectNotDoneByIssueId(oldTask.getIssueId());
				List<String> newListSubject = getChildSubjectByIssueId(newTask.getIssueId());
				if(checkOldIssueListInNewIssueList(oldListSubject,newListSubject)){
					return true;
				}else{
					return false;
				}
			}
		}
		return false;
	}
	@SuppressWarnings("unchecked")
	@Override
	public boolean checkIssueParentDoneBySubject(String subject) {
		String hql = "SELECT issue FROM Issue issue WHERE issue.subject = :subject"
				+ " AND issue.parent IS NULL"
				+ " ORDER BY issue.issueId DESC";
		
		Issue issueBySubject = (Issue) sessionFactory.getCurrentSession()
				.createQuery(hql)
				.setParameter("subject", subject)
				.setMaxResults(1)
				.uniqueResult();
		
		if (issueBySubject == null) {
			return false;
		}
		//check latest issue, is it done or not
		if(!issueBySubject.getStatus().getType().equals(Status.StatusType.START) && !issueBySubject.getStatus().getType().equals(Status.StatusType.IN_PROGRESS)){
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean checkIssueChildDoneBySubject(String subject) {
		String hql = "SELECT issue FROM Issue issue WHERE issue.subject = :subject"
				+ " AND issue.parent IS NOT NULL"
				+ " ORDER BY issue.issueId DESC";
		
		Issue issueBySubject = (Issue) sessionFactory.getCurrentSession()
				.createQuery(hql)
				.setParameter("subject", subject)
				.setMaxResults(1)
				.uniqueResult();
		
		if (issueBySubject == null) {
			return false;
		}
		//check latest issue, is it done or not
		if(!issueBySubject.getStatus().getType().equals(Status.StatusType.START) && !issueBySubject.getStatus().getType().equals(Status.StatusType.IN_PROGRESS)){
			return true;
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean checkSubIssueInSprintBySubject(Issue issue,Sprint sprint) {
		String hql = "SELECT issue FROM Issue issue WHERE issue.subject = :subjectIssue and issue.sprint.sprintId = :sprintId";
		List<Issue> issues = sessionFactory.getCurrentSession().createQuery(hql)
				.setParameter("subjectIssue", issue.getSubject())
				.setParameter("sprintId", sprint.getSprintId())
				.list();
		if(issues.isEmpty()){
			return false;
		}else{
			return true;
		}
		
	}

	@SuppressWarnings("unchecked")
	@Override
	public Issue findNewestIssueAvailableBySubjectNotInSprint(Issue issue,
			Sprint exceptSprint) {
		if(exceptSprint == null){
			return issue;
		}
		//parent issue
		String hql = "SELECT issue FROM Issue issue"
				+ " WHERE issue.subject = :subject"
				+ " AND issue.parent IS NULL"
				+ " AND issue.sprint.sprintId != :sprintId"
				+ " ORDER BY issue.issueId DESC";
		if(issue.getParent() != null){
			hql = "SELECT issue FROM Issue issue"
					+ " WHERE issue.subject = :subject"
					+ " AND issue.parent IS NOT NULL"
					+ " AND issue.sprint.sprintId != :sprintId"
					+ " ORDER BY issue.issueId DESC";
		}
		List<Issue> issuesHaveTheSameName = sessionFactory.getCurrentSession().createQuery(hql)
				.setParameter("subject", issue.getSubject())
				.setParameter("sprintId", exceptSprint.getSprintId())
				.list();
		if(!issuesHaveTheSameName.isEmpty()){
			return issuesHaveTheSameName.get(0);
		}
		return issue;
		
	}

	@Override
	public Issue findIssueByUserStoryAndSprintAndCurrentIssue(
			Issue currentIssue, Sprint sprint) {
		try {
            Criteria cri = this.sessionFactory.getCurrentSession().createCriteria(Issue.class);
            cri.add(Restrictions.eq("userStory.userStoryId", currentIssue.getParent().getUserStory().getUserStoryId()));
            cri.add(Restrictions.eq("sprint.sprintId", sprint.getSprintId()));
            cri.add(Restrictions.eq("subject", currentIssue.getParent().getSubject()));
            cri.addOrder(Order.asc("issueId"));
            cri.setCacheable(true);
//            Issue result = (Issue) cri.uniqueResult();
            List<Issue> result = cri.list();
            if(!result.isEmpty()){
            	return result.get(0);
            }
            return null;
        } catch (HibernateException e) {
        	e.printStackTrace();
            LOGGER.error(e.getMessage());
            return null;
        }
		
	}

	@SuppressWarnings("unchecked")
	@Override
	public Issue findOldestTaskOfUserStory(Long userStoryId) {
		Issue result = null;
		try {
			result = (Issue) sessionFactory.getCurrentSession()
									.createQuery("SELECT issue FROM Issue issue"
											+ " WHERE issue.userStory.userStoryId = :userStoryId"
											+ " ORDER BY issue.issueId ASC")
											.setParameter("userStoryId", userStoryId)
											.setMaxResults(1)
											.uniqueResult();
		} catch (HibernateException ex) {
			LOGGER.error("Error at findOldestTaskOfUserStory", ex);
		}
		return result;
	}

	@Override
	public Issue findNewestTaskOfUserStory(Long userStoryId) {
		Issue result = null;
		try {
			result = (Issue) sessionFactory.getCurrentSession()
									.createQuery("SELECT issue FROM Issue issue"
											+ " WHERE issue.userStory.userStoryId = :userStoryId"
											+ " ORDER BY issue.issueId DESC")
											.setParameter("userStoryId", userStoryId)
											.setMaxResults(1)
											.uniqueResult();
		} catch (HibernateException ex) {
			LOGGER.error("Error at findNewestTaskOfUserStory", ex);
		}
		return result;
	}

	@Override
	public Sprint findSprintHasContinuousIssue(Issue issue) {
		String hql = "SELECT nextIssue.sprint FROM Issue nextIssue"
				+ " WHERE nextIssue.previousId = :currentIssueId";
		Sprint resultSprint = (Sprint) sessionFactory.getCurrentSession()
				.createQuery(hql)
				.setParameter("currentIssueId", issue.getIssueId())
				.uniqueResult();
		return resultSprint;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Sprint> getAllSprintBeforeByIssue(Issue issue) {
		String hql = "SELECT issue.sprint FROM Issue issue"
				+ " WHERE issue.userStory.userStoryId = :userstoryId"
				+ " AND issue.issueId != :currentIssueId"
				+ " AND issue.sprint.sprintId < :currentSprint"
				+ " ORDER BY issue.sprint.sprintId DESC";
		
		List<Sprint> allSprintBefore = sessionFactory.getCurrentSession()
				.createQuery(hql)
				.setParameter("userstoryId", issue.getUserStory().getUserStoryId())
				.setParameter("currentIssueId", issue.getIssueId())
				.setParameter("currentSprint", issue.getSprint().getSprintId())
				.list();
		
		return allSprintBefore;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Sprint> getAllSprintAfterByIssue(Issue issue) {
		String hql = "SELECT issue.sprint FROM Issue issue"
				+ " WHERE issue.userStory.userStoryId = :userstoryId"
				+ " AND issue.issueId != :currentIssueId"
				+ " AND issue.sprint.sprintId > :currentSprint"
				+ " ORDER BY issue.sprint.sprintId DESC";
		
		List<Sprint> allSprintAfter = sessionFactory.getCurrentSession()
				.createQuery(hql)
				.setParameter("userstoryId", issue.getUserStory().getUserStoryId())
				.setParameter("currentIssueId", issue.getIssueId())
				.setParameter("currentSprint", issue.getSprint().getSprintId())
				.list();
		
		return allSprintAfter;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Issue> loadLazyIssuesBySprintId(Long sprintId,
			IssueStateLazyLoading issueStateLazyLoading) {
		
		Session session = sessionFactory.getCurrentSession();
		
		List<Long> listIssueIdHaveChildIssue = new ArrayList<Long>();
		listIssueIdHaveChildIssue = session.createCriteria(Issue.class)
	    		.add(Restrictions.eq("sprint.sprintId", sprintId))
	    		.setCacheable(true)
	    		.add(Restrictions.isNotNull("parent"))
	    		.setProjection(Projections.distinct(Projections.property("parent.issueId"))).list();
	    
		Criteria criteria = session.createCriteria(Issue.class);
		criteria
		.add(Restrictions.eq("sprint.sprintId", sprintId))
		.add(Restrictions.eq("isVoid", false));
		
		if(!listIssueIdHaveChildIssue.isEmpty()){
			criteria.add(Restrictions.not(Restrictions.in("issueId", listIssueIdHaveChildIssue)));
		}
		
		List<LazyFilterInList> lazyFilterInLists = issueStateLazyLoading.getFilterInLists();
		if(lazyFilterInLists != null && !lazyFilterInLists.isEmpty()){
			for (LazyFilterInList lazyFilterInList : lazyFilterInLists) {
				if(!("Any").equals(lazyFilterInList.getValues())){
					criteria.add(Restrictions.eq(lazyFilterInList.getField(), lazyFilterInList.getValues()));
				}
			}
		}
		
		String fieldSort = issueStateLazyLoading.getSorters().getField();
		if(issueStateLazyLoading.getSorters().getValue() == LAZYSORTER_VALUE.ASC){
			criteria.addOrder(Order.asc(fieldSort));
		}else{
			criteria.addOrder(Order.desc(fieldSort));
		}
		criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
		criteria.setMaxResults(issueStateLazyLoading.getLimit());
		criteria.setFirstResult(issueStateLazyLoading.getStart());
		List<Issue> issues = new ArrayList<Issue>();
		issues = criteria.list();
		return issues;
	}
	
		@SuppressWarnings("unchecked")
	@Override
	public List<Issue> findLazyIssuesParentByProjectIdAndSprintId(
			long projectId, long sprintId, StateLazyLoading lazyLoadingIsuseList) {
		//prepare statement
		String statementInit = "SELECT issue FROM Issue issue"
				+ " INNER JOIN issue.sprint sprint"
				+ " INNER JOIN issue.userStory userStory"
				+ " INNER JOIN userStory.project project"
				+ " WHERE project.projectId = :projectId"
				+ " AND sprint.sprintId = :sprintId"
				+ " AND issue.isVoid = false ";
		//prepare filter
		String statementFilter = "";
		for(String field : lazyLoadingIsuseList.getFilters().getField()){
			statementFilter = statementFilter 
					+ " lower(issue.{field}) like lower(:valueFilter) or".replace("{field}", field);
		}
		//remove or
		statementFilter = statementFilter.substring(0,statementFilter.length() - 3);
		
		//prepare
		String statementOrder = "";
		if(lazyLoadingIsuseList.getSorters().getField() != null){
			statementOrder = "ORDER BY issue.{field} {value}";
			statementOrder = statementOrder.replace("{field}", lazyLoadingIsuseList.getSorters().getField())
					.replace("{value}", lazyLoadingIsuseList.getSorters().getValue().toString());
		}
		
		String finalStatement =  statementInit + " AND ( " + statementFilter + " ) " + statementOrder;
		
		List<Issue> lazyIssuies = sessionFactory.getCurrentSession()
				.createQuery(finalStatement)
				.setParameter("projectId", projectId)
				.setParameter("sprintId", sprintId)
				.setParameter("valueFilter", "%" + lazyLoadingIsuseList.getFilters().getValue().toString().trim() + "%")
				.setFirstResult(lazyLoadingIsuseList.getStart())
				.setMaxResults(lazyLoadingIsuseList.getLimit())
				.list();
		return lazyIssuies;
	}

		@SuppressWarnings("unchecked")
		@Override
		public List<Issue> findLazyIssueBySprintId(long sprintId,
				StateLazyLoading lazyLoadingSprintBacklogs) {
			String hql = "SELECT issue FROM Issue issue"
					+ " WHERE issue.sprint.sprintId = :sprintId"
					+ " AND issue.parent IS NULL"
					+ " ORDER BY issue.issueId DESC";
			
			List<Issue> issues = sessionFactory.getCurrentSession()
					.createQuery(hql)
					.setParameter("sprintId", sprintId)
					.setFirstResult(lazyLoadingSprintBacklogs.getStart())
					.setMaxResults(lazyLoadingSprintBacklogs.getLimit())
					.list();
			return issues;
		}

		@Override
		public int countTotalLazyIssueBySprintId(long sprintId,
				StateLazyLoading lazyLoadingSprintBacklogs) {
			String hql = "SELECT count(*) FROM Issue issue"
					+ " WHERE issue.sprint.sprintId = :sprintId"
					+ " AND issue.parent IS NULL";
			Long totalRow = (Long) sessionFactory.getCurrentSession()
					.createQuery(hql)
					.setParameter("sprintId", sprintId)
					.uniqueResult();
			return totalRow.intValue();
		}

		@SuppressWarnings("unchecked")
		@Override
		public int countTotalLazyIssuesParentByProjectIdAndSprintId(
				long projectId, long sprintId,
				StateLazyLoading lazyLoadingIsuseList) {
			//prepare statement
			String statementInit = "SELECT issue FROM Issue issue"
					+ " INNER JOIN issue.sprint sprint"
					+ " INNER JOIN issue.userStory userStory"
					+ " INNER JOIN userStory.project project"
					+ " WHERE project.projectId = :projectId"
					+ " AND sprint.sprintId = :sprintId"
					+ " AND issue.isVoid = false ";
			//prepare filter
			String statementFilter = "";
			for(String field : lazyLoadingIsuseList.getFilters().getField()){
				statementFilter = statementFilter 
						+ " lower(issue.{field}) like lower(:valueFilter) or".replace("{field}", field);
			}
			//remove or
			statementFilter = statementFilter.substring(0,statementFilter.length() - 3);
			
			String finalStatement =  statementInit + " AND ( " + statementFilter + " ) ";
			
			List<Issue> issues  = sessionFactory.getCurrentSession()
					.createQuery(finalStatement)
					.setParameter("projectId", projectId)
					.setParameter("sprintId", sprintId)
					.setParameter("valueFilter", "%" + lazyLoadingIsuseList.getFilters().getValue().toString().trim() + "%")
					.list();
			int totalRow = 0;
			for(Issue issueitem : issues){
				totalRow ++;
				List<Issue> subissues = findIssueByParent(issueitem);
				totalRow  = totalRow + subissues.size();
			}
			
			return totalRow;
		}

		@SuppressWarnings("unchecked")
		@Override
		public int countTotalLazyIssuesBySprintId(Long sprintId,
				IssueStateLazyLoading issueStateLazyLoading) {
			
			Session session = sessionFactory.getCurrentSession();
			List<Long> listIssueIdHaveChildIssue = new ArrayList<Long>();
			listIssueIdHaveChildIssue = session.createCriteria(Issue.class)
		    		.add(Restrictions.eq("sprint.sprintId", sprintId))
		    		.setCacheable(true)
		    		.add(Restrictions.isNotNull("parent"))
		    		.setProjection(Projections.distinct(Projections.property("parent.issueId"))).list();
		    
			Criteria criteria = session.createCriteria(Issue.class);
			criteria
			.add(Restrictions.eq("sprint.sprintId", sprintId))
			.add(Restrictions.eq("isVoid", false));
			
			if(!listIssueIdHaveChildIssue.isEmpty()){
				criteria.add(Restrictions.not(Restrictions.in("issueId", listIssueIdHaveChildIssue)));
			}
			
			List<LazyFilterInList> lazyFilterInLists = issueStateLazyLoading.getFilterInLists();
			if(lazyFilterInLists != null && !lazyFilterInLists.isEmpty()){
				for (LazyFilterInList lazyFilterInList : lazyFilterInLists) {
					if(!("Any").equals(lazyFilterInList.getValues())){
						criteria.add(Restrictions.eq(lazyFilterInList.getField(), lazyFilterInList.getValues()));
					}
				}
			}
			
			Long totalRowIssue = (Long) criteria.setProjection(Projections.rowCount()).uniqueResult();;
			return totalRowIssue.intValue();
		}

		@SuppressWarnings("unchecked")
		@Override
		public List<Issue> findAllLazyIssuesBySprint(long sprintId) {
			String hql = "SELECT issue FROM Issue issue"
					+ " WHERE issue.sprint.sprintId = :sprintId"
					+ " AND issue.parent IS NULL"
					+ " AND issue.isVoid = false"
					+ " ORDER BY issue.issueId DESC";
			List<Issue> issues = sessionFactory.getCurrentSession()
					.createQuery(hql)
					.setParameter("sprintId", sprintId)
					.list();
			return issues;
		}

		@SuppressWarnings("unchecked")
		@Override
		public List<Issue> getIssueOrderByStatus(long statusId) 
		{
			LOGGER.warn("begin daoimpl get issue order by status");
			List<Issue> issues = new ArrayList<Issue>();
			try {
				
				String hqlGetIssueOrderByStatus = "SELECT issue FROM Issue issue"
						+ " WHERE issue.status.statusId = :statusId"
						+ " AND issue.isVoid = false"
						+ " AND NOT EXISTS (SELECT childIssue FROM Issue childIssue WHERE childIssue.parent = issue)"
						+ " ORDER BY issue.orderIssue ASC";
				issues = sessionFactory.getCurrentSession()
						.createQuery(hqlGetIssueOrderByStatus)
						.setParameter("statusId", statusId)
						.list();
				return issues;
			} catch (Exception e) {
				LOGGER.warn("error get issue order by status unsuccessful",e);
				return issues;
			}
		}

		@SuppressWarnings("unchecked")
		@Override
		public List<Issue> getIssueOrderByStatusAndSwimline(long statusid) 
		{
			LOGGER.warn("begin daoimpl get issue order by status and swimline");
			List<Issue> issues = new ArrayList<Issue>();
			try {
				String hqlGetIssueOrderByStatusAndSwimline = "SELECT issue FROM Issue issue"
						+ " WHERE issue.status.statusId = :statusId"
						+ " AND issue.isSwimLine IS TRUE"
						+ " AND issue.isVoid = false"
						+ " AND NOT EXISTS (SELECT childIssue FROM Issue childIssue WHERE childIssue.parent = issue)"
						+ " ORDER BY issue.orderIssue ASC";
				issues = sessionFactory.getCurrentSession()
						.createQuery(hqlGetIssueOrderByStatusAndSwimline)
						.setParameter("statusId", statusid)
						.list();
				return issues;
			} catch (Exception e) {
				LOGGER.warn("error get issue order by status and swimline unsuccessful",e);
				return issues;
			}
		}

		@SuppressWarnings("unchecked")
		@Override
		public List<Issue> getIssueOrderByStatusAndNotInSwimline(long statusId) 
		{
			LOGGER.warn("begin daoimpl get issue order by status and not in swimline");
			List<Issue> issues = new ArrayList<Issue>();
			try {
				String hqlGetIssueOrderByStatusAndNotInSwimline = "SELECT issue FROM Issue issue"
						+ " WHERE issue.status.statusId = :statusId"
						+ " AND ( issue.isSwimLine IS FALSE OR issue.isSwimLine IS NULL )"
						+ " AND issue.isVoid = false"
						+ " AND NOT EXISTS (SELECT childIssue FROM Issue childIssue WHERE childIssue.parent = issue)"
						+ " ORDER BY issue.orderIssue ASC";
				issues = sessionFactory.getCurrentSession()
						.createQuery(hqlGetIssueOrderByStatusAndNotInSwimline)
						.setParameter("statusId", statusId)
						.list();
				return issues;
			} catch (Exception e) {
				LOGGER.warn("error get issue order by status and not in swimline unsuccessful",e);
				return issues;
			}
		}

		@Override
		public List<Issue> allIssueLazyIssuesBySprintId(Long sprintId, IssueStateLazyLoading issueStateLazyLoading) 
		{
			try {
				Session session = sessionFactory.getCurrentSession();
				List<Long> listIssueIdHaveChildIssue = new ArrayList<Long>();
				listIssueIdHaveChildIssue = session.createCriteria(Issue.class)
			    		.add(Restrictions.eq("sprint.sprintId", sprintId))
			    		.setCacheable(true)
			    		.add(Restrictions.isNotNull("parent"))
			    		.setProjection(Projections.distinct(Projections.property("parent.issueId"))).list();
			    
				Criteria criteria = session.createCriteria(Issue.class);
				criteria
				.add(Restrictions.eq("sprint.sprintId", sprintId));
				
				if(!listIssueIdHaveChildIssue.isEmpty()){
					criteria.add(Restrictions.not(Restrictions.in("issueId", listIssueIdHaveChildIssue)));
				}
				
				List<LazyFilterInList> lazyFilterInLists = issueStateLazyLoading.getFilterInLists();
				if(lazyFilterInLists != null && !lazyFilterInLists.isEmpty()){
					for (LazyFilterInList lazyFilterInList : lazyFilterInLists) {
						if(!("Any").equals(lazyFilterInList.getValues())){
							criteria.add(Restrictions.eq(lazyFilterInList.getField(), lazyFilterInList.getValues()));
						}
					}
				}
				criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
				return criteria.list();
			} catch (Exception e) {
				e.printStackTrace();
				LOGGER.error("error get all issue lazy by sprint",e);
			}
			return new ArrayList<Issue>();
		}

		
		@Override
		public void setVoidAllUnexpiredIssuesWhenSetUserStoryVoid(Long userStoryId) 
		{
			List<Long> issueIdsNeedToSetVoid = selectParent(userStoryId);
			if (issueIdsNeedToSetVoid.size() > 0) {

				List<Long> childrenIds = selectChildren(issueIdsNeedToSetVoid);

				if (childrenIds.size() > 0) {
					for (Long l : childrenIds) {
						issueIdsNeedToSetVoid.add(l);
					}
				}

				setVoidAllUnexpiredIssues(issueIdsNeedToSetVoid);
			}
		}
		
		private void setVoidAllUnexpiredIssues(List<Long> issueIdsNeedToSetVoid)
		{
			try{
				String hql = "UPDATE Issue issue"
					+ " SET issue.isVoid = true"
					+ " WHERE issue.id IN :issueId";
				sessionFactory.getCurrentSession().createQuery(hql)
					.setParameterList("issueId", issueIdsNeedToSetVoid)
					.executeUpdate();
				}catch(HibernateException e){
					LOGGER.error("Error when setVoidAllUnexpiredIssues: " + e);
				}
		}
		
		
		@Override
		public void destroyVoidAllUnexpiredIssuesWhenDestroyVoidUserStory(Long userStoryId)
		{
			List<Long> issueIdsNeedToDestroyVoid = selectParent(userStoryId);
			if (issueIdsNeedToDestroyVoid.size() > 0) {

				List<Long> childrenIds = selectChildren(issueIdsNeedToDestroyVoid);

				if (childrenIds.size() > 0) {
					for (Long l : childrenIds) {
						issueIdsNeedToDestroyVoid.add(l);
					}
				}

				destroyVoidAllUnexpiredIssues(issueIdsNeedToDestroyVoid);
			}
		}
		
		private void destroyVoidAllUnexpiredIssues( List<Long> issueIdsNeedToDestroyVoid) 
		{
			try{
				String hql = "UPDATE Issue issue"
					+ " SET issue.isVoid = false"
					+ " WHERE issue.id IN (:issueId)";
				sessionFactory.getCurrentSession().createQuery(hql)
					.setParameterList("issueId", issueIdsNeedToDestroyVoid)
					.executeUpdate();
				}catch(HibernateException e){
					LOGGER.error("Error when destroyVoidAllUnexpiredIssues: " + e);
				}
			
		}

		private List<Long> selectChildren(List<Long> parentIds)
		{
			String hql = "SELECT iss.issueId FROM Issue iss"
					+ " WHERE iss.parent.issueId IN (:parent)";
			
			@SuppressWarnings("unchecked")
			List<Long> childrenIds = sessionFactory.getCurrentSession().createQuery(hql).setParameterList("parent", parentIds).list();
			return childrenIds;
		}
		
		private List<Long> selectParent(Long userStoryId)
		{
			String hql = "SELECT iss.issueId FROM Issue iss"
					+ " WHERE iss.userStory.userStoryId = :userStoryId"
					+ " AND iss.sprint.sprintId IN"
					+ "     (SELECT sp.sprintId FROM Sprint sp WHERE sp.dateEnd > current_timestamp())";
			
			@SuppressWarnings("unchecked")
			List<Long> parentIds = sessionFactory.getCurrentSession().createQuery(hql).setParameter("userStoryId", userStoryId).list();
			return parentIds;
			
		}	
		
		@Override
		public List<Issue> findAllUnexpiredChildrenIssuesByUserStory(Long userStoryId)
		{
			String hql = "SELECT iss.issueId FROM Issue iss"
					+ " WHERE iss.userStory.userStoryId = :userStoryId"
					+ " AND iss.sprint.sprintId IN"
					+ "     (SELECT sp.sprintId FROM Sprint sp WHERE sp.dateEnd > current_timestamp())";
			@SuppressWarnings("unchecked")
			List<Long> parentIssuesIds = sessionFactory.getCurrentSession().createQuery(hql)
					.setParameter("userStoryId", userStoryId).list();
			List<Issue> allChildren = new ArrayList<Issue>();
			if(parentIssuesIds.size() > 0){
				List<Long> childrenIds = selectChildren(parentIssuesIds);
				if(childrenIds.size() > 0){
					for(Long id: childrenIds){
						allChildren.add(findIssueById(id));
					}
				}else{
					for(Long id: parentIssuesIds){
						allChildren.add(findIssueById(id));
					}
				}
			}
			return allChildren;
		}
		
}
