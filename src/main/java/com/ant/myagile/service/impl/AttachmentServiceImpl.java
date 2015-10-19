package com.ant.myagile.service.impl;

import java.io.File;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ant.myagile.dao.AttachmentDao;
import com.ant.myagile.model.Attachment;
import com.ant.myagile.model.Issue;
import com.ant.myagile.model.KanbanIssue;
import com.ant.myagile.model.Topic;
import com.ant.myagile.model.UserStory;
import com.ant.myagile.model.Wiki;
import com.ant.myagile.service.AttachmentService;
import com.ant.myagile.service.HistoryService;
import com.ant.myagile.service.ITopicService;
import com.ant.myagile.service.IssueService;
import com.ant.myagile.service.UserStoryService;
import com.ant.myagile.utils.MyAgileFileUtils;
import com.ant.myagile.utils.Utils;

@Service
@Transactional
public class AttachmentServiceImpl implements AttachmentService {

	private static final Logger logger = Logger.getLogger(AttachmentServiceImpl.class);
	
	@Autowired
	private AttachmentDao attachmentDao;

	@Autowired
	private HistoryService historyService;

	@Autowired
	private ITopicService topicService;
	
	@Autowired
	private IssueService issueService;
	
	@Autowired
	private Utils utils;
	
	@Autowired
	private UserStoryService userStoryService;

	private static final String ROOT_FOLDER_PATH = MyAgileFileUtils
			.getRootStorageLocation();
	private static final String TEMP_FOLDER_PATH = MyAgileFileUtils
			.getStorageLocation("myagile.upload.temp.location");
	private static final String ATTACHMENT_FOLDER = "/attachment";

	@Override
	public String fileNameProcess(String fileName) {
		return MyAgileFileUtils.fileNameProcess(fileName);
	}

	@Override
	public void deleteFile(String filename, Long projectId) {
		MyAgileFileUtils.deleteFile(ROOT_FOLDER_PATH + "/projects/P"
				+ projectId + "/" + ATTACHMENT_FOLDER + "/" + filename);
	}
	
	/* 
	 * Delete attachment of Topic
	 */
	@Override
	public void deleteFileOfTopic(String filename, Long wikiId, Long topicId) {
		MyAgileFileUtils.deleteFile(ROOT_FOLDER_PATH + "/wiki/W" + wikiId + "/T" + topicId
				+ "/" + ATTACHMENT_FOLDER + "/" + filename);
	}
	/* 
	 * Delete all attachments of Topic
	 */
	@Override
	public void deleteAllFileOfTopic(Long wikiId, Long topicId){
		
	}
	@Override
	public void deleteFileInTemp(String filename) {
		MyAgileFileUtils.deleteFile(TEMP_FOLDER_PATH + ATTACHMENT_FOLDER + "/"
				+ filename);
	}

	@Override
	public String replaceFile(String oldFileName, String newFileName) {
		return MyAgileFileUtils.renameFile(
				TEMP_FOLDER_PATH + ATTACHMENT_FOLDER, oldFileName, newFileName);
	}

	@Override
	public boolean moveAttachmentFile(Attachment attachment, Long projectId) {
		String filePath = TEMP_FOLDER_PATH + ATTACHMENT_FOLDER + "/"
				+ attachment.getDiskFilename();
		File oldFile = new File(filePath);
		boolean result = false;
		String newFilePath = "";
		if (attachment.getContainerType().equalsIgnoreCase(
				Attachment.WIKI_ATTACHMENT)) {
			newFilePath = ROOT_FOLDER_PATH + "/wiki/W" + projectId + "/"
					+ ATTACHMENT_FOLDER;
		} else if (attachment.getContainerType().equalsIgnoreCase(
				Attachment.TOPIC_ATTACHMENT)) {
			long wikiId = topicService.getTopicById(projectId).getWiki()
					.getWikiId();
			newFilePath = ROOT_FOLDER_PATH + "/wiki/W" + wikiId + "/T"
					+ projectId + "/" + ATTACHMENT_FOLDER;
		} else if (attachment.getContainerType().equalsIgnoreCase(Attachment.ISSUE_ATTACHMENT) ||
					attachment.getContainerType().equalsIgnoreCase(Attachment.USERSTORY_ATTACHMENT) || attachment.getContainerType().equalsIgnoreCase(Attachment.KANBAN_ISSUE_ATTACHMENT)) {
			newFilePath = ROOT_FOLDER_PATH + "/projects/P" + projectId + "/"
					+ ATTACHMENT_FOLDER;
		}

		if (oldFile.exists()) {
			File newFile = new File(newFilePath);
			if (!newFile.exists()) {
				newFile.mkdirs();
			}
			result = oldFile.renameTo(new File(newFilePath + "/"
					+ attachment.getDiskFilename()));
			this.attachmentDao.update(attachment);
		}
		return result;
	}

	@Override
	public boolean moveAttachmentFile(Attachment attachment, Long wikiId,
			Long topicId) {
		String filePath = TEMP_FOLDER_PATH + ATTACHMENT_FOLDER + "/"
				+ attachment.getDiskFilename();
		File oldFile = new File(filePath);
		boolean result = false;
		String newFilePath = "";
		newFilePath = ROOT_FOLDER_PATH + "/wiki/W" + wikiId + "/T" + topicId
				+ "/" + ATTACHMENT_FOLDER;
		if (oldFile.exists()) {
			File newFile = new File(newFilePath);
			if (!newFile.exists()) {
				newFile.mkdirs();
			}
			result = oldFile.renameTo(new File(newFilePath + "/"
					+ attachment.getDiskFilename()));
			this.attachmentDao.update(attachment);
		}
		return result;
	}

	@Override
	public boolean save(Attachment attachment) {
		if (attachment.getContainerId() != null
				&& attachment.getContainerType().equals("Issue")) {
			historyService.saveHistoryForAttachment(attachment, "ISSUE");
		} else if (attachment.getContainerId() != null
				&& attachment.getContainerType().equals("Userstory")) {
			historyService.saveHistoryForAttachment(attachment, "BACKLOG");
		}
		return this.attachmentDao.save(attachment);
	}

	@Override
	public List<Attachment> findAll() {
		return this.attachmentDao.findAll();
	}

	@Override
	public List<Attachment> findAttachmentNotAdd() {
		return this.attachmentDao.findAttachmentNotAdd();
	}

	@Override
	public List<Attachment> findAttachmentNotAddByIssue(Issue issue) {
		return this.attachmentDao.findAttachmentNotAddByIssue(issue);
	}

	@Override
	public boolean update(Attachment attachment) {
		return this.attachmentDao.update(attachment);
	}

	@Override
	public boolean delete(Attachment attachment) {
		if (attachment.getContainerId() != null
				&& attachment.getContainerType().equals("Issue")) {
			historyService.saveHistoryForDeleteAttachment(attachment, "ISSUE");
		} else if (attachment.getContainerId() != null
				&& attachment.getContainerType().equals("Userstory")) {
			historyService
					.saveHistoryForDeleteAttachment(attachment, "BACKLOG");
		}
		return this.attachmentDao.delete(attachment);
	}

	@Override
	public List<Attachment> findAttachmentByIssue(Issue issue) {
		return this.attachmentDao.findAttachmentByIssue(issue);
	}

	@Override
	public Attachment findAttachmentById(long attachmentId) {
		return this.attachmentDao.findAttachmentById(attachmentId);
	}

	@Override
	public List<Attachment> findAttachmentByUserStory(UserStory userStory) {
		return attachmentDao.findAttachmentByUserStory(userStory);
	}

	@Override
	public List<Attachment> findAttachmentNotAddedByContainerType(
			String containerType) {
		return attachmentDao
				.findAttachmentNotAddedByContainerType(containerType);
	}

	@Override
	public List<Attachment> findAttachmentByWiki(Wiki wiki) {
		return attachmentDao.findAttachmentByWiki(wiki);
	}

	@Override
	public void deleteFileInWiki(String filename, Long wikiId) {
		MyAgileFileUtils.deleteFile(ROOT_FOLDER_PATH + "/wiki/W" + wikiId + "/"
				+ ATTACHMENT_FOLDER + "/" + filename);
	}
	
	@Override
	public List<Attachment> findAttachmentByTopic(Topic topic) {
		return attachmentDao.findAttachmentByTopic(topic);
	}

	@Override
	public void deleteAttachmentIssueByAttachment(Attachment deleteAttachment) {
		if(deleteAttachment.getContainerType().equals(Attachment.USERSTORY_ATTACHMENT)){
			UserStory userStoryOfAttachment = userStoryService.findUserStoryById(deleteAttachment.getContainerId());
			List<Issue> issueOfUserstory = issueService.findIssuesByUserStory(deleteAttachment.getContainerId());
			for(Issue issueItem : issueOfUserstory){
				List<Attachment> attachmentsOfIssue = findAttachmentByIssue(issueItem);
				if(attachmentsOfIssue != null){
					for(Attachment attachmentDelete : attachmentsOfIssue){
						if(deleteAttachment.getDiskFilename().equals(attachmentDelete.getDiskFilename())){
							delete(attachmentDelete);
							deleteFile(attachmentDelete.getDiskFilename(), userStoryOfAttachment.getProject().getProjectId());
						}
					}
				}
			}
		}
	}

	@Override
	public void addAttachmentToIssue(Attachment newAttachment) {
		if(newAttachment.getContainerType().equals(Attachment.USERSTORY_ATTACHMENT)){
			List<Issue> issueOfUserstory = issueService.findIssuesByUserStory(newAttachment.getContainerId());
			if(issueOfUserstory != null){
				for(Issue issueItem : issueOfUserstory){
					// copy the attachments from user story to issue
					Attachment attachment = new Attachment();
					attachment.setFilename(newAttachment.getFilename());
					attachment.setDiskFilename(newAttachment.getDiskFilename());
					attachment.setContainerId(issueItem.getIssueId());
					attachment.setContainerType(Attachment.ISSUE_ATTACHMENT);
					attachment.setTemp(false);
					attachment.setCreatedOn(new Date());
					attachment.setAuthor(this.utils.getLoggedInMember());
					attachmentDao.save(attachment);
				}
			}
		}
	}

	@Override
	public void addAttachmentForUserStoryFromIssueAttachment(
			Attachment attachmentOfIssue) {
		if(attachmentOfIssue.getContainerType().equals(Attachment.ISSUE_ATTACHMENT)){
			Issue issueOfAttachment = issueService.findIssueById(attachmentOfIssue.getContainerId());
			UserStory userStoryOfIssue = issueOfAttachment.getUserStory();
			if(userStoryOfIssue != null){
				//add attach for userstory
				// copy the attachments from user story to issue
				Attachment attachmentForUserStory = new Attachment();
				attachmentForUserStory.setFilename(attachmentOfIssue.getFilename());
				attachmentForUserStory.setDiskFilename(attachmentOfIssue.getDiskFilename());
				attachmentForUserStory.setContainerId(userStoryOfIssue.getUserStoryId());
				attachmentForUserStory.setContainerType(Attachment.USERSTORY_ATTACHMENT);
				attachmentForUserStory.setTemp(false);
				attachmentForUserStory.setCreatedOn(new Date());
				attachmentForUserStory.setAuthor(this.utils.getLoggedInMember());
				attachmentDao.save(attachmentForUserStory);
				logger.warn("add attachment for userstory from issue success");
			}
		}
	}

	@Override
	public void addAttachmentForOtherIssueFromCurrentIssueAttachment(
			Attachment attachmentOfIssue) {
		if(attachmentOfIssue.getContainerType().equals(Attachment.ISSUE_ATTACHMENT)){
			Issue issueOfAttachment = issueService.findIssueById(attachmentOfIssue.getContainerId());
			UserStory userStoryOfIssue = issueOfAttachment.getUserStory();
			List<Issue> issueOfUserStory = issueService.findIssuesByUserStory(userStoryOfIssue.getUserStoryId());
			if(issueOfUserStory != null){
				for(Issue issueItem : issueOfUserStory){
					if(issueItem.getIssueId().compareTo(attachmentOfIssue.getContainerId()) != 0){
						//add attach for other issue
						Attachment attachmentForIssue = new Attachment();
						attachmentForIssue.setFilename(attachmentOfIssue.getFilename());
						attachmentForIssue.setDiskFilename(attachmentOfIssue.getDiskFilename());
						attachmentForIssue.setContainerId(issueItem.getIssueId());
						attachmentForIssue.setContainerType(Attachment.ISSUE_ATTACHMENT);
						attachmentForIssue.setTemp(false);
						attachmentForIssue.setCreatedOn(new Date());
						attachmentForIssue.setAuthor(this.utils.getLoggedInMember());
						attachmentDao.save(attachmentForIssue);
						logger.warn("add attachment for other issue from current issue success");
					}
				}
			}
		}
	}

	@Override
	public void deleteAttachmentUserStoryFromAttachmentIssue(
			Attachment deleteAttachmentOfIssue) {
		if(deleteAttachmentOfIssue.getContainerType().equals(Attachment.ISSUE_ATTACHMENT)){
			Issue issueOfAttachment = issueService.findIssueById(deleteAttachmentOfIssue.getContainerId());
			UserStory userStoryOfIssue = issueOfAttachment.getUserStory();
			List<Attachment> attachmentOfUserStory = attachmentDao.findAttachmentByUserStory(userStoryOfIssue);
			for(Attachment attachmentItem : attachmentOfUserStory){
				if(attachmentItem.getDiskFilename().equals(deleteAttachmentOfIssue.getDiskFilename())){
					delete(attachmentItem);
					deleteFile(attachmentItem.getDiskFilename(), userStoryOfIssue.getProject().getProjectId());
					logger.warn("delete attachment userstory from issue attachment");
				}
			}
		}
	}

	@Override
	public void deleteAttachmentOtherIssueFromAttachmentCurrentIssue(
			Attachment deleteAttachmentOfIssue) {
		if(deleteAttachmentOfIssue.getContainerType().equals(Attachment.ISSUE_ATTACHMENT)){
			//get issue from attachment
			Issue issueOfAttachment = issueService.findIssueById(deleteAttachmentOfIssue.getContainerId());
			//get userstory from issue
			UserStory userStoryOfIssue = issueOfAttachment.getUserStory();
			//get list issue from userstory
			List<Issue> issueOfUserStory = issueService.findIssuesByUserStory(userStoryOfIssue.getUserStoryId());
			for(Issue issueItem : issueOfUserStory){
				//check issue different current issue
				if(issueItem.getIssueId() != deleteAttachmentOfIssue.getContainerId()){
					//get list attachment from issue
					List<Attachment> attachmentOfUserStory = attachmentDao.findAttachmentByIssue(issueItem);
					for(Attachment attachmentItem : attachmentOfUserStory){
						if(attachmentItem.getDiskFilename().equals(deleteAttachmentOfIssue.getDiskFilename())){
							delete(attachmentItem);
							deleteFile(attachmentItem.getDiskFilename(), userStoryOfIssue.getProject().getProjectId());
							logger.warn("delete attachment other issue from current issue attachment");
						}
					}
				}
			}
			
		}
	}

	@Override
	public List<Attachment> findAttachmentByKanbanIssue(KanbanIssue kanbanIssue) {
		return this.attachmentDao.findAttachmentByKanbanIssue(kanbanIssue);
	}

	@Override
	public Attachment findAttachmentByIssueAnDiskFileName(Issue issue,
			String diskFileName) {
		return attachmentDao.findAttachmentByIssueAnDiskFileName(issue, diskFileName);
	}

}
