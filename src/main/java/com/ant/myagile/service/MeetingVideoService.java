package com.ant.myagile.service;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import com.ant.myagile.model.MeetingVideo;
import com.ant.myagile.model.Sprint;

public interface MeetingVideoService {
	MeetingVideo addMeetingVideo(String originalFilename, Date meetingDate, Long sprintId)
			throws IllegalStateException, IOException;
	MeetingVideo updateMeetingVideo(String originalFilename, Date meetingDate, Long sprintId);
	MeetingVideo findMeetingVideoByDateAndSprintId(Date date, Long sprintId);
	
	String processVideoFileName(String filename, Long sprintId, Date date);
	boolean changeAndMoveVideoFileName(String oldName, String newName, Long sprintId, Long teamId);
	boolean deleteVideoFile(String filename, Long teamId, Long sprintId);
	
    List<MeetingVideo> getAllMeetingVideoInSprint(Sprint sprint);
    
    void deleteAllMeetingVideosInSprint(Sprint sprint);
	    
}
