package com.ant.myagile.service.impl;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ant.myagile.dao.MeetingVideoDao;
import com.ant.myagile.dao.SprintDao;
import com.ant.myagile.model.MeetingVideo;
import com.ant.myagile.model.Sprint;
import com.ant.myagile.service.MeetingVideoService;
import com.ant.myagile.utils.MyAgileFileUtils;
import com.itextpdf.text.pdf.PdfStructTreeController.returnType;

@Service("meetingVideoService")
public class MeetingVideoServiceImpl implements MeetingVideoService {
	
	@Autowired
	private MeetingVideoDao meetingVideoDao;
	
	@Autowired
	private SprintDao sprintDao;
	
	private static final String ROOT_FOLDER_PATH = MyAgileFileUtils.getRootStorageLocation();
	private static final String TEMP_FOLDER_PATH = MyAgileFileUtils.getStorageLocation("myagile.upload.temp.location");
	private static final String MEETING_VIDEO_FOLDER = "meeting_videos";

	
	@Override
	public MeetingVideo addMeetingVideo(String originalFilename, Date meetingDate, Long sprintId) throws IllegalStateException, IOException {
	
		Sprint sprint = sprintDao.findSprintById(sprintId);

		if(sprint != null) {
			MeetingVideo meetingVideo = new MeetingVideo();
			meetingVideo.setMeetingDate(meetingDate);
			meetingVideo.setVideoFileName(originalFilename);
			meetingVideo.setSprint(sprint);
			return meetingVideoDao.save(meetingVideo);
		} else {
			return null;
		}
	}

	@Override
	public MeetingVideo findMeetingVideoByDateAndSprintId(Date date,
			Long sprintId) {
		return meetingVideoDao.findMeetingVideoByDateAndSprintId(date, sprintId);
	}

	@Override
	public String processVideoFileName(String filename, Long sprintId, Date date) {
		String ext = FilenameUtils.getExtension(filename);
		StringBuilder sb = new StringBuilder();
		sb.append(sprintId+"_"+date.getTime()+"_"+new Date().getTime()+"."+ext);
		return sb.toString();
	}

	@Override
	public boolean changeAndMoveVideoFileName(String oldName, String newName, Long sprintId, Long teamId) {
		String filePath = TEMP_FOLDER_PATH+"/"+MEETING_VIDEO_FOLDER+"/"+oldName;
		File oldFile = new File(filePath);
		boolean result = false;
		String newFilePath = ROOT_FOLDER_PATH+"/teams/T"+teamId+"/sprints/SP"+sprintId+"/"+MEETING_VIDEO_FOLDER+"/";
		
		if(oldFile.exists()) {
			File newFile = new File(newFilePath);
			if(!newFile.exists()) {
				newFile.mkdirs();
			}
			result = oldFile.renameTo(new File(newFilePath+"/"+newName));
		}
		return result;
	}

	@Override
	public MeetingVideo updateMeetingVideo(String originalFilename,
			Date meetingDate, Long sprintId) {
		
		MeetingVideo mv = meetingVideoDao.findMeetingVideoByDateAndSprintId(meetingDate, sprintId);
		Sprint sprint = sprintDao.findSprintById(sprintId);
		if(mv != null && sprint != null) {
			mv.setMeetingDate(meetingDate);
			mv.setVideoFileName(originalFilename);
			mv.setSprint(sprint);
			return meetingVideoDao.update(mv);
		} else {
			return null;
		}

	}

	@Override
	public boolean deleteVideoFile(String filename, Long teamId, Long sprintId) {
		String filePath = ROOT_FOLDER_PATH+"/teams/T"+teamId+"/sprints/SP"+sprintId+"/"+MEETING_VIDEO_FOLDER+"/"+filename;
		File f = new File(filePath);
		if(f.exists()) {
			return f.delete();
		} else {
			return false;
		}
	}
	
	public static String meetingVideoURL(String teamId, String sprintId, String filename) {
		return ROOT_FOLDER_PATH+"/teams/T"+teamId+"/sprints/SP"+sprintId+"/meeting_videos/"+filename;
	}

	public MeetingVideoDao getMeetingVideoDao() {
		return meetingVideoDao;
	}

	public void setMeetingVideoDao(MeetingVideoDao meetingVideoDao) {
		this.meetingVideoDao = meetingVideoDao;
	}

	@Override
	public List<MeetingVideo> getAllMeetingVideoInSprint(Sprint sprint) {
		return meetingVideoDao.getAllMeetingVideoInSprint(sprint);
	}

	@Override
	public void deleteAllMeetingVideosInSprint(Sprint sprint) {
		List<MeetingVideo> meetingVideos = meetingVideoDao.getAllMeetingVideoInSprint(sprint);
		if(meetingVideos.size() > 0){
			for (MeetingVideo meetingVideo : meetingVideos) {
				meetingVideoDao.delete(meetingVideo);
			}
			}
		
	}


}
