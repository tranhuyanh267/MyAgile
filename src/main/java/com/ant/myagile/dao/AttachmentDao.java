package com.ant.myagile.dao;

import java.util.List;

import com.ant.myagile.model.Attachment;
import com.ant.myagile.model.Issue;
import com.ant.myagile.model.KanbanIssue;
import com.ant.myagile.model.Topic;
import com.ant.myagile.model.UserStory;
import com.ant.myagile.model.Wiki;

public interface AttachmentDao {
    boolean save(Attachment attachment);

    boolean update(Attachment attachment);

    boolean delete(Attachment attachment);

    Attachment findAttachmentById(long attachmentId);

    List<Attachment> findAll();

    List<Attachment> findAttachmentNotAdd();

    List<Attachment> findAttachmentNotAddByIssue(Issue issue);

    List<Attachment> findAttachmentByIssue(Issue issue);
    
    List<Attachment> findAttachmentByWiki(Wiki wiki);

    List<Attachment> findAttachmentByUserStory(UserStory userStory);

    List<Attachment> findAttachmentNotAddedByContainerType(String containerType);

	List<Attachment> findAttachmentByTopic(Topic topic);

	List<Attachment> findAttachmentByKanbanIssue(KanbanIssue kanbanIssue);

	Attachment findAttachmentByIssueAnDiskFileName(Issue issue,
			String diskFileName);
}
