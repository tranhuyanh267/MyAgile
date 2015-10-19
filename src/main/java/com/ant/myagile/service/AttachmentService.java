package com.ant.myagile.service;

import java.util.List;

import com.ant.myagile.model.Attachment;
import com.ant.myagile.model.Issue;
import com.ant.myagile.model.KanbanIssue;
import com.ant.myagile.model.Topic;
import com.ant.myagile.model.UserStory;
import com.ant.myagile.model.Wiki;

public interface AttachmentService {
	boolean save(Attachment attachment);

	boolean update(Attachment attachment);

	boolean delete(Attachment attachment);

	void deleteFileInTemp(String filename);

	String fileNameProcess(String fileName);

	void deleteFile(String filename, Long projectId);

	void deleteFileOfTopic(String filename, Long wikiId, Long topicId);

	void deleteAllFileOfTopic(Long wikiId, Long topicId);

	void deleteFileInWiki(String filename, Long wikiId);

	Attachment findAttachmentById(long attachmentId);

	String replaceFile(String oldFileName, String newFileName);

	boolean moveAttachmentFile(Attachment attachment, Long projectId);

	boolean moveAttachmentFile(Attachment attachment, Long wikiId, Long topicId);

	List<Attachment> findAll();

	List<Attachment> findAttachmentNotAdd();

	List<Attachment> findAttachmentNotAddByIssue(Issue issue);

	List<Attachment> findAttachmentByIssue(Issue issue);

	List<Attachment> findAttachmentByWiki(Wiki wiki);

	List<Attachment> findAttachmentByUserStory(UserStory userStory);

	List<Attachment> findAttachmentNotAddedByContainerType(String containerType);

	List<Attachment> findAttachmentByTopic(Topic topic);

	void deleteAttachmentIssueByAttachment(Attachment deleteAttachment);

	void addAttachmentToIssue(Attachment newAttachment);

	void addAttachmentForUserStoryFromIssueAttachment(Attachment attachment);

	void addAttachmentForOtherIssueFromCurrentIssueAttachment(
			Attachment attachment);

	void deleteAttachmentUserStoryFromAttachmentIssue(
			Attachment deleteAttachment);

	void deleteAttachmentOtherIssueFromAttachmentCurrentIssue(
			Attachment deleteAttachment);

	List<Attachment> findAttachmentByKanbanIssue(KanbanIssue kanbanIssue);

	Attachment findAttachmentByIssueAnDiskFileName(Issue issue,
			String diskFileName);

}
