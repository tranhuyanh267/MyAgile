package com.ant.myagile.service.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ant.myagile.dao.MemberDao;
import com.ant.myagile.dao.UserStoryDao;
import com.ant.myagile.dto.StateLazyLoading;
import com.ant.myagile.model.Issue;
import com.ant.myagile.model.KanbanIssue;
import com.ant.myagile.model.KanbanStatus;
import com.ant.myagile.model.Member;
import com.ant.myagile.model.Sprint;
import com.ant.myagile.model.Status;
import com.ant.myagile.model.UserStory;
import com.ant.myagile.model.UserStory.StatusType;
import com.ant.myagile.service.HistoryService;
import com.ant.myagile.service.IssueService;
import com.ant.myagile.service.KanbanIssueService;
import com.ant.myagile.service.LastUserStoryIndexService;
import com.ant.myagile.service.UserStoryService;
import com.ant.myagile.utils.Utils;


@Service
@Transactional
public class UserStoryServiceImpl implements UserStoryService, Serializable {
	private static final long serialVersionUID = 1L;
	public static final int NUM_DONE =100;
	private static final Logger LOGGER = Logger.getLogger(UserStoryServiceImpl.class.getName());

	@Autowired
	private UserStoryDao userStoryDao;
	@Autowired
	private HistoryService historyService;
	@Autowired
	private IssueService issueService;
	@Autowired
	private MemberDao memberDao;
	@Autowired
	LastUserStoryIndexService lastUserStoryIndexService;
	@Autowired
	private Utils utils;
	@Autowired
	private KanbanIssueService kanbanIssueService;


	public List<UserStory> findAllUserStoryByProjectId(long projectId) {
		return this.userStoryDao.findAllUserStoryByProjectId(projectId);
	}

	@Override
	public boolean create(UserStory userStory) {
		
		Long index = lastUserStoryIndexService.findLastUserStoryIndex(userStory.getProject().getProjectId());
		userStory.setSortId(index+1);
		userStory.setStatus(UserStory.StatusType.TODO);
		lastUserStoryIndexService.updateIndex(userStory.getProject(), userStory.getSortId());
		LOGGER.warn("create userstory:" + userStory.getName() +", status:" + userStory.getStatus() +", priority:" + userStory.getPriority());		
        if(this.userStoryDao.create(userStory)){
        	LOGGER.warn("create userstory success");
			Member author = utils.getLoggedInMember();
			this.historyService.saveHistoryForProductBL(userStory,"Create", author);
			return true;
		}
        LOGGER.warn("create userstory fail!");
		return false;
	}

	@Override
    public boolean createUserStoryFromEmail(UserStory userStory, String email) {
		Long index = lastUserStoryIndexService.findLastUserStoryIndex(userStory.getProject().getProjectId());
        userStory.setSortId(index + 1);
        userStory.setStatus(UserStory.StatusType.TODO);
        lastUserStoryIndexService.updateIndex(userStory.getProject(), userStory.getSortId());
        if(userStoryDao.create(userStory)) {
            Member author = memberDao.findMemberByEmail(email);
            this.historyService.saveHistoryForProductBL(userStory,"Create", author);
            return true;
        }
        return false;
	}

	@Override
	public boolean update(UserStory userStory) {
		LOGGER.warn("update userstory");
		this.utils.evict(userStory);
		UserStory oldUserStory = this.userStoryDao.findUserStoryById(userStory.getUserStoryId());	
				
		updateKanbanIssues(oldUserStory, userStory );
		updateScrumIssues(oldUserStory, userStory );
		
		
		if(userStory!=null && userStory.getStatus() == null){
			LOGGER.warn("status null, set status to 'TODO'");
			userStory.setStatus(UserStory.StatusType.TODO);
		}
		
		if(this.userStoryDao.updateUserStory(userStory)){
			this.utils.evict(oldUserStory);
			this.historyService.saveHistoryForUpdateProductBL(oldUserStory, userStory);
			LOGGER.warn("update userstory success");
			return true;
		}
		LOGGER.warn("update userstory fail");
		return false;
	}

	private void updateKanbanIssues( UserStory oldUserStory, UserStory newUserStory) {
		try {
			if(oldUserStory.getKanbanIssues()!=null){
				for (KanbanIssue item : oldUserStory.getKanbanIssues()) {
					if(item.getIsSubIssue() == false){ // copy from user story
						item.setDescription(newUserStory.getDescription());
						item.setSubject(newUserStory.getName());
						newUserStory.setKanbanIssues(oldUserStory.getKanbanIssues());
					}						
				}					
			}else{
				LOGGER.warn("Kanban issuses null: id:"+ oldUserStory.getUserStoryId());
			}
		} catch (Exception e) {
			LOGGER.error("update kanban issuses: error :"+ e.getMessage()+"of user story: id:" + oldUserStory.getUserStoryId(),e);
		}
		LOGGER.warn("Update kanban issuse success : id"+ oldUserStory.getUserStoryId() +":" + newUserStory.getName());
	}
	
	private void updateScrumIssues( UserStory oldUserStory, UserStory newUserStory) {
		try {
			if(oldUserStory.getIssues()!=null){
				for (Issue item : oldUserStory.getIssues()) {
//					if(item.getIsParent()){ // copy from user story
						item.setDescription(newUserStory.getDescription());
						item.setSubject(newUserStory.getName());
						newUserStory.setKanbanIssues(oldUserStory.getKanbanIssues());
//					}						
				}				
			}else{
				LOGGER.warn("Issuses null: id:"+ oldUserStory.getUserStoryId());
			}
		} catch (Exception e) {
			LOGGER.error("update issuses: error :"+ e.getMessage()+"of user story: id:" + oldUserStory.getUserStoryId(),e);
		}
		LOGGER.warn("Update issuse success : id"+ oldUserStory.getUserStoryId() +":" + newUserStory.getName());
	}

	@Override
	public boolean delete(long userStoryId) {
		Member author = utils.getLoggedInMember();
		this.historyService.saveHistoryForProductBL(this.userStoryDao.findUserStoryById(userStoryId),"Delete", author);
		return this.userStoryDao.deleteUserStory(userStoryId);
	}

	@Override
	public UserStory checkExistUserStory(String name, long projectId) {
		return this.userStoryDao.checkExistUserStory(name, projectId);
	}

	@Override
	public UserStory findUserStoryById(long userStoryId) {
		return this.userStoryDao.findUserStoryById(userStoryId);
	}

	@Override
	public List<UserStory> findUserStoriesByProjectId(Long projectId) {
		return this.userStoryDao.findUserStoriesByProjectId(projectId);
	}

	@Override
	public UserStory findLastUserStoryInProject(Long projectId) {
		return this.userStoryDao.findLastUserStoryInProject(projectId);
	}

	@Override
	public List<UserStory> findUserStoriesByCriteria(List<UserStory.StatusType> selectedFilter, long projectId, String sortField, int typeOfSort, String searchedKeyWord) {
		return this.userStoryDao.findUserStoriesByCriteria(selectedFilter, projectId, sortField, typeOfSort, searchedKeyWord);
	}

	@Override
	public int findProgressOfUserStory(UserStory userStory) {
		try {
			List<Issue> issues = this.issueService.findIssuesByUserStory(userStory.getUserStoryId());
			float remain = 0, estimate = 0;
			if (issues == null || issues.size() == 0) {
				return 0;
			}
			boolean flagDone = true;
			for (Issue child : issues) {
				if (!this.issueService.checkStatusDoneOfIssue(child)) {
					flagDone = false;
				}
				remain += this.issueService.findTotalRemainPoint(child);
				estimate += this.issueService.findTotalEstimatePoint(child);
			}
			if (flagDone) {
				return NUM_DONE;
			}
			if (estimate == 0) {
				return 0;
			}
			int result = NUM_DONE - (int) (remain * NUM_DONE / estimate);

			return Math.min(NUM_DONE, Math.max(0, result));
		} catch (Exception e) {
			LOGGER.error(e);
		}
		return 0;
	}

	@Override
	public UserStory.StatusType findStatusOfUserStory(UserStory userStory) {
		List<Issue> issue=new ArrayList<Issue>();
		try {
			issue.addAll(issueService.findNoChidrenIssuesByUserStory(userStory.getUserStoryId()));
			if (issue.size() == 0) {
				return UserStory.StatusType.TODO;
			}
			for (Issue child : issue) {
				if ((child.getStatus() == null) || ((child.getStatus().getType() != Status.StatusType.DONE)
						&& (child.getStatus().getType() != Status.StatusType.ACCEPTED_HIDE) 
						&& (child.getStatus().getType() != Status.StatusType.ACCEPTED_SHOW))) {
					return UserStory.StatusType.IN_PROGRESS;
				}
			}
			return UserStory.StatusType.DONE;
		} catch (Exception e) {
			LOGGER.error("findStatusOfUserStory throw exception: "+e.getMessage());
		}
		return UserStory.StatusType.TODO;
	}
	
	@Override
	public UserStory findUserStoryByIssue(Issue issue) {
		return userStoryDao.findUserStoryByIssue(issue);
	}

	@Override
	public List<UserStory> findAllUserStoryToDoByProjectId(long projectId) {
		return userStoryDao.findAllUserStoryToDoByProjectId(projectId);
	}
	
	@Override
	public List<UserStory> findAllUserStoryByIdList(List<Long> userStoryIdList) {
		return userStoryDao.findAllUserStoryByIdList(userStoryIdList);
	}

	@Override
	public List<UserStory> findAllUserStoryToDoAndProgress(long idProject) {
		return userStoryDao.findAllUserStoryToDoAndProgress(idProject);
	}

	@Override
	public UserStory checkUserStoryForEdit(String name, long projectId,
			long userStoryID) {
		// TODO Auto-generated method stub
		return userStoryDao.checkUserStoryForEdit(name, projectId, userStoryID);
	}
	
	@Override
	public List<UserStory> findRemainingUserStoriesByProject(long projectId) {
		return userStoryDao.findRemainingUserStoriesByProject(projectId);
	}

	@Override
	public List<UserStory> loadLazyUserstoryProjectId(long projectId,
			StateLazyLoading stateUserstoryList) {
		return userStoryDao.loadLazyUserstoryProjectId(projectId,stateUserstoryList);
	}

	@Override
	public List<UserStory> findLazyUserStoryByProjectNotInSprint(
			long projectId, long sprintId,
			StateLazyLoading lazyLoadingProductBacklogs) {
		return userStoryDao.findLazyUserStoryByProjectNotInSprint(projectId,sprintId,lazyLoadingProductBacklogs);
	}

	@Override
	public List<UserStory> findLazyUserStoryToDoAndProgress(
			Long currentProjectId, StateLazyLoading lazyLoadingProductBacklogs) {
		return userStoryDao.findLazyUserStoryToDoAndProgress(currentProjectId,lazyLoadingProductBacklogs);
	}

	@Override
	public int countTotalRowloadLazyUserstoryProjectId(long projectId,
			StateLazyLoading stateUserstoryList) {
		return userStoryDao.countTotalRowloadLazyUserstoryProjectId(projectId,stateUserstoryList);
	}

	@Override
	public int countTotalLazyUserStoryByProjectNotInSprint(long projectId,
			long sprintId, StateLazyLoading lazyLoadingProductBacklogs) {
		return userStoryDao.countTotalLazyUserStoryByProjectNotInSprint(projectId,sprintId,lazyLoadingProductBacklogs);
	}

	@Override
	public void updateAllIssueOfUserStoryHaveTheSameContent(UserStory us) {
		userStoryDao.updateAllIssueOfUserStoryHaveTheSameContent(us);
	}

	@Override
	public int countTotalUserStoryToDoAndProgress(Long currentProjectId) {
		return userStoryDao.countTotalUserStoryToDoAndProgress(currentProjectId);
	}

	@Override
	public void updateSubjectDescriptionNotePriorityOfAllIssueOfUserStory(
			UserStory userStoryOfIssue) {
		userStoryDao.updateSubjectDescriptionNotePriorityOfAllIssueOfUserStory(userStoryOfIssue);
	}

	@Override
	public StatusType findStatusOfUserStoryBySprintAndUserStory(
			UserStory userStory, Sprint sprint) {
		List<Issue> issue=new ArrayList<Issue>();
		try {
			issue.addAll(issueService.findIssuesByUserStory(userStory.getUserStoryId()));
			if (issue.size() == 0) {
				return UserStory.StatusType.TODO;
			}
			for (Issue child : issue) {
				if(child.getSprint().getSprintId().compareTo(sprint.getSprintId()) == 0){
					if ((child.getStatus() == null) || ((child.getStatus().getType() != Status.StatusType.DONE)
							&& (child.getStatus().getType() != Status.StatusType.ACCEPTED_HIDE) 
							&& (child.getStatus().getType() != Status.StatusType.ACCEPTED_SHOW))) {
						return UserStory.StatusType.IN_PROGRESS;
					}
				}
			}
			return UserStory.StatusType.DONE;
		} catch (Exception e) {}
		return null;
	}

	@Override
	public List<UserStory> loadAllLazyUserstoryProjectId(long projectId,
			StateLazyLoading stateUserstoryList) {
		return userStoryDao.loadAllLazyUserstoryProjectId(projectId,stateUserstoryList);
	}
}
