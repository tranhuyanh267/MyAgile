package com.ant.myagile.webservices;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;

import com.ant.myagile.model.MeetingVideo;
import com.ant.myagile.model.Member;
import com.ant.myagile.service.MeetingVideoService;
import com.ant.myagile.service.MemberService;
import com.ant.myagile.service.impl.MeetingVideoServiceImpl;
import com.ant.myagile.utils.MyAgileFileUtils;
import com.ant.myagile.utils.SpringContext;
import com.ant.myagile.utils.Utils;
import com.ant.myagile.utils.WebServicesUtils;
import com.google.gson.JsonObject;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;

@Path("/")
public class AndroidWS {
	private static final Logger LOGGER = Logger.getLogger(AndroidWS.class);
	private final String fileUploadTempDir = MyAgileFileUtils.getStorageLocation("myagile.upload.temp.location");
	private final String meetingVideoFolder = "/meeting_videos/";
	private final MeetingVideoServiceImpl meetingVideoService = SpringContext.getApplicationContext().getBean(MeetingVideoServiceImpl.class);
	private final String fileDir = fileUploadTempDir + meetingVideoFolder;
	private String filename;

	@POST
	@Path("login")
	public String checkExistedLdapUsername(@FormParam("email") String email, @FormParam("password") String password) {
		try {
			MemberService memberService = SpringContext.getApplicationContext().getBean(MemberService.class);
			Member member = memberService.findMemberByEmail(email);
			String encodedPassword = SpringContext.getApplicationContext().getBean(Utils.class).encodePassword(password);
			if (member.getPassword().equals(encodedPassword)) {
				return returnData(member.getUsername());
			}
			return "false";
		} catch (Exception e) {
			return "false";
		}
	}

	@POST
	@Path("getdata")
	public String checkExistedLdapUsername(@FormParam("email") String email) {
		try {
			MemberService memberService = SpringContext.getApplicationContext().getBean(MemberService.class);
			Member member = memberService.findMemberByEmail(email);
			return returnData(member.getUsername());

		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			return "false";
		}
	}

	@GET
	@Path("test")
	public String test() {
		return "100";
	}

	private String returnData(String username) {
		JsonObject response = new JsonObject();
		response.add("teams", WebServicesUtils.getTeamByUsername(username));
		response.add("sprints", WebServicesUtils.getSprintByTeam(username));
		return response.toString();
	}

	@POST
	@Path("checkvideoexists")
	public String checkVideoExisted(@FormParam("sprintId") String sprintId, @FormParam("meetingDate") String meetingDate) {
		try {
			MeetingVideoService meetingVideoService = SpringContext.getApplicationContext().getBean(MeetingVideoService.class);
			Date meetingDateObject = WebServicesUtils.formatter.parse(meetingDate);
			MeetingVideo mv = meetingVideoService.findMeetingVideoByDateAndSprintId(meetingDateObject, Long.valueOf(sprintId));
			if (mv == null) {
				return "false";
			} else {
				return "true";
			}

		} catch (Exception e) {
			return "error";
		}
	}

	@POST
	@Path("upload")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public String uploadFile(@FormDataParam("sprintId") String sprintId, @FormDataParam("teamId") String teamId, @FormDataParam("date") String date,
			@FormDataParam("file") InputStream uploadedInputStream, @FormDataParam("file") FormDataContentDisposition fileDetail) {
		try {
			Date dateOfSprint = WebServicesUtils.formatter.parse(date);
			filename = fileDetail.getFileName();
			writeToFile(uploadedInputStream, fileDir + fileDetail.getFileName());
			Calendar calendar = new GregorianCalendar();
			calendar.clear();
			calendar.setTime(dateOfSprint);
			handleFile(fileDetail.getFileName(), sprintId, calendar.getTime(), teamId);
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
		}
		return "200";
	}

	private void handleFile(String filename, String sprintIdStr, Date meetingDate, String teamIdStr) throws Exception {
		Long sprintId = Long.parseLong(sprintIdStr);
		Long teamId = Long.parseLong(teamIdStr);
		String oldFileName = filename;
		String newFileName = meetingVideoService.processVideoFileName(oldFileName, sprintId, meetingDate);
		MeetingVideo meetingVideo = meetingVideoService.findMeetingVideoByDateAndSprintId(meetingDate, sprintId);
		if (meetingVideo == null) {
			meetingVideoService.changeAndMoveVideoFileName(oldFileName, newFileName, sprintId, teamId);
			meetingVideoService.addMeetingVideo(newFileName, meetingDate, sprintId);
		} else {
			MeetingVideo mv = meetingVideoService.findMeetingVideoByDateAndSprintId(meetingDate, sprintId);
			meetingVideoService.deleteVideoFile(mv.getVideoFileName(), teamId, sprintId);
			meetingVideoService.changeAndMoveVideoFileName(oldFileName, newFileName, sprintId, teamId);
			meetingVideoService.updateMeetingVideo(newFileName, meetingDate, sprintId);
		}
	}

	// save uploaded file to new location
	private void writeToFile(InputStream uploadedInputStream, String uploadedFileLocation) {
		try {
			int read = 0;
			byte[] bytes = new byte[1024];

			File dstFile = new File(fileDir);
			if (!dstFile.exists()) {
				dstFile.mkdirs();
			}
			File dst = new File(fileDir + "/" + filename);
			OutputStream out = new FileOutputStream(dst);
			while ((read = uploadedInputStream.read(bytes)) != -1) {
				out.write(bytes, 0, read);
			}
			out.flush();
			out.close();
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
		}
	}

}
