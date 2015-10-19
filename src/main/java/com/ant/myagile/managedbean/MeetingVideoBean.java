package com.ant.myagile.managedbean;

import java.io.IOException;
import java.io.Serializable;
import java.text.ParseException;
import java.util.Date;

import javax.faces.context.FacesContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.ant.myagile.model.MeetingVideo;
import com.ant.myagile.service.MeetingVideoService;

@Component("meetingVideoBean")
@Scope("session")
public class MeetingVideoBean implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@Autowired
	private MeetingVideoService meetingVideoService;	
	private MeetingVideo meetingVideo;	
	private Date meetingDate;	
	/**
	 * Find video file of date when changing date of Sprint
	 * @throws ParseException
	 */
	public void meetingDateChange() throws ParseException {
		String sprintId = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("sprintId");
		if(sprintId != null) {
			meetingVideo = meetingVideoService.findMeetingVideoByDateAndSprintId(meetingDate, Long.valueOf(sprintId));
		}
	}
	/**
	 * Find video file of date when changing date of Sprint
	 * @param sprintId - ID of Sprint, Long number format
	 * @throws ParseException
	 */
	public void meetingDateChange(Long sprintId) throws ParseException {
		if(sprintId != null) {
			meetingVideo = meetingVideoService.findMeetingVideoByDateAndSprintId(meetingDate, sprintId);
		}
	}


	public MeetingVideo getMeetingVideo() {
		return meetingVideo;
	}


	public void setMeetingVideo(MeetingVideo meetingVideo) {
		this.meetingVideo = meetingVideo;
	}


	public Date getMeetingDate() throws ParseException {
		return meetingDate;
	}


	public void setMeetingDate(Date meetingDate) throws ParseException {
		this.meetingDate = meetingDate;
	}
	
	/**
	 * Set a video for a date of sprint if it has not. Otherwise, replace old video by a new one.
	 * @throws ParseException
	 * @throws IllegalStateException
	 * @throws IOException
	 */
	public void updateMeetingVideo() throws ParseException, IllegalStateException, IOException {
		String filename = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("filename");
		String sprintIdStr = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("sprintId");
		String teamIdStr = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("teamId");

		Long sprintId = Long.parseLong(sprintIdStr);
		Long teamId = Long.parseLong(teamIdStr);
	
		String oldFileName = filename;
		String newFileName = meetingVideoService.processVideoFileName(oldFileName, sprintId, this.meetingDate);
		
		
		if(meetingVideo == null) {
			meetingVideoService.changeAndMoveVideoFileName(oldFileName, newFileName, sprintId, teamId);
			this.setMeetingVideo(meetingVideoService.addMeetingVideo(newFileName, meetingDate, sprintId));
		} else {
			MeetingVideo mv = meetingVideoService.findMeetingVideoByDateAndSprintId(this.meetingDate, sprintId);
			meetingVideoService.deleteVideoFile(mv.getVideoFileName(),teamId,sprintId);
			meetingVideoService.changeAndMoveVideoFileName(oldFileName, newFileName, sprintId, teamId);
			this.setMeetingVideo(meetingVideoService.updateMeetingVideo(newFileName, meetingDate, sprintId));
		}
	}
	
}
