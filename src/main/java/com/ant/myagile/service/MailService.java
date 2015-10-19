package com.ant.myagile.service;

import java.io.IOException;
import java.net.UnknownHostException;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Part;

import com.ant.myagile.dto.FeedbackExpressDTO;
import com.ant.myagile.model.Member;
import com.ant.myagile.model.UserStory;

public interface MailService {
	boolean checkMail(String email);

	String getRealPath() throws UnknownHostException;

	boolean sendMail(String subject, String content, String toEmail)
			throws Exception;

	boolean sendMailToAdmin(String subject, String content)
			throws Exception;

	/**
	 * Read inbox mail, detect new mail and process it
	 * 
	 * @throws Exception
	 */
	void readMail() throws Exception;

	/**
	 * Handle message, check if project match to add new userstory
	 * 
	 * @param message
	 */
	boolean processMessage(Message message);

	/**
	 * Send email when not find match project name
	 * 
	 * @param projectName
	 * @param projectList
	 * @param email
	 * @throws Exception
	 */
	void sendEmailNoProjectMatch(String projectName, String projectList,
			String email) throws Exception;

	/**
	 * Send email when add new userstory successful
	 * 
	 * @param userStory
	 * @param email
	 * @throws Exception
	 */
	void sendEmailAddUserStorySuccessful(UserStory userStory, String email)
			throws Exception;

	/**
	 * Extract project name from subject of email Ex: extract project "My Agile"
	 * from subject "#My Agile#"
	 * 
	 * @param subject
	 *            : subject from email
	 * @return project name
	 */
	String getProjectNameFromEmailSubject(String subject);

	/**
	 * Extract the name of the userstory from the content of the email which in
	 * HTML format
	 * 
	 * @param content
	 *            : content of email in HTML format
	 * @return
	 */
	String getNameFromMessage(String content);

	/**
	 * Return the primary text content of the message. Refer HTML format
	 */
	String getText(Part p) throws MessagingException, IOException;

	String createAutoNumber();

	boolean saveUserStoryFromMessage(Message message, String projectName,
			Member member) throws Exception;

	/**
	 * Check if a message content null or not
	 * 
	 * @param message
	 * @return
	 * @throws Exception
	 */
	boolean checkMessageNotEmpty(Message message) throws Exception;

	boolean sendMailFeedbackToAdmin(FeedbackExpressDTO feedbackData);

}
