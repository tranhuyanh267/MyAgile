package com.ant.myagile.service.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Flags.Flag;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import com.ant.myagile.dao.MemberDao;
import com.ant.myagile.dto.FeedbackExpressDTO;
import com.ant.myagile.dto.NameFileTempDTO;
import com.ant.myagile.model.Attachment;
import com.ant.myagile.model.Member;
import com.ant.myagile.model.Project;
import com.ant.myagile.model.UserStory;
import com.ant.myagile.service.AttachmentService;
import com.ant.myagile.service.MailService;
import com.ant.myagile.service.MemberService;
import com.ant.myagile.service.ProjectService;
import com.ant.myagile.service.UserStoryService;
import com.ant.myagile.utils.MyAgileFileUtils;
import com.ant.myagile.utils.Utils;
import com.ocpsoft.pretty.faces.util.StringUtils;

/**
 * This class provide many functions to deal with send/receive/process email <br>
 * The properties (account, password, protocol, ...) are defined in pom.xml and
 * depend on the environment.
 * <hr>
 * Ex. Dev & Test server use myagile@axonactive.vn <br>
 * Release server use notify@myagile.org
 */
@Component
public class MailServiceImpl implements MailService {
	private static final String ROOT_FOLDER_PATH = MyAgileFileUtils
			.getRootStorageLocation();
	private static final String TEMP_FOLDER_PATH = MyAgileFileUtils
			.getStorageLocation("myagile.upload.temp.location");
	private static final String ATTACHMENT_FOLDER = "/attachment";
	private static final int BUFFER_SIZE = 4096;
	static String username = Utils.getConfigParam("myagile.email.username");
	static String password = Utils.getConfigParam("myagile.email.password");
	static String smtpHost = Utils.getConfigParam("myagile.email.smtpHost");
	static String imapHost = Utils.getConfigParam("myagile.email.imapHost");
	static String port = Utils.getConfigParam("myagile.email.smtp.port");
	static String fetchsize = Utils
			.getConfigParam("myagile.email.imap.fetchsize");
	static String imapProtocol = Utils
			.getConfigParam("myagile.email.imapProtocol");
	static String mailFrom = Utils.getConfigParam("myagile.email.mailFrom");
	static String emailFeedback = Utils.getConfigParam("myagile.email.feedback");

	private boolean textIsHtml;

	private final static Logger LOGGER = Logger
			.getLogger(MailServiceImpl.class);

	@Autowired
	ProjectService projectService;

	@Autowired
	private MemberDao memberDao;

	@Autowired
	MemberService memberService;

	@Autowired
	UserStoryService userStorySerivce;

	@Autowired
	AttachmentService attachmentService;
	
	@Autowired
	private Utils utils;

	private class SMTPAuthenticator extends Authenticator {
		protected PasswordAuthentication getPasswordAuthentication() {
			return new PasswordAuthentication(MailServiceImpl.username,
					MailServiceImpl.password);
		}
	}

	/**
	 * Open a session with properties
	 * 
	 * @return a session for read mail
	 */
	private Session getSession() {
		Properties properties = new Properties();
		properties.setProperty("mail.smtp.host", smtpHost);
		properties.put("mail.smtp.auth", "true");
		properties.put("mail.imap.fetchsize", fetchsize);
		properties.put("mail.smtp.port", port);
		// This configuration only used in myagile.org server
		// Add SSLSocketFactory for secure layer
		if (Utils.getConfigParam("myagile.application.environment").equals(
				"Release")) {
			properties.put("mail.smtp.socketFactory.class",
					"javax.net.ssl.SSLSocketFactory");
		}
		Session session = Session.getDefaultInstance(properties,
				new SMTPAuthenticator());
		return session;
	}

	@Override
	public boolean checkMail(String email) {
		String emailPattern = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
				+ "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
		Pattern pattern = Pattern.compile(emailPattern);
		Matcher matcher = pattern.matcher(email);
		return matcher.matches();
	}

	@Override
	public String getRealPath() throws UnknownHostException {
		ExternalContext ec = FacesContext.getCurrentInstance()
				.getExternalContext();
		String scheme = ec.getRequestScheme();
		String hostName = ec.getRequestServerName();
		String path = ec.getRequestContextPath();
		String port = ":" + ec.getRequestServerPort();
		return scheme + "://" + hostName + port + path + "/";
	}

	@Override
	public boolean sendMail(String subject, String content, String toEmail)
			throws Exception {
		try {
			Session session = this.getSession();
			MimeMessage message = new MimeMessage(session);
			message.setFrom(new InternetAddress(MailServiceImpl.mailFrom));
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(
					toEmail));
			message.setSubject(subject);
			Multipart mp = new MimeMultipart();
			MimeBodyPart htmlPart = new MimeBodyPart();
			htmlPart.setHeader("Content-Type", "text/html; charset=\"utf-8\"");
			htmlPart.setContent(content, "text/html; charset=utf-8");
			htmlPart.setHeader("Content-Transfer-Encoding", "quoted-printable");
			mp.addBodyPart(htmlPart);
			message.setContent(mp);
			Transport.send(message);
			return true;
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
		}
		return false;
	}

	@Override
	public void readMail() throws Exception {
		Session session = this.getSession();
		Store store = session.getStore(imapProtocol);
		store.connect(imapHost, username, password);
		Folder inboxFolder = store.getFolder("inbox");
		Folder userStoryFolder = store.getFolder("userstory");
		inboxFolder.open(Folder.READ_WRITE);
		Message[] messages = inboxFolder.getMessages();
		for (Message message : messages) {
			try {
				if (!message.getFlags().contains(Flag.SEEN)) {
					message.setFlag(Flag.SEEN, true);
					boolean isAdded = this.processMessage(message);
					if (isAdded) {
						inboxFolder.copyMessages(new Message[] { message },
								userStoryFolder);
						message.setFlag(Flag.DELETED, true);
					} else {
						message.setFlag(Flag.SEEN, false);
					}
				}
			} catch (MessagingException e) {
				LOGGER.error(e.getMessage());
			}
		}
		inboxFolder.close(true);
		store.close();
	}

	@Override
	public boolean processMessage(Message message) {
		String emailFrom;
		try {
			emailFrom = ((InternetAddress) message.getFrom()[0]).getAddress()
					.toString().toLowerCase();
			LOGGER.debug("Enter processMessage with email from: " + emailFrom);
			String subject = message.getSubject().trim();
			String projectName = this.getProjectNameFromEmailSubject(subject);
			Member member = memberService.findMemberByEmail(emailFrom);
			if ((member != null) && (projectName != null)) {
				if (checkMessageNotEmpty(message)) {
					saveUserStoryFromMessage(message, projectName, member);
				} else {
					LOGGER.info("Message body is empty");
				}
				return true;
			} else {
				LOGGER.info("Receive email from unknown receipt or wrong syntax in title: "
						+ emailFrom);
			}
		} catch (Exception e) {
			LOGGER.error(e);
		}
		return false;
	}

	@Override
	public boolean checkMessageNotEmpty(Message message) throws Exception {
		String descriptionOfUserStory = this.getText(message).trim();
		Document doc = Jsoup.parse(descriptionOfUserStory);
		String text = doc.text().replaceAll("[^a-zA-Z0-9]", "");
		if (text != null && text.length() == 0) {
			return false;
		}
		return true;
	}

	@Override
	public boolean saveUserStoryFromMessage(Message message,
			String projectName, Member member) throws Exception {
		String nameOfUserStory = "";
		String descriptionOfUserStory = this.getText(message).trim();
		List<Project> projects = projectService.findByMemberAndOwner(member
				.getMemberId());
		boolean findMatch = false;
		String projectList = "";
		for (Project project : projects) {
			projectList += "<li>" + project.getProjectName() + "</li>";
			if (project.getProjectName().equals(projectName)) {
				findMatch = true;
				LOGGER.debug("Find project match with name: " + projectName);
				UserStory userStory = new UserStory();
				if (this.textIsHtml) {
					nameOfUserStory = this
							.getNameFromMessage(descriptionOfUserStory);
				} else {
					Scanner scanner = new Scanner(descriptionOfUserStory);
					while (nameOfUserStory.length() == 0 && scanner.hasNext()) {
						nameOfUserStory = scanner.nextLine().trim();
					}
					descriptionOfUserStory = descriptionOfUserStory.replaceAll(
							"(\r\n|\n)", "<br />");
					scanner.close();
				}
				userStory.setProject(project);
				userStory.setName(nameOfUserStory);
				userStory.setDescription(descriptionOfUserStory);
				userStorySerivce.createUserStoryFromEmail(userStory,
						member.getUsername());
				getAllAttachment(message, member.getUsername(), project,
						userStory);
				this.sendEmailAddUserStorySuccessful(userStory,
						member.getUsername());
			}
		}
		if (!findMatch) {
			LOGGER.info("No project match with name: " + projectName);
			this.sendEmailNoProjectMatch(projectName, projectList,
					member.getUsername());
			return false;
		}
		return true;
	}

	@Override
	public void sendEmailNoProjectMatch(String projectName, String projectList,
			String email) throws Exception {
		String contentNoPrjectMatch = "<h4>No project match with the name: <b>\""
				+ projectName
				+ "\"</b></h4>"
				+ "<p>The mail subject must be \"#Project Name#\"</p>";
		if (projectList != null && projectList.length() != 0) {
			contentNoPrjectMatch += "<p>Your project name could be: </p>"
					+ projectList;
		} else {
			contentNoPrjectMatch += "<p>You haven't add any project yet! </p>";
		}
		this.sendMail("No project match!", contentNoPrjectMatch, email);
	}

	@Override
	public void sendEmailAddUserStorySuccessful(UserStory userStory,
			String email) throws Exception {
		String contentAddUserStorySuccessful = "<h4>New Userstory had been added to <b>\""
				+ userStory.getProject().getProjectName()
				+ "\"</b> project successfully !</h4>"
				+ "<p>Name: "
				+ userStory.getName()
				+ "</p>"
				+ "<p>Description: "
				+ userStory.getDescription() + "</p>";
		this.sendMail("Your Userstory had been added!",
				contentAddUserStorySuccessful, email);
	}

	@Override
	public String getProjectNameFromEmailSubject(String subject) {
		Pattern pattern = Pattern.compile("^#(.*)#$");
		Matcher matcher = pattern.matcher(subject);
		if (matcher.find()) {
			return matcher.group(1).trim();
		} else {
			return null;
		}
	}

	@Override
	public String getNameFromMessage(String content) {
		Document doc = Jsoup.parse(content);
		String name = "";
		Elements selector = doc.select("body");
		Elements firstDiv = doc.select("body div");
		if (firstDiv.size() > 0 && firstDiv.get(0) != null) {
			String firstDivText = firstDiv.get(0).ownText()
					.replaceAll("[^a-zA-Z0-9]", "");
			if (firstDivText.length() > 0) {
				return firstDiv.get(0).ownText();
			}
		}
		String html = selector.html().replaceAll("(?i)<br[^>]*>", "br2n");
		html = html.replaceAll("(</div>)", "br2n");
		String[] allSentences = Jsoup.parse(html).text().trim().split("br2n");
		int i = 0;
		while (name.length() == 0 && (i < allSentences.length)) {
			String nameTrimed = allSentences[i].replaceAll("[^a-zA-Z0-9]", "");
			if (nameTrimed.length() != 0) {
				name = nameTrimed;
			}
			i++;
		}
		return name;
	}

	@Override
	public String getText(Part p) throws MessagingException, IOException {
		if (p.isMimeType("text/*")) {
			String s = (String) p.getContent();
			this.textIsHtml = p.isMimeType("text/html");
			return s;
		}

		if (p.isMimeType("multipart/alternative")) {
			Multipart mp = (Multipart) p.getContent();
			String text = null;
			for (int i = 0; i < mp.getCount(); i++) {
				Part bp = mp.getBodyPart(i);
				if (bp.isMimeType("text/plain")) {
					if (text == null) {
						text = getText(bp);
					}
					continue;
				} else if (bp.isMimeType("text/html")) {
					this.textIsHtml = bp.isMimeType("text/html");
					String s = getText(bp);
					if (s != null) {
						return s;
					}
				} else {
					return getText(bp);
				}
			}
			return text;
		} else if (p.isMimeType("multipart/*")) {
			Multipart mp = (Multipart) p.getContent();
			for (int i = 0; i < mp.getCount(); i++) {
				String s = getText(mp.getBodyPart(i));
				if (s != null) {
					return s;
				}
			}
		}
		return null;
	}

	/**
	 * this function save attachment of userStory to DB from email
	 * 
	 * @param userStory
	 * @param fileName
	 * @param diskFileName
	 * @param email
	 */
	public boolean saveAttachment(Message message, UserStory userStory,
			String fileName, String diskFileName, String email) {
		try {
			Member author = memberDao.findMemberByEmail(email);
			Attachment attachment = new Attachment();

			attachment.setFilename(fileName);
			attachment.setDiskFilename(diskFileName);
			attachment.setContainerId(userStory.getUserStoryId());
			attachment.setContainerType(Attachment.USERSTORY_ATTACHMENT);
			attachment.setTemp(false);
			attachment.setCreatedOn(message.getReceivedDate());
			attachment.setAuthor(author);
			return attachmentService.save(attachment);
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * This function get attachment from mail and save into disk and database
	 * 
	 * @param message
	 * @param project
	 */
	public void getAllAttachment(Message message, String emailFrom,
			Project project, UserStory userStory) {
		try {
			Multipart multipart = (Multipart) message.getContent();
			for (int i = 0; i < multipart.getCount(); i++) {
				BodyPart bodyPart = multipart.getBodyPart(i);
				if (!Part.ATTACHMENT
						.equalsIgnoreCase(bodyPart.getDisposition())
						&& !StringUtils.isNotBlank(bodyPart.getFileName())) {
					continue;
				}
				InputStream is = bodyPart.getInputStream();
				String path = ROOT_FOLDER_PATH + "/projects/P"
						+ project.getProjectId() + "/attachment/";
				File folderProject = new File(path);
				if (!folderProject.exists()) {
					folderProject.mkdirs();
				}
				String fileNameOld = bodyPart.getFileName();
				String fileNameOnDisk = MyAgileFileUtils
						.getFileNameFinal(bodyPart.getFileName());
				boolean saveSuccess = saveAttachment(message, userStory,
						fileNameOld, fileNameOnDisk, emailFrom);
				if (saveSuccess) {
					File file = new File(folderProject + "/" + fileNameOnDisk);
					FileOutputStream fos = new FileOutputStream(file);
					byte[] buf = new byte[BUFFER_SIZE];
					int bytesRead;
					while ((bytesRead = is.read(buf)) != -1) {
						fos.write(buf, 0, bytesRead);
					}
					fos.close();
				}
			}
		} catch (Exception e) {
		}

	}

	@Override
	public String createAutoNumber() {
		String uuid = UUID.randomUUID().toString();
		uuid = uuid.replace("-", "");
		return uuid;
	}

	@Override
	public boolean sendMailToAdmin(String subject, String content)
			throws Exception {
		List<Member> adminList = memberService.findAdminMembers();
		for (Member admin : adminList) {
			sendMail(subject, content, admin.getUsername());
		}
		return false;
	}

	@Override
	public boolean sendMailFeedbackToAdmin(FeedbackExpressDTO feedbackData) {
		try {
			Session session = this.getSession();
			String[] emailAddresses = emailFeedback.split(";");
			MimeMessage mimeMessage = new MimeMessage(session);
			MimeMessageHelper message = new MimeMessageHelper(
					mimeMessage,true, "UTF-8");
			message.setFrom(new InternetAddress(MailServiceImpl.mailFrom));
			message.setTo(emailAddresses);
			message.setSubject("[Feedback]" + feedbackData.getSubject());
			message.setText(feedbackData.getDescription(), true);
			if(feedbackData.getFileTempDTOs() != null && !feedbackData.getFileTempDTOs().isEmpty()){
				for(NameFileTempDTO fileName : feedbackData.getFileTempDTOs()) {
					File attachFileForMail = new File(TEMP_FOLDER_PATH + ATTACHMENT_FOLDER + "/" + fileName.getDiskFilename());
					message.addAttachment(fileName.getFilename(),attachFileForMail);
				}
			}
			Transport.send(mimeMessage);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error("error sendMailFeedbackToAdmin ",e);
		}
		return false;
		
	}

}
