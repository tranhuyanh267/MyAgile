package com.ant.myagile.dto;

import java.util.ArrayList;
import java.util.List;

public class FeedbackExpressDTO {

	private String subject;
	private String description;
	private String captcha;
	private String captchaAnswer;
	private Boolean inputCorrectCaptcha;
	private List<NameFileTempDTO> fileTempDTOs = new ArrayList<NameFileTempDTO>();

	public FeedbackExpressDTO() {
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getCaptcha() {
		return captcha;
	}

	public void setCaptcha(String captcha) {
		this.captcha = captcha;
	}

	public String getCaptchaAnswer() {
		return captchaAnswer;
	}

	public void setCaptchaAnswer(String captchaAnswer) {
		this.captchaAnswer = captchaAnswer;
	}

	public Boolean getInputCorrectCaptcha() {
		return inputCorrectCaptcha;
	}

	public void setInputCorrectCaptcha(Boolean inputCorrectCaptcha) {
		this.inputCorrectCaptcha = inputCorrectCaptcha;
	}

	public List<NameFileTempDTO> getFileTempDTOs() {
		return fileTempDTOs;
	}

	public void setFileTempDTOs(List<NameFileTempDTO> fileTempDTOs) {
		this.fileTempDTOs = fileTempDTOs;
	}

}
