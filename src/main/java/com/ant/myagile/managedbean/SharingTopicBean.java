package com.ant.myagile.managedbean;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.context.Flash;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.ant.myagile.model.Attachment;
import com.ant.myagile.model.Topic;
import com.ant.myagile.model.Wiki;
import com.ant.myagile.service.AttachmentService;
import com.ant.myagile.service.ITopicService;
import com.ant.myagile.service.WikiService;
import com.ant.myagile.utils.JSFUtils;
import com.ant.myagile.utils.Utils;

@Component("sharingTopicBean")
@Scope("session")
public class SharingTopicBean implements Serializable {
	private static final Logger LOGGER = Logger
			.getLogger(SharingTopicBean.class);

	@Autowired
	ITopicService topicService;
	@Autowired
	Utils utils;
	@Autowired
	AttachmentService attachmentService;
	@Autowired
	WikiService wikiService;

	private Attachment attachment;
	private Attachment selectedAttachment;
	private List<Attachment> listAttachments;
	private static final long serialVersionUID = 1L;

	private long idCurrentTopic = 0L;
	private Topic currentTopic;
	private String title, content, nameOfLastModifyMember;
	private Wiki currentWiki;
	public SharingTopicBean() {
		this.currentTopic = new Topic();
		resetFields();
	}

	private void resetFields() {
		this.title = "";
		this.content = "";
	}

	private void initFields() {
		this.title = this.currentTopic.getTitle();
		if (this.title == null) {
			this.title = "Please enter title...";
		}
		this.content = this.currentTopic.getContent();
		if (this.content == null) {
			this.content = "";
		}
		this.nameOfLastModifyMember = this.currentTopic.getLastModifyMember().getLastName()
				+ " "
				+ this.currentTopic.getLastModifyMember().getFirstName();
		listAttachments = new ArrayList<Attachment>();
		if (attachmentService.findAttachmentByTopic(this.currentTopic) != null) {
			listAttachments = attachmentService
					.findAttachmentByTopic(this.currentTopic);
		}
		// Get WikiId
		FacesContext context = FacesContext.getCurrentInstance();
		WikiBean wikiBean = getWikiBean(context);
		currentWiki = wikiBean.getWiki();
	}

	public void redirectTopicEdit() {
		FacesContext context = FacesContext.getCurrentInstance();
		Map<String, String> reqParams = FacesContext.getCurrentInstance()
				.getExternalContext().getRequestParameterMap();
		try {
			long idTopic = Long.parseLong(reqParams.get("idTopic"));
			this.currentTopic = this.topicService.getTopicById(idTopic);
			String contextPath = "";
			if (this.currentTopic != null) { // the topic's existed
				contextPath = context.getExternalContext().getRequestContextPath()
						+ "/topic/detail";
			} else { // create new topic
				WikiBean wikiBean = getWikiBean(context);
				Topic topic = new Topic();
				topic.setTitle(reqParams.get("titleTopic"));
				topic.setContent("");
				topic.setLastModifyDate(new Date());
				topic.setLastModifyMember(this.utils.getLoggedInMember());
				topic.setWiki(wikiBean.getWiki());
				this.topicService.create(topic);
				this.currentTopic = topic;
				contextPath = FacesContext.getCurrentInstance()
						.getExternalContext().getRequestContextPath()
						+ "/topic/edit";
			}
			initFields();
		
			FacesContext.getCurrentInstance().getExternalContext()
					.redirect(contextPath);
		} catch (IOException e) {
			LOGGER.error(e);
		}
	}

	public String edit() {
		initFields();
		return "pretty:topicEditting";
	}

	public String save() {
		FacesContext context = FacesContext.getCurrentInstance();
		try {
			String title = this.title.trim();
			
			// update title in wiki content
			if (!title.equals(this.currentTopic.getTitle())) {
				//check duplicate title
				if(topicService.isDuplicateTitle(title, this.currentTopic)){
					context.addMessage(
							null,
							new FacesMessage(FacesMessage.SEVERITY_ERROR,"Fail",
									"The topic title is existed!"));
					return null;
				}
				WikiBean wikiBean = getWikiBean(context);
				this.wikiService.changeTitle(wikiBean.getWiki(),
						this.currentTopic, title);
			}
			this.currentTopic.setTitle(this.title.trim());
			this.currentTopic.setContent(this.content);
			this.currentTopic.setLastModifyDate(new Date());
			this.currentTopic.setLastModifyMember(this.utils.getLoggedInMember());
			if (this.currentTopic.getTopicId() > 0) {
				this.topicService.edit(this.currentTopic);
				initFields();
			} else {
				this.topicService.create(this.currentTopic);
				resetFields();
			}
			
			context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Success",
					"Save successfully!"));
			return "pretty:topicDetail";
		} catch (Exception e) {
			context.addMessage(
					null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR,"Fail",
							"Save unsuccessfully"));
			return null;
		}
	}
	
	public String cancel(){
		initFields();
		return "pretty:topicDetail";
	}
	public String detailById(long id) {
		this.currentTopic = this.topicService.getTopicById(id);
		initFields();
		return "pretty:topicDetail";
	}

	public String delete() {
		FacesContext context = FacesContext.getCurrentInstance();
		boolean isSuccessful= this.topicService.delete(this.currentTopic);
		FacesMessage message = new FacesMessage();
		if(isSuccessful){
			message.setSeverity(FacesMessage.SEVERITY_INFO);
			message.setDetail("Delete successfully!");
		} else{
			message.setSeverity(FacesMessage.SEVERITY_ERROR);
			message.setDetail("Can not delete topic!");
		}
		Flash flash = context.getExternalContext().getFlash();
		flash.setKeepMessages(true);
		flash.setRedirect(true);
		context.addMessage(null, message);
		
		return "pretty:wiki";
	}
	

	/**
	 * Attach file to topic Firstly, upload file to server then move file to
	 * attachments folder of wiki Path in server:
	 * /opt/myagile/data/Wiki/W<wikiId>/T<topicId>/attachment/
	 */
	public void uploadFile() {
		// Get WikiId
		FacesContext context = FacesContext.getCurrentInstance();
		WikiBean wikiBean = getWikiBean(context);
		long wikiId = wikiBean.getWiki().getWikiId();
		// Create new attachment from uploaded file
		String filename = JSFUtils.getRequestParameter("filename");
		String diskFileName = this.attachmentService
				.fileNameProcess(FilenameUtils.removeExtension(filename));
		diskFileName = this.attachmentService.replaceFile(filename,
				diskFileName);
		this.attachment = new Attachment();
		this.attachment.setFilename(filename);
		this.attachment.setDiskFilename(diskFileName);
		this.attachment.setContainerId((long) this.currentTopic.getTopicId());
		this.attachment.setContainerType(Attachment.TOPIC_ATTACHMENT);
		this.attachment.setTemp(false);
		this.attachment.setCreatedOn(new Date());
		this.attachment.setAuthor(this.utils.getLoggedInMember());
		// Save to attachment
		this.attachmentService.save(this.attachment);
		Attachment newAttachment = this.attachmentService
				.findAttachmentById(this.attachment.getAttachmentId());
		if (this.listAttachments == null) {
			this.listAttachments = new ArrayList<Attachment>();
		}
		if (newAttachment.getContainerId() != null) {
			this.attachment.setTemp(false);
			this.attachmentService.moveAttachmentFile(this.attachment, wikiId,
					(long) this.currentTopic.getTopicId());
			this.listAttachments.add(newAttachment);
		} else {
			this.attachment.setTemp(true);
			this.listAttachments.add(newAttachment);
		}
	}

	/**
	 * Delete attachment of topic
	 */
	public void deleteAttachmentOfTopic() {
		Attachment deleteAttachment = this.selectedAttachment;
		// Get WikiId
		FacesContext context = FacesContext.getCurrentInstance();
		WikiBean wikiBean = getWikiBean(context);
		long wikiId = wikiBean.getWiki().getWikiId();
		if (this.attachmentService.delete(deleteAttachment)) {
			this.attachmentService.deleteFileOfTopic(
					deleteAttachment.getDiskFilename(), wikiId,
					this.currentTopic.getTopicId());
		}
		this.listAttachments.remove(deleteAttachment);
	}

	private WikiBean getWikiBean(FacesContext context) {
		WikiBean wikiBean = (WikiBean) context
				.getApplication()
				.getExpressionFactory()
				.createValueExpression(context.getELContext(), "#{wikiBean}",
						WikiBean.class).getValue(context.getELContext());
		return wikiBean;
	}

	/*** GETTER & SETTER **/
	public Topic getCurrentTopic() {
		return currentTopic;
	}

	public void setCurrentTopic(Topic currentTopic) {
		this.currentTopic = currentTopic;
	}

	public Wiki getCurrentWiki() {
		return currentWiki;
	}

	public void setCurrentWiki(Wiki currentWiki) {
		this.currentWiki = currentWiki;
	}

	// work around to handle html
	public String getTitle() {
		String title = this.title;
		title = title.replaceAll("&lt;", "&amp;lt;").replaceAll("&gt;",
				"&amp;gt;");
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	// work around to handle html
	public String getContent() {
		String content = this.content != null ? this.content : "";
		content = content.replaceAll("&lt;", "&amp;lt;").replaceAll("&gt;",
				"&amp;gt;");
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getTitlePreview() {
		return this.title;
	}

	public String getContentPreview() {
		String content = Utils.createTopicLinkFromContent(this.content, this.currentTopic.getWiki().getWikiId(), topicService);
		return content;
	}

	public String getNameOfLastModifyMember() {
		return nameOfLastModifyMember;
	}

	public void setNameOfLastModifyMember(String nameOfLastModifyMember) {
		this.nameOfLastModifyMember = nameOfLastModifyMember;
	}

	public Attachment getSelectedAttachment() {
		return selectedAttachment;
	}

	public void setSelectedAttachment(Attachment selectedAttachment) {
		this.selectedAttachment = selectedAttachment;
	}

	public List<Attachment> getListAttachments() {
		return listAttachments;
	}

	public void setListAttachments(List<Attachment> listAttachments) {
		this.listAttachments = listAttachments;
	}

	public long getIdCurrentTopic() {
		return idCurrentTopic;
	}

	public void setIdCurrentTopic(long idCurrentTopic) {
		this.idCurrentTopic = idCurrentTopic;
	}

}
