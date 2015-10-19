package com.ant.myagile.managedbean;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;
import javax.imageio.ImageIO;

import org.primefaces.context.RequestContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.ant.myagile.model.Member;
import com.ant.myagile.model.MemberEmail;
import com.ant.myagile.service.MailService;
import com.ant.myagile.service.MemberEmailService;
import com.ant.myagile.service.MemberService;
import com.ant.myagile.utils.JSFUtils;
import com.ant.myagile.utils.MyAgileFileUtils;
import com.ant.myagile.utils.Utils;

@Component("profileMB")
@Scope("session")
public class ProfileBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Autowired
	private MemberService memberService;

	@Autowired
	private MailService mailService;

	@Autowired
	private MemberEmailService memberEmailService;

	@Autowired
	private Utils utils;

	private Member member;
	private String error;
	private String email;
	private String pass1;
	private String pass2;
	private String pass3;
	private String confirmPass;
	private String oldEmail;

	private List<MemberEmail> emailList = new ArrayList<MemberEmail>();
	private MemberEmail memberEmail;
	private String emailAddress;
	private MemberEmail deletedEmail;
	private MemberEmail switchedEmail;
	private static final int MAX_NUMBER_OF_EMAIL = 8;
	private final String imageMemberFolder = MyAgileFileUtils.getStorageLocation("myagile.upload.image.member.folder");
	
	private String x1;
	private String y1;
	private String x2;
	private String y2;
	private String w;
	private String h;
	private String fileName = "hcm_office.jpg";
	@PostConstruct
	public void init() {
		x1 = "1";
	}
	/**
	 * handle when each request comes
	 */
	public void initPreview() {
		if (JSFUtils.isPostbackRequired()) {
			member = utils.getLoggedInMember();
			loadMemberEmailList();
			resetValue();
			JSFUtils.resetForm("editProfile");
		}
	}

	/**
	 * add new member email to email member list not accept an email without
	 * value; not accept an email is exist in the system (primary email and
	 * alternate email of a members is activated) not accept an email if number
	 * of email of member greater than 8 Else, add the email become alternate
	 * email of current user and send a confirm email to added email.
	 */
	public void addEmailToEmailList() {
		if (emailAddress != null) {
			emailAddress = emailAddress.toLowerCase().trim();
			if (emailAddress.equals("")) {
				JSFUtils.addCallBackParam("success", false);
				return;
			}
			if (!memberEmailService.checkExistEmail(emailAddress)) {
				JSFUtils.addCallBackParam("success", false);
				JSFUtils.addErrorMessage("profileMessage", Utils.getMessage("myagile.profile.DuplicateEmailOfSystem", null));
				return;
			}
			loadMemberEmailList();
			if (emailList.size() >= MAX_NUMBER_OF_EMAIL) {
				JSFUtils.addErrorMessage("profileMessage", Utils.getMessage("myagile.profile.OverNumberOfEmail", null));
				return;
			}
			try {
				memberEmail = new MemberEmail();
				memberEmail.setEmail(emailAddress);
				memberEmail.setMember(member);
				memberEmail.setActive(false);
				String token = mailService.createAutoNumber();
				memberEmail.setToken(token);
				if (memberEmailService.create(memberEmail)) {
					sendMailToAternateEmail(memberEmail, token);
					JSFUtils.addCallBackParam("success", true);
					Object[] params = { emailAddress };
					JSFUtils.addSuccessMessage("profileMessage", Utils.getMessage("myagile.profile.AddAlternateEmailSuccess", params));
					handleBeforeAddEmail();
				} else {
					JSFUtils.addErrorMessage("profileMessage", Utils.getMessage("myagile.profile.AddAlternateEmailFail", null));
				}
			} catch (Exception e) {
				JSFUtils.addErrorMessage("profileMessage", Utils.getMessage("myagile.profile.AddAlternateEmailFail", null));
			}
		} else {
			JSFUtils.addCallBackParam("success", false);
		}
	}

	/**
	 * load email list from database
	 */
	private void loadMemberEmailList() {
		emailList.clear();
		emailList.addAll(memberEmailService.findMemberEmailByMember(member));
	}

	/**
	 * handle before adding alternate email
	 */
	public void handleBeforeAddEmail() {
		JSFUtils.resetForm("editProfile");
		setEmailAddress("");
		loadMemberEmailList();
	}

	/**
	 * send mail to added mail
	 * 
	 * @param memEmail
	 *            : added member mail
	 * @param tk
	 *            : token
	 */
	private void sendMailToAternateEmail(MemberEmail memEmail, String tk) {
		String path;
		try {
			path = mailService.getRealPath();
			String content = "<p>This mail has sent from MyAgile system.</p> " + "<p>Your email <strong>" + memEmail.getEmail() + "</strong> was added as an alternate email of "
					+ utils.getLoggedInMember().getUsername() + " in Myagile (www.myagile.org).</p>" + "<p>Please <a href=\"" + path + "pages/profile/active.xhtml?tk=" + tk + "&id="
					+ memEmail.getEmailId() + "\">click here</a> " + "to accept and activate!</p>" + "<p>Please contact us in necessary case!<br/>";
			mailService.sendMail("Activate your alternate email", content, memEmail.getEmail());
		} catch (UnknownHostException e) {
			JSFUtils.addErrorMessage("profileMessage", Utils.getMessage("myagile.profile.SendAlternateEmailFail", null));
		} catch (Exception e) {
			JSFUtils.addErrorMessage("profileMessage", Utils.getMessage("myagile.profile.SendAlternateEmailFail", null));
		}
	}

	/**
	 * active added member email receive a request with 2 values: tk(token of
	 * alternate email) and id (id of alternate email) Not accept if member
	 * delete the alternate email before activating the email if token is valid,
	 * alternate email will be activated and the token also be deleted, the user
	 * is not valid will be delete
	 * 
	 */
	public void activeEmail() {
		try {
			String token = JSFUtils.getRequestParameter("tk");
			String mEmailId = JSFUtils.getRequestParameter("id");
			MemberEmail mMail = memberEmailService.findMemberEmailById(Long.valueOf(mEmailId));
			if (mMail == null) {
				if (deletedEmail != null && mEmailId.equals(deletedEmail.getEmailId().toString())) {
					Object[] params = { deletedEmail.getEmail() };
					JSFUtils.addErrorMessage("profileMessage", Utils.getMessage("myagile.profile.InvalidMemberEmail", params));
					return;
				}
				JSFUtils.addErrorMessage("profileMessage", Utils.getMessage("myagile.profile.InvalidActivatedLink", null));
				return;
			}
			if (!memberEmailService.checkExistEmail(mMail.getEmail())) {
				JSFUtils.addErrorMessage("profileMessage", Utils.getMessage("myagile.member.ActiveMailUnsuccessByNotLogic", null));
				return;
			}
			if (mMail.getToken().equals(token)) {
				if (!mMail.getMember().getUsername().equals(utils.getLoggedInMember().getUsername())) {
					JSFUtils.addErrorMessage("profileMessage", Utils.getMessage("myagile.prifile.NoMemberLogin", null));
				} else {
					mMail.setToken("");
					mMail.setActive(true);
					if (memberEmailService.update(mMail)) {
						JSFUtils.addSuccessMessage("profileMessage", Utils.getMessage("myagile.profile.ActiveAlternateEmailSuccess", null));
						for (Member m : memberService.findAll()) {
							if (!(m.isActive()) && m.getUsername().equals(mMail.getEmail())) {
								memberService.delete(m);
							}
						}
					} else {
						mMail.setToken(token);
						mMail.setActive(false);
						JSFUtils.addErrorMessage("profileMessage", Utils.getMessage("myagile.profile.ActiveAlternateEmailFail", null) + "cannot update---");
					}
				}
			} else {
				JSFUtils.addErrorMessage("profileMessage", Utils.getMessage("myagile.profile.InvalidToken", null));
			}
		} catch (Exception e) {
			JSFUtils.addErrorMessage("profileMessage", Utils.getMessage("myagile.profile.ActiveAlternateEmailFail", null) + "other catch-");
		}
	}

	/**
	 * 
	 * validate input email
	 */
	public void validateEmail(FacesContext context, UIComponent validate, Object value) throws ValidatorException {
		if (value == null) {
			return;
		}
		String emailtemp = value.toString().toLowerCase().trim();
		if (emailtemp.equals("")) {
			return;
		}
		if (!memberEmailService.checkExistEmail(Utils.standardizeString(value.toString()), getMember())) {
			FacesMessage msg = new FacesMessage(Utils.getMessage("myagile.profile.DuplicateEmailOfMember", null));
			throw new ValidatorException(msg);
		}
		if (!Utils.checkEmailFormat(Utils.standardizeString(value.toString().toLowerCase()))) {
			FacesMessage msg = new FacesMessage(Utils.getMessage("myagile.profile.InvalidEmailFormat", null));
			throw new ValidatorException(msg);
		}
	}

	/**
	 * delete selected email
	 */
	public void deleteMemberEmail() {
		try {
			if (memberEmailService.delete(deletedEmail)) {
				loadMemberEmailList();
			} else {
				JSFUtils.addErrorMessage("profileMessage", Utils.getMessage("myagile.profile.DeleteAlternateEmailFail", null));
			}
		} catch (Exception e) {
			JSFUtils.addErrorMessage("profileMessage", Utils.getMessage("myagile.profile.DeleteAlternateEmailFail", null));
		}
	}

	/**
	 * change selected email to primary email swap email and redirect to login
	 * page because of session problem
	 */
	public void switchAlternateToPrimaryEmail() {
		try {
			String temp = member.getUsername();
			member.setUsername(switchedEmail.getEmail());
			MemberEmail mEmailTemp = memberEmailService.findMemberEmailByEmail(switchedEmail.getEmail());
			if (switchedEmail != null) {
				if (memberService.update(member)) {
					mEmailTemp.setEmail(temp);
					if (memberEmailService.update(mEmailTemp)) {
						ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
						ec.invalidateSession();
						String contextPath = FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath() + "/";
						ec.redirect(contextPath);
					} else {
						member.setUsername(temp);
						memberService.update(member);
					}
				}
			}
			JSFUtils.addErrorMessage("profileMessage", Utils.getMessage("myagile.profile.SwitchAlternateEmailFail", null));
		} catch (Exception e) {
			JSFUtils.addErrorMessage("profileMessage", Utils.getMessage("myagile.profile.SwitchAlternateEmailFail", null));
		}
	}

	public void resetValue() {
		this.setPass1("");
		this.setPass2("");
		this.setPass3("");
		this.setMember(utils.getLoggedInMember());
		this.setOldEmail(this.member.getUsername().trim());
	}

	public String resetValueCancel() {
		resetValue();
		return "/pages/home/index?faces-redirect=true";
	}

	public String getFullName() {
		if (getMember().getFirstName() == null && getMember().getLastName() == null) {
			return getMember().getUsername();
		}
		return getMember().getLastName() + " " + getMember().getFirstName();

	}

	@SuppressWarnings("static-access")
	public boolean changePasswordNoExpired() {
		String confirmPassChange = utils.encodePassword(this.pass1);
		if (!pass1.equals("") && !confirmPassChange.equals(this.member.getPassword())) {
			FacesContext.getCurrentInstance().addMessage("profileMessage", new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERROR:", this.utils.getMessage("myagile.profile.InvalidOldPass", null)));
			return false;
		} else if (pass1.equals("")) {
			if (!pass2.equals("") || !pass3.equals("")) {
				FacesContext.getCurrentInstance().addMessage("profileMessage", new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERROR:", this.utils.getMessage("myagile.profile.InvalidOldPass", null)));
				return false;
			}
		} else if (!pass1.equals("") && confirmPassChange.equals(this.member.getPassword())) {
			if(!isStrongPassword(pass2)){
				FacesContext.getCurrentInstance().addMessage("profileMessage", new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERROR:", this.utils.getMessage("myagile.PassNotStrong", null)));
				return false;
			}else if (!pass2.equals("") && !pass3.equals("") && (pass2.equals(pass3))) {
				this.member.setPassword(utils.encodePassword(pass2));
				return true;
			} else if ((!pass2.equals("") || !pass3.equals("")) && (!pass2.equals(pass3))) {
				FacesContext.getCurrentInstance().addMessage("profileMessage", new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERROR:", this.utils.getMessage("myagile.profile.ErrorMatchPass", null)));
				return false;
			} else if ((pass2.equals("") && pass3.equals(""))) {
				FacesContext.getCurrentInstance().addMessage("profileMessage", new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERROR:", this.utils.getMessage("myagile.profile.ErrorNullPass", null)));
				return false;
			}
		}
		return true;
	}
	
	private boolean isStrongPassword(String password){
		String regex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$";//at least uppercase, lowercase and digit
		return password.matches(regex);
		
	}

	@SuppressWarnings("static-access")
	public boolean changePasswordExpired() {
		if (!pass2.equals("") && !pass3.equals("") && (pass2.equals(pass3))) {
			this.member.setPassword(utils.encodePassword(pass2));
			this.member.setPasswordExpired(false);
			return true;
		}
		if ((!pass2.equals("") || !pass3.equals("")) && (!pass2.equals(pass3))) {
			FacesContext.getCurrentInstance().addMessage("profileMessage", new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERROR:", this.utils.getMessage("myagile.profile.ErrorMatchPass", null)));
			return false;
		}
		return true;
	}

	@SuppressWarnings("static-access")
	public boolean checkData() {
		boolean result = true;
		if (this.member.isPasswordExpired()) {
			result = changePasswordExpired();
		} else {
			result = changePasswordNoExpired();
		}
		if ((!this.member.getMobile().equals("") || this.member.getMobile() != null) && !this.memberService.checkMobile(this.member.getMobile())) {
			FacesContext.getCurrentInstance().addMessage("profileMessage", new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERROR:", this.utils.getMessage("myagile.profile.InvalidPhone", null)));
			result = false;
		}
		if (this.member.getLastName().equals("") || this.member.getFirstName().equals("")) {
			result = false;
		}
		return result;
	}

	/**
	 * Update logged in member's profile information, allow every field can be
	 * null
	 */
	@SuppressWarnings("static-access")
	public void updateMember() {
		if (checkData()) {
			if (!oldEmail.equals(member.getUsername().trim())) {
				this.member.setAccountExpired(false);
			}
			if (!this.member.isAccountExpired() && !this.member.isPasswordExpired()) {
				this.member.setActive(true);
			}
			if (memberService.update(this.getMember())) {
				FacesContext.getCurrentInstance().addMessage("profileMessage", new FacesMessage(FacesMessage.SEVERITY_INFO, "Successful:", this.utils.getMessage("myagile.UpdateSuccess", null)));
			}
		}
	}

	/**
	 * Replace old member's avatar by a new one
	 */
	public void updateAvatar() {
		Member memberTemp = new Member();
		memberTemp = getMember();
		String filename = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("filename");
		String newFileName = this.memberService.memberImageNameProcess(memberTemp.getUsername());
		this.memberService.deleteLogo(memberTemp.getAvatar());
		String newLogoFilename = this.memberService.renameLogo(filename, newFileName);
		memberTemp.setAvatar(newLogoFilename);
		this.memberService.update(memberTemp);
	}

	/**
	 * function check
	 * 
	 * @return true or false
	 */
	@SuppressWarnings("static-access")
	public boolean checkEmail() {
		return utils.checkEmailFormat(this.member.getUsername());
	}

	/**
	 * check userExist on system when input on login page
	 */
	public void checkExistEmail() {
		RequestContext request = RequestContext.getCurrentInstance();
		setEmail(JSFUtils.getRequestParameter("username"));
		Member memberTemp = this.memberService.findMemberByEmail(this.email.trim());
		boolean flag;
		if (memberTemp == null) {
			flag = false;
		} else {
			flag = true;
		}
		request.addCallbackParam("emailExist", flag);
	}

	/**
	 * Merge with email exists on system to one account
	 */
	public void mergeAccount() {
		RequestContext request = RequestContext.getCurrentInstance();
		Member memberTemp = this.memberService.findMemberByEmail(this.email);
		String passOldMember = memberTemp.getPassword();
		String passTemp = this.utils.encodePassword(this.confirmPass);
		if (passOldMember.equals(passTemp)) {
			memberTemp.setlDapUsername(this.member.getlDapUsername());
			this.memberService.update(memberTemp);
			this.memberService.delete(this.member);
			request.update("editProfile");
		} else {
			request.addCallbackParam("correctPass", false);
		}

	}
	
	public void updateFullPathLogo(){
	    fileName = member.getAvatar();
	}
	
	public void cropImage() throws IOException{
	    FacesContext context = FacesContext.getCurrentInstance();
        Map<String, String> map = context.getExternalContext()
                .getRequestParameterMap();
        String strImageWidth = map.get("imageWidth");
        String strImageHeight = map.get("imageHeight");
	    int x1 = Integer.parseInt(map.get("x1"));
	    int y1 = Integer.parseInt(map.get("y1"));
	    int w = Integer.parseInt(map.get("w"));
	    int h = Integer.parseInt(map.get("h"));
	    int imageWidth = Integer.parseInt(strImageWidth);
	    int imageHeight = Integer.parseInt(strImageHeight);
	    
	    String fullPath = imageMemberFolder + "/" + fileName;
        BufferedImage image = ImageIO.read(new File(fullPath));
        int heightRaw = image.getHeight();
        int widthRaw = image.getWidth();
        BufferedImage out=image.getSubimage(x1*widthRaw/imageWidth,y1*heightRaw/imageHeight,w*widthRaw/imageWidth,h*heightRaw/imageHeight);
        ImageIO.write(out,"jpg",new File(fullPath));
        
        Member memberTemp = new Member();
        memberTemp = getMember();
        String filename = fileName;
        String newFileName = this.memberService.memberImageNameProcess(memberTemp.getUsername());
        String newLogoFilename = this.memberService.renameLogo(filename, newFileName);
        memberTemp.setAvatar(newLogoFilename);
        this.memberService.update(memberTemp);
}

	public Member getMember() {
		if (this.member == null) {
			this.member = this.utils.getLoggedInMember();
		}
		return this.member;

	}

	public void setMember(Member member) {
		this.member = member;
	}

	public String getConfirmPass() {
		return this.confirmPass;
	}

	public void setConfirmPass(String confirmPass) {
		this.confirmPass = confirmPass;
	}

	public String getPass1() {
		return pass1;
	}

	public void setPass1(String pass1) {
		this.pass1 = pass1;
	}

	public String getPass2() {
		return pass2;
	}

	public void setPass2(String pass2) {
		this.pass2 = pass2;
	}

	public String getPass3() {
		return pass3;
	}

	public void setPass3(String pass3) {
		this.pass3 = pass3;
	}

	public String getEmail() {
		return this.email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Utils getUtils() {
		return this.utils;
	}

	public void setUtils(Utils utils) {
		this.utils = utils;
	}

	public String getError() {
		return this.error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public String getOldEmail() {
		return oldEmail;
	}

	public void setOldEmail(String oldEmail) {
		this.oldEmail = oldEmail;
	}

	public List<MemberEmail> getEmailList() {
		return emailList;
	}

	public void setEmailList(List<MemberEmail> emailList) {
		this.emailList = emailList;
	}

	public MemberEmail getMemberEmail() {
		return memberEmail;
	}

	public void setMemberEmail(MemberEmail memberEmail) {
		this.memberEmail = memberEmail;
	}

	public String getEmailAddress() {
		if (emailAddress != null) {
			emailAddress = emailAddress.toLowerCase();
		}
		return emailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

	public MemberEmail getDeletedEmail() {
		return deletedEmail;
	}

	public void setDeletedEmail(MemberEmail deletedEmail) {
		this.deletedEmail = deletedEmail;
	}

	public MemberEmail getSwitchedEmail() {
		return switchedEmail;
	}

	public void setSwitchedEmail(MemberEmail switchedEmail) {
		this.switchedEmail = switchedEmail;
	}

    public String getX1() {
        return x1;
    }

    public void setX1(String x1) {
        this.x1 = x1;
    }

    public String getY1() {
        return y1;
    }

    public void setY1(String y1) {
        this.y1 = y1;
    }

    public String getX2() {
        return x2;
    }

    public void setX2(String x2) {
        this.x2 = x2;
    }

    public String getY2() {
        return y2;
    }

    public void setY2(String y2) {
        this.y2 = y2;
    }

    public String getW() {
        return w;
    }

    public void setW(String w) {
        this.w = w;
    }

    public String getH() {
        return h;
    }

    public void setH(String h) {
        this.h = h;
    }

    public String getFullPathLogo() {
        return fileName;
    }

    public void setFullPathLogo(String fullPathLogo) {
        this.fileName = fullPathLogo;
    }
    
	
}
