package com.ant.myagile.managedbean;

import java.io.IOException;
import java.io.Serializable;
import java.net.UnknownHostException;
import java.util.Date;

import javax.faces.context.FacesContext;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.ant.myagile.model.Member;
import com.ant.myagile.service.MailService;
import com.ant.myagile.service.MemberEmailService;
import com.ant.myagile.service.MemberRoleService;
import com.ant.myagile.service.MemberService;
import com.ant.myagile.service.RoleService;
import com.ant.myagile.utils.JSFUtils;
import com.ant.myagile.utils.Utils;

@Component("memberBean")
@Scope("session")
public class MemberBean implements Serializable {
	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = Logger.getLogger(MemberBean.class);
	
	@Autowired
	private MemberService memberService;

	@Autowired
	private Utils utils;

	@Autowired
	private MailService mailService;

	@Autowired
	private MemberEmailService memberEmailService;

	@Autowired
	private RoleService roleService;

	@Autowired
	MemberRoleService memberRoleService;

	private String username;

	/**
	 * Send email with information to reset password in case user forget it.
	 */
	@SuppressWarnings("static-access")
	public void sendMailResetPassword() {
		try {
			Member memberExist = memberService
					.getMemberByUsername(getUsername());
			if (memberExist != null) {
				String newPass = memberService.generatePasswordDefault();

				memberExist.setTokenDate(new Date());
				memberExist.setToken(mailService.createAutoNumber());
				memberExist.setTokenPass(utils.encodePassword(newPass));
				memberService.update(memberExist);

				String path = mailService.getRealPath();
				StringBuffer strbuf = new StringBuffer();
				strbuf.append("<h3>Email to reset your password</h3>");
				strbuf.append("<p>Dear " + memberExist.getFirstName() + "</p>");
				strbuf.append("<p>We recently received a password reset request for this email address. If you did not request a password reset, please ignore this email.</p>");
				strbuf.append("<p>If it was you, please continue to process!</p>");
				strbuf.append("<ul><li>Username: " + getUsername() + "</li>");
				strbuf.append("<li>Password: " + newPass + "</li></ul>");
				strbuf.append("<p><a href='"
						+ path
						+ "pages/member/active.xhtml?tkPass="
						+ memberExist.getToken()
						+ "'>Please click here to reset your password and login to My Agile application.</a></p>");
				if (mailService.sendMail("Reset your password",
						strbuf.toString(), getUsername())) {
					JSFUtils.addSuccessMessage("msgs", utils.getMessage(
							"myagile.member.SendEmailPasswordSuccess", null));
				} else {
					JSFUtils.addErrorMessage("msgs", utils.getMessage(
							"myagile.member.SendEmailError", null));
				}
			} else {
				JSFUtils.addWarningMessage("msgs", utils.getMessage(
						"myagile.member.SendEmailNotExist", null));
			}
		} catch (Exception e) {

		}
	}
	
	public void goToForgotPassWord(){
		String username = JSFUtils.getRequestParameter("username");
		if(username != null){
		this.username = username;
		}
		String contextPath = FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath() + "/forgetPassword";
		JSFUtils.redirect(contextPath);
	}

	/**
	 * Allow user's account to be active after clicking on link in forget
	 * password mail
	 */
	@SuppressWarnings("static-access")
	public void activeForgetPassword() {
		try {
			FacesContext facesContext = FacesContext.getCurrentInstance();
			String tkPass = facesContext.getExternalContext()
					.getRequestParameterMap().get("tkPass");
			Member memberCheck = memberService.findMemberByToken(tkPass);
			if (memberCheck != null) {
				memberCheck.setPassword(memberCheck.getTokenPass());
				String tk = mailService.createAutoNumber();
				memberCheck.setToken(tk);
				memberCheck.setTokenDate(new Date());
				memberCheck.setPasswordExpired(false);
				memberService.update(memberCheck);
				JSFUtils.addSuccessMessage("msgs",
						utils.getMessage("myagile.member.ActivePassword", null));
			} else {
				JSFUtils.addErrorMessage("msgs",
						utils.getMessage("myagile.member.TokenInvalid", null));
			}
		} catch (Exception e) {
			JSFUtils.addErrorMessage("msgs",
					utils.getMessage("myagile.member.ErrorResetPass", null));
		}
	}

	/**
	 * check userExist on system when input on login page
	 */
	public void checkExistMember() {
		String userName = JSFUtils.getRequestParameter("username")
				.toLowerCase();
		Member memberTemp = memberService.findMemberByEmail(userName.trim());
		boolean flag;
		if (memberTemp == null) {
			Member member = memberService.findMemberByAlternateEmail(userName);
			if (member != null) {
				flag = true;
			} else {
				flag = false;
			}
		} else {
			flag = true;
		}
		JSFUtils.addCallBackParam("memberExist", flag);
	}

	/**
	 * 
	 */
	public void checkLockedMember() {
		String userName = JSFUtils.getRequestParameter("username")
				.toLowerCase();
		Member member = memberService.findMemberByEmail(userName.trim());
		JSFUtils.addCallBackParam("memberLocked", member.isAccountLocked());
	}

	/***
	 * This fuction to save new member before send email to confirm
	 * 
	 * @param email
	 * @param password
	 * @param firstName
	 * @param lastName
	 * @param tk
	 * @return
	 */
	public boolean saveNewMember(String email, String password,
			String firstName, String lastName, String tk) {
		Member newMember = new Member();
		newMember.setUsername(email.toLowerCase());
		newMember.setFirstName(firstName);
		newMember.setLastName(lastName);
		newMember.setlDapUsername("");
		newMember.setToken(tk);
		newMember.setActive(false);
		newMember.setAvatar("user.png");
		newMember.setEnabled(true);
		newMember.setPassword(utils.encodePassword(password));
		newMember.setPasswordExpired(false);
		newMember.setAccountExpired(true);
		return memberService.saveAndAddRoleMember(newMember, (long) 1);
	}

	/**
	 * Send email for new user register
	 * 
	 * @throws Exception
	 */
	public void sendMailRegister() throws Exception {
		String contextPath = FacesContext.getCurrentInstance()
				.getExternalContext().getRequestContextPath()
				+ "/login?error=";
		String mailTo = JSFUtils.getRequestParameter("email").toLowerCase();
		String firstName = JSFUtils.getRequestParameter("firstName");
		String lastName = JSFUtils.getRequestParameter("lastName");
		String password = JSFUtils.getRequestParameter("password");
		String path = mailService.getRealPath();
		String tk = mailService.createAutoNumber();
		String activeUrl = path + "pages/login/active.xhtml?tk=" + tk;
		String content = "<h4>Welcome to My Agile!</h4><p>Dear "
				+ firstName
				+ ",</p><br>"
				+ "<p>This mail has been sent from My Agile system.</p>"
				+ "<p>Almost finished. We need to confirm your email address</p>"
				+ "<p><a href=\""
				+ activeUrl
				+ "\">Click here</a> to active!</p>"
				+ "<p> If you can't click on this, please copy this text below and paste to your browser: <br>"
				+ "<a href=\"" + activeUrl + "\">" + activeUrl + "</a></p>"
				+ "<p>This is an auto-generated email, please do not reply MyAgile<https://www.myagile.org/></p>";
		try {
			//check exist email
			Member memberFromUserName = memberService.getMemberByUsername(mailTo.trim());
			if(memberFromUserName == null && !mailTo.trim().isEmpty()){
				//save account
				if (saveNewMember(mailTo.trim(), password, firstName, lastName, tk)) {
					mailService.sendMail(
							"Welcome to My Agile! Please confirm your email",
							content, mailTo.trim());

				} else {
					JSFUtils.redirect(contextPath + "sendMailFail");
				}
			} else {
				LOGGER.warn("Note: User signup a existed account= " + mailTo.trim());
			}
		} catch (Exception e) {
			JSFUtils.redirect(contextPath + "notsuccess");
		}
	}

	/**
	 * Set account active when logging in first time to system
	 * 
	 * @throws IOException
	 */
	@SuppressWarnings("static-access")
	public void activeAccount() throws IOException {
		try {
			FacesContext facesContext = FacesContext.getCurrentInstance();
			String tk = facesContext.getExternalContext()
					.getRequestParameterMap().get("tk");
			Member memberTemp = memberService.findMemberByToken(tk);
			if (memberTemp != null) {
				// not accept if exist a member or an alternate email is
				// activated
				if (!memberEmailService.checkExistEmail(memberTemp
						.getUsername())) {
					JSFUtils.addErrorMessage("msgs", utils.getMessage(
							"myagile.member.ActiveAccountUnsuccessByNotLogic",
							null));
					return;
				}
				if (memberTemp.isActive()) {
					JSFUtils.addWarningMessage("msgs", utils.getMessage(
							"myagile.member.WarringActiveAccount", null));
				} else {
					if (!memberTemp.getUsername().equals(
							utils.getLoggedInMember().getUsername())) {
						JSFUtils.addErrorMessage("msgs", utils.getMessage(
								"myagile.member.WarringNotLogin", null));
					} else {
						if (!memberTemp.isPasswordExpired()) {
							memberTemp.setActive(true);
						}
						memberTemp.setAccountExpired(false);
						memberService.update(memberTemp);
						if (memberTemp.isPasswordExpired()) {
							JSFUtils.addWarningMessage("msgs", utils
									.getMessage(
											"myagile.member.ActiveMailSuccess",
											null));
						} else {
							JSFUtils.addSuccessMessage(
									"msgs",
									utils.getMessage(
											"myagile.member.ActiveAccountSuccess",
											null));
							String contextPath = FacesContext
									.getCurrentInstance().getExternalContext()
									.getRequestContextPath();
							JSFUtils.redirect(contextPath + "/profile");
						}
					}
				}
			} else {
				JSFUtils.addErrorMessage("msgs",
						utils.getMessage("myagile.member.TokenInvalid", null));
			}
		} catch (Exception e) {
			JSFUtils.addErrorMessage("msgs", utils.getMessage(
					"myagile.member.ActiveAccountUnsuccess", null));
		}
	}

	/**
	 * Resend email to active member account
	 * 
	 * @throws UnknownHostException
	 */
	@SuppressWarnings("static-access")
	public void resendActiveMail() throws UnknownHostException {
		String path = mailService.getRealPath();
		Member memberTemp = memberService.findMemberByEmail(this.username);
		if (memberTemp != null) {
			String content = "<h4>Welcome to My Agile!</h4>"
					+ "<p>Dear "
					+ memberTemp.getLastName()
					+ ",</p>"
					+ "<br>"
					+ "<p>This mail has been sent from MyAgile system.</p>"
					+ "<p>We recently received a activation request for this email address. If you did not request a activation request, please ignore this email.</p>"
					+ "<p>Almost finished. We need to confirm your email address</p>"
					+ "<p><a href=\""
					+ path
					+ "pages/login/active.xhtml?tk="
					+ memberTemp.getToken()
					+ "\">Click here</a> to active!</p>"
					+ "<p>This is an auto-generated email, please do not reply MyAgile<https://www.myagile.org/></p>";
			try {
				if (mailService.sendMail(
						"Welcome to My Agile! Please confirm your email",
						content, this.username)) {
					String email = memberTemp.getUsername();
					Object[] params = { email };
					JSFUtils.addSuccessMessage("messages", utils.getMessage(
							"myagile.member.SendEmailSuccess", params));
				} else {
					JSFUtils.addErrorMessage("messages", utils.getMessage(
							"myagile.member.SendEmailError", null));
				}
			} catch (Exception e) {
				JSFUtils.addErrorMessage("messages",
						utils.getMessage("myagile.member.SendEmailError", null));
			}
		}
	}

	public void getLDapUsername() {
		String username = JSFUtils.getRequestParameter("username");
		Member member = memberService.getMemberByUsername(username);
		String lDapUserName = "";

		if (member != null && !member.getlDapUsername().equals("")) {
			lDapUserName = member.getlDapUsername();
		}
		JSFUtils.addCallBackParam("username", lDapUserName);
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public boolean isAdministrator(long memberId) {
		return memberRoleService.isAdministrator(memberId);
	}

	public boolean isLocked(long memberId) {
		return memberService.findMemberById(memberId).isAccountLocked();
	}
}
