package com.ant.myagile.managedbean;

import java.util.ArrayList;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FilenameUtils;
import org.primefaces.context.RequestContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.ant.myagile.dto.FeedbackExpressDTO;
import com.ant.myagile.dto.NameFileTempDTO;
import com.ant.myagile.service.AttachmentService;
import com.ant.myagile.service.MailService;
import com.ant.myagile.utils.JSFUtils;

@Component("feedbackExpressBean")
@Scope("session")
public class FeedbacExpressBean {

	@Autowired
	private AttachmentService attachmentService;
	@Autowired
	private MailService mailService;
	
	private FeedbackExpressDTO feedbackData = new FeedbackExpressDTO();
	private String diskFileNameNeedToDelete = "";
	
	
	public void resetFeedback() {
		JSFUtils.resetForm("frm-add-feedback");
		feedbackData.setInputCorrectCaptcha(null);
		feedbackData.setSubject("");
		feedbackData.setDescription("");
		feedbackData.setCaptchaAnswer("");
		for(NameFileTempDTO nameFile : feedbackData.getFileTempDTOs()) {
			attachmentService.deleteFileInTemp(nameFile.getDiskFilename());
			feedbackData.getFileTempDTOs().remove(nameFile);
		}
	}
	
	public void addFeedback() {
		if (!feedbackData.getCaptcha().equals(feedbackData.getCaptchaAnswer())) {
			feedbackData.setInputCorrectCaptcha(false);
		} else {
			if(mailService.sendMailFeedbackToAdmin(feedbackData)) {
				RequestContext.getCurrentInstance().execute("showmessage()");
				resetFeedback();
			}
		}
	}
	
	public void deleteAttachment() {
		attachmentService.deleteFileInTemp(diskFileNameNeedToDelete);
		for(NameFileTempDTO nameFile : feedbackData.getFileTempDTOs()) {
			if(diskFileNameNeedToDelete.equals(nameFile.getDiskFilename())) {
				feedbackData.getFileTempDTOs().remove(nameFile);
			}
		}
	}

	public void uploadFile() {
		String fileName = JSFUtils.getRequestParameter("filename");
		String diskFileName = attachmentService.fileNameProcess(FilenameUtils
				.removeExtension(fileName));
		diskFileName = this.attachmentService.replaceFile(fileName,
				diskFileName);
		// add list file
		NameFileTempDTO nameFileUploaded = new NameFileTempDTO();
		nameFileUploaded.setFilename(fileName);
		nameFileUploaded.setDiskFilename(diskFileName);
		feedbackData.getFileTempDTOs().add(nameFileUploaded);
	}

	public String generateCaptcha() {
		String captcha = "";
		String[] chars = { "A", "B", "C", "D", "E", "F", "G", "H", "E", "I",
				"J", "L", "M", "N", "O", "P", "Q", "R", "T", "S", "X", "W",
				"Y", "Z" };
		for (int i = 0; i < 5; i++) {
			int indexChar = (int) Math.floor(Math.random() * 24);
			captcha = captcha + chars[indexChar];
		}
		feedbackData.setCaptcha(captcha);
		return captcha;
	}

	// getter and setter
	public FeedbackExpressDTO getFeedbackData() {
		return feedbackData;
	}

	public void setFeedbackData(FeedbackExpressDTO feedbackData) {
		this.feedbackData = feedbackData;
	}

	public String getDiskFileNameNeedToDelete() {
		return diskFileNameNeedToDelete;
	}

	public void setDiskFileNameNeedToDelete(String diskFileNameNeedToDelete) {
		this.diskFileNameNeedToDelete = diskFileNameNeedToDelete;
	}

}
