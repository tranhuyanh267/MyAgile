package com.ant.myagile.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ant.myagile.dao.KanbanHistoryDao;
import com.ant.myagile.model.Attachment;
import com.ant.myagile.model.KanbanHistory;
import com.ant.myagile.model.KanbanHistoryFieldChange;
import com.ant.myagile.model.KanbanIssue;
import com.ant.myagile.model.KanbanHistory.ActionType;
import com.ant.myagile.model.KanbanHistory.ContainerType;
import com.ant.myagile.model.KanbanStatus.StatusType;
import com.ant.myagile.model.Member;
import com.ant.myagile.model.MemberIssue;
import com.ant.myagile.service.KanbanHistoryService;
import com.ant.myagile.service.MemberIssueService;
import com.ant.myagile.utils.Utils;

@Service
@Transactional
public class KanbanHistoryServiceImpl implements KanbanHistoryService {

	private final static Logger logger = Logger.getLogger(KanbanHistoryServiceImpl.class);
	
	@Autowired
	private KanbanHistoryDao kanbanHistoryDao;
	@Autowired
	private Utils utils;
	@Autowired
	private MemberIssueService memberIssueService;
	

	@Override
	public boolean save(KanbanHistory kanbanHistory) {
		return kanbanHistoryDao.save(kanbanHistory);
	}

	@Override
	public boolean update(KanbanHistory kanbanHistory) {
		return kanbanHistoryDao.update(kanbanHistory);
	}

	@Override
	public boolean delete(KanbanHistory kanbanHistory) {
		return kanbanHistoryDao.delete(kanbanHistory);
	}

	@Override
	public void historyForAddKanbanIssue(KanbanIssue addTask) {
		//add history for add
    	KanbanHistory kanbanHistoryCreate = new KanbanHistory();
    	kanbanHistoryCreate.setActionType(ActionType.Create);
    	kanbanHistoryCreate.setContainerType(ContainerType.KanbanIssue);
    	kanbanHistoryCreate.setDateCreated(new Date());
    	kanbanHistoryCreate.setAuthor(utils.getLoggedInMember());
    	kanbanHistoryCreate.setContainerId(addTask.getIssueId());
    	kanbanHistoryDao.save(kanbanHistoryCreate);
	}

	@Override
	public void historyForDeleteKanbanIssue(KanbanIssue kanbanIssueDelete) {
		KanbanHistory historyDeleteKanbanIssue = new KanbanHistory();
		historyDeleteKanbanIssue.setContainerId(kanbanIssueDelete.getIssueId());
		historyDeleteKanbanIssue.setActionType(ActionType.Delete);
		historyDeleteKanbanIssue.setDateCreated(new Date());
		historyDeleteKanbanIssue.setContainerType(ContainerType.KanbanIssue);
		historyDeleteKanbanIssue.setAuthor(utils.getLoggedInMember());
		kanbanHistoryDao.save(historyDeleteKanbanIssue);
	}

	@Override
	public void historyForEditKanbanIssue(KanbanIssue kanbanIssueBeforeUpdate,
			KanbanIssue kanbanIssueAfterUpdate, List<Member> oldAssignees) {
		try {
			//add history
			KanbanHistory kanbanHistoryEdit = new KanbanHistory();
			kanbanHistoryEdit.setActionType(ActionType.Update);
			kanbanHistoryEdit.setDateCreated(new Date());
			kanbanHistoryEdit.setAuthor(utils.getLoggedInMember());
			kanbanHistoryEdit.setContainerId(kanbanIssueBeforeUpdate.getIssueId());
			kanbanHistoryEdit.setContainerType(ContainerType.KanbanIssue);
			
			
			List<KanbanHistoryFieldChange> historyFieldChanges = new ArrayList<KanbanHistoryFieldChange>();
			
			//is subject change
			if(!checkEqual(kanbanIssueBeforeUpdate.getSubject(),kanbanIssueAfterUpdate.getSubject())){
				KanbanHistoryFieldChange subjectFieldChange = new KanbanHistoryFieldChange();
				subjectFieldChange.setFieldName(KanbanHistoryFieldChange.KANBAN_ISSUE_SUBJECT);
				subjectFieldChange.setOldValue(kanbanIssueBeforeUpdate.getSubject());
				subjectFieldChange.setNewValue(kanbanIssueAfterUpdate.getSubject());
				subjectFieldChange.setKanbanHistory(kanbanHistoryEdit);
				historyFieldChanges.add(subjectFieldChange);
			}
			//is description change
			if(!checkEqual(kanbanIssueBeforeUpdate.getDescription(),kanbanIssueAfterUpdate.getDescription())){
				KanbanHistoryFieldChange descriptionFieldChange = new KanbanHistoryFieldChange();
				descriptionFieldChange.setFieldName(KanbanHistoryFieldChange.KANBAN_ISSUE_DESCRIPTION);
				descriptionFieldChange.setOldValue(kanbanIssueBeforeUpdate.getDescription());
				descriptionFieldChange.setNewValue(kanbanIssueAfterUpdate.getDescription());
				descriptionFieldChange.setKanbanHistory(kanbanHistoryEdit);
				historyFieldChanges.add(descriptionFieldChange);
			}
			
			//is note change
			if(!checkEqual(kanbanIssueBeforeUpdate.getNote(),kanbanIssueAfterUpdate.getNote())){
				KanbanHistoryFieldChange noteFieldChange = new KanbanHistoryFieldChange();
				noteFieldChange.setFieldName(KanbanHistoryFieldChange.KANBAN_ISSUE_NOTE);
				noteFieldChange.setOldValue(kanbanIssueBeforeUpdate.getNote());
				noteFieldChange.setNewValue(kanbanIssueAfterUpdate.getNote());
				noteFieldChange.setKanbanHistory(kanbanHistoryEdit);
				historyFieldChanges.add(noteFieldChange);
			}
			
			//is type change
			if(!checkEqual(kanbanIssueBeforeUpdate.getType(),kanbanIssueAfterUpdate.getType())){
				KanbanHistoryFieldChange typeFieldChange = new KanbanHistoryFieldChange();
				typeFieldChange.setFieldName(KanbanHistoryFieldChange.KANBAN_ISSUE_TYPE);
				typeFieldChange.setOldValue(kanbanIssueBeforeUpdate.getType());
				typeFieldChange.setNewValue(kanbanIssueAfterUpdate.getType());
				typeFieldChange.setKanbanHistory(kanbanHistoryEdit);
				historyFieldChanges.add(typeFieldChange);
			}
			
			//is priority change
			if(!checkEqual(kanbanIssueBeforeUpdate.getPriority(),kanbanIssueAfterUpdate.getPriority())){
				KanbanHistoryFieldChange priorityFieldChange = new KanbanHistoryFieldChange();
				priorityFieldChange.setFieldName(KanbanHistoryFieldChange.KANBAN_ISSUE_PRIORITY);
				priorityFieldChange.setOldValue(kanbanIssueBeforeUpdate.getPriority());
				priorityFieldChange.setNewValue(kanbanIssueAfterUpdate.getPriority());
				priorityFieldChange.setKanbanHistory(kanbanHistoryEdit);
				historyFieldChanges.add(priorityFieldChange);
			}
			
			//check estimate point
			if(kanbanIssueBeforeUpdate.getKanbanStatus() != null && kanbanIssueBeforeUpdate.getKanbanStatus().getType().equals(StatusType.START)) {
				if(!checkEqual(kanbanIssueBeforeUpdate.getEstimate(),kanbanIssueAfterUpdate.getEstimate())) {
					KanbanHistoryFieldChange estimateFieldChange = new KanbanHistoryFieldChange();
					estimateFieldChange.setFieldName(KanbanHistoryFieldChange.KANBAN_ISSUE_ESTIMATE);
					estimateFieldChange.setOldValue(kanbanIssueBeforeUpdate.getEstimate());
					estimateFieldChange.setNewValue(kanbanIssueAfterUpdate.getEstimate());
					estimateFieldChange.setKanbanHistory(kanbanHistoryEdit);
					historyFieldChanges.add(estimateFieldChange);
				}
			}
			
			//check update remain point
			if(kanbanIssueBeforeUpdate.getKanbanStatus() != null && !kanbanIssueBeforeUpdate.getKanbanStatus().getType().equals(StatusType.START)) {
				//is change remain point
				if(!checkEqual(kanbanIssueBeforeUpdate.getRemain(),kanbanIssueAfterUpdate.getRemain())) {
					KanbanHistoryFieldChange remainFieldChange = new KanbanHistoryFieldChange();
					remainFieldChange.setFieldName(KanbanHistoryFieldChange.KANBAN_ISSUE_REMAIN);
					remainFieldChange.setOldValue(kanbanIssueBeforeUpdate.getRemain());
					remainFieldChange.setNewValue(kanbanIssueAfterUpdate.getRemain());
					remainFieldChange.setKanbanHistory(kanbanHistoryEdit);
					historyFieldChanges.add(remainFieldChange);
				}
			}
			
			//status in product backlog to kanban
			if(kanbanIssueBeforeUpdate.getKanbanStatus() == null || kanbanIssueBeforeUpdate.getKanbanSwimline() == null) {
				if(kanbanIssueAfterUpdate.getKanbanStatus() != null && kanbanIssueAfterUpdate.getKanbanSwimline() != null) {
					KanbanHistoryFieldChange statusFieldChangeToKanban = new KanbanHistoryFieldChange();
					statusFieldChangeToKanban.setFieldName(KanbanHistoryFieldChange.KANBAN_ISSUE_STATUS);
					statusFieldChangeToKanban.setOldValue(KanbanHistoryFieldChange.KANBAN_ISSUE_PRODUCT_BACKLOG_COLUM);
					String newValue = kanbanIssueAfterUpdate.getKanbanStatus().getName() + " at Swimline " + kanbanIssueAfterUpdate.getKanbanSwimline().getName();
					statusFieldChangeToKanban.setNewValue(newValue);
					statusFieldChangeToKanban.setKanbanHistory(kanbanHistoryEdit);
					historyFieldChanges.add(statusFieldChangeToKanban);
				}
			} else {
				//move kanban issue to product backlog column
				if(kanbanIssueAfterUpdate.getKanbanStatus() == null && kanbanIssueAfterUpdate.getKanbanSwimline() == null) {
					KanbanHistoryFieldChange statusFieldChangeToProductBacklogColumn = new KanbanHistoryFieldChange();
					statusFieldChangeToProductBacklogColumn.setFieldName(KanbanHistoryFieldChange.KANBAN_ISSUE_STATUS);
					String oldValue = kanbanIssueBeforeUpdate.getKanbanStatus().getName() + " at Swimline " + kanbanIssueBeforeUpdate.getKanbanSwimline().getName();
					statusFieldChangeToProductBacklogColumn.setOldValue(oldValue);
					statusFieldChangeToProductBacklogColumn.setNewValue(KanbanHistoryFieldChange.KANBAN_ISSUE_PRODUCT_BACKLOG_COLUM);
					statusFieldChangeToProductBacklogColumn.setKanbanHistory(kanbanHistoryEdit);
					historyFieldChanges.add(statusFieldChangeToProductBacklogColumn);
				} else if(kanbanIssueBeforeUpdate.getKanbanStatus().getStatusId().compareTo(kanbanIssueAfterUpdate.getKanbanStatus().getStatusId()) != 0 
						|| kanbanIssueBeforeUpdate.getKanbanSwimline().getSwimlineId().compareTo(kanbanIssueAfterUpdate.getKanbanSwimline().getSwimlineId()) != 0) {
					//move around kanban
					KanbanHistoryFieldChange statusFieldChangeAroundKanban = new KanbanHistoryFieldChange();
					statusFieldChangeAroundKanban.setFieldName(KanbanHistoryFieldChange.KANBAN_ISSUE_STATUS);
					String oldValue = kanbanIssueBeforeUpdate.getKanbanStatus().getName() + " at Swimline " + kanbanIssueBeforeUpdate.getKanbanSwimline().getName();
					statusFieldChangeAroundKanban.setOldValue(oldValue);
					String newValue = kanbanIssueAfterUpdate.getKanbanStatus().getName() + " at Swimline " + kanbanIssueAfterUpdate.getKanbanSwimline().getName();
					statusFieldChangeAroundKanban.setNewValue(newValue);
					statusFieldChangeAroundKanban.setKanbanHistory(kanbanHistoryEdit);
					historyFieldChanges.add(statusFieldChangeAroundKanban);
				}
				
			}
			
			//is change assing
			List<Member> newAssignees = memberIssueService.getMembersByIssue(kanbanIssueAfterUpdate.getIssueId());
			if(oldAssignees != null && newAssignees != null && (!oldAssignees.containsAll(newAssignees) || !newAssignees.containsAll(oldAssignees))) {
				KanbanHistoryFieldChange memberFieldChange = new KanbanHistoryFieldChange();
				memberFieldChange.setFieldName(KanbanHistoryFieldChange.KANBAN_ISSUE_MEMBER_ISSUE);
				String allOldMember = "";
				for(Member oldMember : oldAssignees) {
					allOldMember = allOldMember + "["+oldMember.getFirstName() +" "+ oldMember.getLastName() +"]";
				}
				memberFieldChange.setOldValue(allOldMember);
				String allNewMember = "";
				for(Member oldMember : newAssignees) {
					allNewMember = allNewMember + "["+oldMember.getFirstName() +" "+ oldMember.getLastName() +"]";
				}
				memberFieldChange.setNewValue(allNewMember);
				memberFieldChange.setKanbanHistory(kanbanHistoryEdit);
				historyFieldChanges.add(memberFieldChange);
			}
			//change remove or assign member
			
			
			//save kanban history edit
			kanbanHistoryEdit.setKanbanHistoryFieldChanges(historyFieldChanges);
			if(!historyFieldChanges.isEmpty()){
				kanbanHistoryDao.save(kanbanHistoryEdit);
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("error historyForEditKanbanIssue");
		}
	}

	private boolean checkEqual(String strVal, String otherStrVal) {
		if(strVal == null && otherStrVal == null) {
			return true;
		} else if((strVal == null && otherStrVal != null) || (strVal != null && otherStrVal == null)) {
			return false;
		} else {
			return strVal.equals(otherStrVal);
		}
	}

	@Override
	public void historyForEstimateKanbanIssue(KanbanIssue kanbanIssueBeforeUpdate, KanbanIssue kanbanIssueAfterUpdate) {
		try {
			//add history
			KanbanHistory kanbanHistoryEdit = new KanbanHistory();
			kanbanHistoryEdit.setActionType(ActionType.Update);
			kanbanHistoryEdit.setDateCreated(new Date());
			kanbanHistoryEdit.setAuthor(utils.getLoggedInMember());
			kanbanHistoryEdit.setContainerId(kanbanIssueBeforeUpdate.getIssueId());
			kanbanHistoryEdit.setContainerType(ContainerType.KanbanIssue);
			
			List<KanbanHistoryFieldChange> historyFieldChanges = new ArrayList<KanbanHistoryFieldChange>();
			//is change estimate point
			if(!checkEqual(kanbanIssueBeforeUpdate.getEstimate(),kanbanIssueAfterUpdate.getEstimate())) {
				KanbanHistoryFieldChange estimateFieldChange = new KanbanHistoryFieldChange();
				estimateFieldChange.setFieldName(KanbanHistoryFieldChange.KANBAN_ISSUE_ESTIMATE);
				estimateFieldChange.setOldValue(kanbanIssueBeforeUpdate.getEstimate());
				estimateFieldChange.setNewValue(kanbanIssueAfterUpdate.getEstimate());
				estimateFieldChange.setKanbanHistory(kanbanHistoryEdit);
				historyFieldChanges.add(estimateFieldChange);
			}
			
			//save kanban history edit
			kanbanHistoryEdit.setKanbanHistoryFieldChanges(historyFieldChanges);
			kanbanHistoryDao.save(kanbanHistoryEdit);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("error historyForEstimateKanbanIssue ",e);
		}
		
		
	}

	@Override
	public void historyForRemainPointKanbanIssue(KanbanIssue kanbanIssueBeforeUpdate, KanbanIssue kanbanIssueAfterUpdate) {
		try {
			//add history
			KanbanHistory kanbanHistoryEdit = new KanbanHistory();
			kanbanHistoryEdit.setActionType(ActionType.Update);
			kanbanHistoryEdit.setDateCreated(new Date());
			kanbanHistoryEdit.setAuthor(utils.getLoggedInMember());
			kanbanHistoryEdit.setContainerId(kanbanIssueBeforeUpdate.getIssueId());
			kanbanHistoryEdit.setContainerType(ContainerType.KanbanIssue);
			
			List<KanbanHistoryFieldChange> historyFieldChanges = new ArrayList<KanbanHistoryFieldChange>();
			//is change estimate point
			if(!checkEqual(kanbanIssueBeforeUpdate.getRemain(),kanbanIssueAfterUpdate.getRemain())) {
				KanbanHistoryFieldChange remainFieldChange = new KanbanHistoryFieldChange();
				remainFieldChange.setFieldName(KanbanHistoryFieldChange.KANBAN_ISSUE_REMAIN);
				remainFieldChange.setOldValue(kanbanIssueBeforeUpdate.getRemain());
				remainFieldChange.setNewValue(kanbanIssueAfterUpdate.getRemain());
				remainFieldChange.setKanbanHistory(kanbanHistoryEdit);
				historyFieldChanges.add(remainFieldChange);
			}
			//save kanban history edit
			kanbanHistoryEdit.setKanbanHistoryFieldChanges(historyFieldChanges);
			kanbanHistoryDao.save(kanbanHistoryEdit);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("error historyForRemainPointKanbanIssue ",e);
		}
		
	}

	@Override
	public void historyForAssignMemberForKanbanIssue(Member member,
			KanbanIssue kanbanIssue) {
		try {
			//add history
			KanbanHistory kanbanHistoryEdit = new KanbanHistory();
			kanbanHistoryEdit.setActionType(ActionType.Update);
			kanbanHistoryEdit.setDateCreated(new Date());
			kanbanHistoryEdit.setAuthor(utils.getLoggedInMember());
			kanbanHistoryEdit.setContainerId(kanbanIssue.getIssueId());
			kanbanHistoryEdit.setContainerType(ContainerType.KanbanIssue);
			
			List<KanbanHistoryFieldChange> historyFieldChanges = new ArrayList<KanbanHistoryFieldChange>();
			KanbanHistoryFieldChange assignFieldChange = new KanbanHistoryFieldChange();
			assignFieldChange.setFieldName(KanbanHistoryFieldChange.KANBAN_ISSUE_ASSIGN);
			assignFieldChange.setOldValue("");
			assignFieldChange.setNewValue(member.getFirstName() + " " + member.getLastName());
			assignFieldChange.setKanbanHistory(kanbanHistoryEdit);
			historyFieldChanges.add(assignFieldChange);
			//save kanban history edit
			kanbanHistoryEdit.setKanbanHistoryFieldChanges(historyFieldChanges);
			kanbanHistoryDao.save(kanbanHistoryEdit);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("error historyForRemainPointKanbanIssue ",e);
		}
		
	}

	@Override
	public void historyForUnAssignMemberForKanbanIssue(Member member,
			KanbanIssue kanbanIssueById) {
		try {
			//add history
			KanbanHistory kanbanHistoryEdit = new KanbanHistory();
			kanbanHistoryEdit.setActionType(ActionType.Update);
			kanbanHistoryEdit.setDateCreated(new Date());
			kanbanHistoryEdit.setAuthor(utils.getLoggedInMember());
			kanbanHistoryEdit.setContainerId(kanbanIssueById.getIssueId());
			kanbanHistoryEdit.setContainerType(ContainerType.KanbanIssue);
			
			List<KanbanHistoryFieldChange> historyFieldChanges = new ArrayList<KanbanHistoryFieldChange>();
			KanbanHistoryFieldChange unAssignFieldChange = new KanbanHistoryFieldChange();
			unAssignFieldChange.setFieldName(KanbanHistoryFieldChange.KANBAN_ISSUE_UNASSIGN);
			unAssignFieldChange.setOldValue("");
			unAssignFieldChange.setNewValue(member.getFirstName() + " " + member.getLastName());
			unAssignFieldChange.setKanbanHistory(kanbanHistoryEdit);
			historyFieldChanges.add(unAssignFieldChange);
			//save kanban history edit
			kanbanHistoryEdit.setKanbanHistoryFieldChanges(historyFieldChanges);
			kanbanHistoryDao.save(kanbanHistoryEdit);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("error historyForUnAssignMemberForKanbanIssue ",e);
		}
	}

	@Override
	public List<KanbanHistory> getByKanbanIssue(KanbanIssue kanbanIssue) {
		return kanbanHistoryDao.getByKanbanIssue(kanbanIssue);
	}

	@Override
	public List<KanbanHistory> findByTeamAndFilter(long teamId, int start,
			int endrow, Date dateStart, Date dateEnd) {
		return kanbanHistoryDao.findByTeamAndFilter(teamId,start,endrow,dateStart,dateEnd);
	}

	@Override
	public List<Date> findDateRangeOfKanbanIssueHistoryByTeamId(long teamId) {
		return kanbanHistoryDao.findDateRangeOfKanbanIssueHistoryByTeamId(teamId);
	}

	@Override
	public void historyForAddAttachment(KanbanIssue kanbanIssue,
			Attachment newAttachment) {
		try {
			//add history
			KanbanHistory kanbanHistoryAddAttachment = new KanbanHistory();
			kanbanHistoryAddAttachment.setActionType(ActionType.Upfile);
			kanbanHistoryAddAttachment.setDateCreated(new Date());
			kanbanHistoryAddAttachment.setAuthor(utils.getLoggedInMember());
			kanbanHistoryAddAttachment.setContainerId(kanbanIssue.getIssueId());
			kanbanHistoryAddAttachment.setContainerType(ContainerType.KanbanIssue);
			
			List<KanbanHistoryFieldChange> historyFieldChanges = new ArrayList<KanbanHistoryFieldChange>();
			KanbanHistoryFieldChange upFileFieldChange = new KanbanHistoryFieldChange();
			upFileFieldChange.setFieldName(KanbanHistoryFieldChange.KANBAN_ISSUE_ATTACHMENT);
			upFileFieldChange.setOldValue("");
			upFileFieldChange.setNewValue(newAttachment.getFilename());
			upFileFieldChange.setKanbanHistory(kanbanHistoryAddAttachment);
			historyFieldChanges.add(upFileFieldChange);
			//save kanban history edit
			kanbanHistoryAddAttachment.setKanbanHistoryFieldChanges(historyFieldChanges);
			kanbanHistoryDao.save(kanbanHistoryAddAttachment);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("error historyForAddAttachment ",e);
		}
		
	}

	@Override
	public void historyForDeleteAttachment(KanbanIssue kanbanIssue,
			Attachment attachment) {
		try {
			//add history
			KanbanHistory kanbanHistoryDeleteAttachment = new KanbanHistory();
			kanbanHistoryDeleteAttachment.setActionType(ActionType.Deletefile);
			kanbanHistoryDeleteAttachment.setDateCreated(new Date());
			kanbanHistoryDeleteAttachment.setAuthor(utils.getLoggedInMember());
			kanbanHistoryDeleteAttachment.setContainerId(kanbanIssue.getIssueId());
			kanbanHistoryDeleteAttachment.setContainerType(ContainerType.KanbanIssue);
			
			List<KanbanHistoryFieldChange> historyFieldChanges = new ArrayList<KanbanHistoryFieldChange>();
			KanbanHistoryFieldChange deleteFileFieldChange = new KanbanHistoryFieldChange();
			deleteFileFieldChange.setFieldName(KanbanHistoryFieldChange.KANBAN_ISSUE_ATTACHMENT);
			deleteFileFieldChange.setOldValue("");
			deleteFileFieldChange.setNewValue(attachment.getFilename());
			deleteFileFieldChange.setKanbanHistory(kanbanHistoryDeleteAttachment);
			historyFieldChanges.add(deleteFileFieldChange);
			//save kanban history edit
			kanbanHistoryDeleteAttachment.setKanbanHistoryFieldChanges(historyFieldChanges);
			kanbanHistoryDao.save(kanbanHistoryDeleteAttachment);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("error historyForDeleteAttachment ",e);
		}
		
	}
}