package com.ant.myagile.service.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.context.FacesContext;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ant.myagile.dao.TopicDao;
import com.ant.myagile.dao.impl.TopicDaoImpl;
import com.ant.myagile.managedbean.WikiBean;
import com.ant.myagile.model.Attachment;
import com.ant.myagile.model.Topic;
import com.ant.myagile.model.Wiki;
import com.ant.myagile.service.AttachmentService;
import com.ant.myagile.service.ITopicService;

@Service
public class TopicServiceImpl implements ITopicService , Serializable{
	private static final Logger LOGGER = Logger
			.getLogger(TopicDaoImpl.class);
	private static final long serialVersionUID = 1L;
	@Autowired
	private TopicDao topicDao;
	@Autowired
	AttachmentService attachmentService;
	@Override
	public Topic getTopicById(long id) {
		Topic topic = topicDao.getTopicById(id);
		return topic;
	}

	@Override
	public void create(Topic topic) {
		topicDao.add(topic);
	}

	@Override
	public void edit(Topic topic) {
		topicDao.update(topic);
	}

	@Override
	public Topic getTopicByTitle(String title,long wikiId) {
		return this.topicDao.getTopicByName(title, wikiId);
	}

	@Override
	public Boolean delete(Topic topic) {
		
		try{
			// get listAttachments base on topic
			List<Attachment> listAttachments = new ArrayList<Attachment>();;
			if (attachmentService.findAttachmentByTopic(topic) != null) {
				listAttachments = attachmentService
						.findAttachmentByTopic(topic);
			}
			
			// while listAttachments have item --> delete it
			while(listAttachments!=null && listAttachments.size()!=0) {
				Attachment deleteAttachment = listAttachments.get(0);
				// Get WikiId
				FacesContext context = FacesContext.getCurrentInstance();
				WikiBean wikiBean = getWikiBean(context);
				long wikiId = wikiBean.getWiki().getWikiId();
				if (this.attachmentService.delete(deleteAttachment)) {
					this.attachmentService.deleteFileOfTopic(
							deleteAttachment.getDiskFilename(), wikiId,
							topic.getTopicId());
				}
				listAttachments.remove(deleteAttachment);
			}
		}catch(Exception e){
			LOGGER.debug(e.getMessage());
			return false;
		}
		// delete topic
		return this.topicDao.delete(topic);
	}
	

	private WikiBean getWikiBean(FacesContext context) {
		WikiBean wikiBean = (WikiBean) context
				.getApplication()
				.getExpressionFactory()
				.createValueExpression(context.getELContext(), "#{wikiBean}",
						WikiBean.class).getValue(context.getELContext());
		return wikiBean;
	}

	@Override
	public boolean isDuplicateTitle(String title, Topic topic) {
		Topic topicTemp =  this.getTopicByTitle(title, topic.getWiki().getWikiId());
		if (topicTemp != null && topic.getTopicId() != topicTemp.getTopicId())
			return true;
		return false;
	}

	@Override
	public List<Topic> getListTopicByWiki(Wiki wiki) {
		
		return topicDao.getListTopicByWiki(wiki);
	}

	@Override
	public void deleteAllTopicInWiki(Wiki wiki) {
		List<Topic> topics = topicDao.getListTopicByWiki(wiki);
		if(topics.size() > 0){
			for (Topic topic : topics) {
				topicDao.delete(topic);
			}
		}
		
	}
}
