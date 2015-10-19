package com.ant.myagile.service.impl;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.stringtemplate.v4.compiler.STParser.list_return;

import com.ant.myagile.dao.KanbanIssueDao;
import com.ant.myagile.dao.KanbanStatusDao;
import com.ant.myagile.dao.KanbanSwimlineDao;
import com.ant.myagile.model.Issue;
import com.ant.myagile.model.KanbanIssue;
import com.ant.myagile.model.KanbanStatus;
import com.ant.myagile.model.KanbanSwimline;
import com.ant.myagile.model.Status;
import com.ant.myagile.model.Team;
import com.ant.myagile.model.UserStory;
import com.ant.myagile.service.KanbanIssueService;
import com.ant.myagile.utils.Utils;
import com.itextpdf.text.pdf.PdfStructTreeController.returnType;

@Service
@Transactional
public class KanbanIssueServiceImpl implements KanbanIssueService{

	private static final Logger logger = Logger.getLogger(KanbanIssueServiceImpl.class);
	
	@Autowired
	SessionFactory sessionFactory;
	
	@Autowired
	KanbanIssueDao kanbanIssueDao;
	@Autowired
	KanbanStatusDao kanbanStatusDao;
	@Autowired
	KanbanSwimlineDao kanbanSwimlineDao;
	
	public static final int MAX_PERCENT = 100;
	
	@Override
	public boolean saveKanbanIssue(KanbanIssue issue) {
		return kanbanIssueDao.saveKanbanIssue(issue);
	}

	@Override
	public boolean updateKanbanIssue(KanbanIssue issue) {
		return kanbanIssueDao.updateKanbanIssue(issue);
	}

	@Override
	public void deleteKanbanIssue(KanbanIssue issue) {
		kanbanIssueDao.deleteKanbanIssue(issue);
	}

	@Override
	public long saveKanbanIssueAndGetId(KanbanIssue issue) {
		return kanbanIssueDao.saveKanbanIssueAndGetId(issue);
	}

	@Override
	public KanbanIssue findKanbanIssueById(long issueId) {
		return kanbanIssueDao.findKanbanIssueById(issueId);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<KanbanIssue> findKanbanIssuesByUserStory(Long userStoryId) {
		List<KanbanIssue> lKanbanIssue = sessionFactory.getCurrentSession()
				.createCriteria(KanbanIssue.class)
				.add(Restrictions.eq("userStory.userStoryId",userStoryId))
				.addOrder(Order.asc("issueId")).setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).list();
		return lKanbanIssue;
	}

	@Override
	public KanbanIssue findKanbanIssueParentByIssueChild(KanbanIssue issue) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<KanbanIssue> getListKanbanIssueByUserstory(long idUserStory) {
		// TODO Auto-generated method stub
		return kanbanIssueDao.getListKanbanIssueByUserstory(idUserStory);
	}

	@Override
	public KanbanIssue checkDuplicate(String subject, long userStoryId,long id) {
		// TODO Auto-generated method stub
		return kanbanIssueDao.checkDuplicate(subject, userStoryId ,id);
	}

	@Override
	public List<KanbanIssue> getListKanbanIssueNotNullByUserstory(
			long idUserstory) {
		return kanbanIssueDao.getListKanbanIssueNotNullByUserstory(idUserstory);
	}

	@Override
	public boolean checkAllKanbanIssueDoneOfUserstory(long idUserstory) {
		return kanbanIssueDao.checkAllKanbanIssueDoneOfUserstory(idUserstory);
	}

	@Override
	public int calculateProgress(KanbanIssue kanbanIssue) {
		try {
			float estimatePoint = Utils.convertPoint(kanbanIssue.getEstimate());
			if (estimatePoint > 0) {
				float remainPoint = Utils.convertPoint(kanbanIssue.getRemain());
				float numerator = ((estimatePoint - remainPoint) >= 0) ? estimatePoint
						- remainPoint
						: 0;
				return (int) ((numerator / estimatePoint) * MAX_PERCENT);
			} else if (kanbanIssue.getKanbanStatus().getType().equals(KanbanStatus.StatusType.DONE)) {
				return MAX_PERCENT;
			} else {
				return 0;
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("calculate project kanban issue error, please check remain and estimate point format",e);
			return 0;
		}
	}

	@Override
	public int calculateProgressUSForKanbanissue(UserStory us) {
		return kanbanIssueDao.calculateProgressUSForKanbanissue(us);
	}

	@Override
	public List<KanbanIssue> getAllKanbanIssuesByTeam(Team team) {
		return kanbanIssueDao.getAllKanbanIssuesByTeam(team);
	}

	@Override
	public void deleteAllKanbanIssuesByTeam(Team team) {
		List<KanbanIssue> kanbanIssues = kanbanIssueDao.getAllKanbanIssuesByTeam(team);
		if(kanbanIssues.size() > 0){
			for (KanbanIssue kanbanIssue : kanbanIssues) {
				kanbanIssueDao.deleteKanbanIssue(kanbanIssue);
			}
		}
		
	}

	@Override
	public boolean updateAllKanbanIssueByUserStoryStatusOfTeam(
			UserStory userStory, Team team) {
		List<KanbanIssue> kanbanIssueOfUserStory = kanbanIssueDao.findKanbanIssuesByUserStory(userStory.getUserStoryId());
		if(kanbanIssueOfUserStory != null && !kanbanIssueOfUserStory.isEmpty()){
			KanbanSwimline kanbanSwimLineDefault = kanbanSwimlineDao.firstSwimlineByTeamId(team.getTeamId());
			if(userStory.getStatus().equals(UserStory.StatusType.DONE)){
				KanbanStatus kanbanStatusDone = kanbanStatusDao.getKanbanStatusDoneByTeamId(team.getTeamId());
				for(KanbanIssue kanbanIssueItem : kanbanIssueOfUserStory){
					kanbanIssueItem.setKanbanStatus(kanbanStatusDone);
					if(kanbanIssueItem.getKanbanSwimline() == null) {
						kanbanIssueItem.setKanbanSwimline(kanbanSwimLineDefault);
					}
					kanbanIssueDao.updateKanbanIssue(kanbanIssueItem);
				}
			}else { 
				KanbanStatus kanbanStatusStart = kanbanStatusDao.getKanbanStatusStartByTeamId(team.getTeamId());
				for(KanbanIssue kanbanIssueItem : kanbanIssueOfUserStory){
					kanbanIssueItem.setKanbanStatus(kanbanStatusStart);
					if(kanbanIssueItem.getKanbanSwimline() == null) {
						kanbanIssueItem.setKanbanSwimline(kanbanSwimLineDefault);
					}
					kanbanIssueDao.updateKanbanIssue(kanbanIssueItem);
				}
			}
		}
		return true;
	}

	
	@Override
	public boolean existKanbanIssueByUserStoryAndSubject(UserStory userStory,
			String subject) {
		return kanbanIssueDao.existKanbanIssueByUserStoryAndSubject(userStory,subject);
	}

	@Override
	public void updateKanbanIssueByUserStoryAndSubjectAndIssue(
			UserStory userStory, String subject, Issue issue) {
		kanbanIssueDao.updateKanbanIssueByUserStoryAndSubjectAndIssue(userStory,subject,issue);
	}

	@Override
	public void deleteAllKanbanIssueByUserStoryAndSubject(UserStory userStory,
			String subject) {
		kanbanIssueDao.deleteAllKanbanIssueByUserStoryAndSubject(userStory,subject);
	}
	
	

	@Override
	public void deleteKanbanIssueByIssueId(Long issueId) {
		kanbanIssueDao.deleteKanbanIssueByIssueId(issueId);
	}

	@Override
	public void updateKanbanIssueByIssueId(Long issueId) {
		kanbanIssueDao.updateKanbanIssueByIssueId(issueId);
	}

	@Override
	public List<KanbanIssue> getKanbanIssueProgressByUserStory(
			UserStory userStoryOfKanbanIssue) {
		return kanbanIssueDao.getKanbanIssueProgressByUserStory(userStoryOfKanbanIssue);
	}

	@Override
	public KanbanIssue findKanbanIssueIsNotSubIssueOfUserStory(UserStory userStory) {
		return kanbanIssueDao.findKanbanIssueIsNotSubIssueOfUserStory(userStory);
	}

	@Override
	public void deleteAllKanbanIssueByUserStoryID(Long userStoryId) {
		kanbanIssueDao.deleteAllKanbanIssueByUserStoryID(userStoryId);
		
	}
	
	@Override
	public void setVoidAllKanbanIssueWhenSetVoidUserStory(Long userStoryId){
		kanbanIssueDao.setVoidAllKanbanIssueWhenSetVoidUserStory(userStoryId);
	}

	@Override
	public void destroyVoidAllKanbanIssueWhenDestroyVoidUserStory(Long userStoryId){
		kanbanIssueDao.destroyVoidAllKanbanIssueWhenDestroyVoidUserStory(userStoryId);
	}
}
