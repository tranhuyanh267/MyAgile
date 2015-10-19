package com.ant.myagile.service;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.ant.myagile.service.impl.MeetingVideoServiceImpl;
import com.ant.myagile.utils.MyAgileFileUtils;


public class MeetingVideoServiceImplTest extends TestCase {

	private static final String TEMP_FOLDER_PATH = MyAgileFileUtils.getStorageLocation("myagile.upload.temp.location");
	private static final String MEETING_VIDEO_FOLDER = "meeting_videos";
	private MeetingVideoServiceImpl meetingVideoServiceImpl;
	
	@Before
	public void setUp() throws IOException {
		this.meetingVideoServiceImpl = new MeetingVideoServiceImpl();
	}
	
	@Test
	public void testMeetingVideoURL() {
		String filename = "abc.mp4";
		String t = "0";
		String sp = "35";
		assertEquals("/opt/myagile/data/teams/T0/sprints/SP35/meeting_videos/abc.mp4", MeetingVideoServiceImpl.meetingVideoURL(t, sp, filename));
	}
	
	@Test
	public void testChangeAndMoveVideoFileName() throws IOException {
		File tempFile = new File(TEMP_FOLDER_PATH+"/"+MEETING_VIDEO_FOLDER+"/temp.mp4");
		if(!tempFile.exists()) {
			tempFile.mkdirs();
			tempFile.createNewFile();
		} 
		
		Long teamId = 0L;
		Long sprintId = 10L;
		String newName = "tempNew.mp4";

		assertEquals(true, meetingVideoServiceImpl.changeAndMoveVideoFileName("temp.mp4", newName, sprintId, teamId));
	}
	
	@Test
	public void testDeleteVideoFile() throws IOException {
		File testDeleteFile = new File("/opt/myagile/data/teams/T0/sprints/SP10/meeting_videos/testDeleteFile.mp4");
		
		if(!testDeleteFile.exists()) {
			testDeleteFile.mkdirs();
			testDeleteFile.createNewFile();
		}
		String filename = "testDeleteFile.mp4";
		Long teamId = 0L;
		Long sprintId = 10L;
		meetingVideoServiceImpl.deleteVideoFile(filename, teamId, sprintId);
		assertEquals(true, !testDeleteFile.exists());
	}
	
	
	@After
	public void tearDown() throws IOException {
		File tempFolder = new File("/opt/myagile/data/teams/T0");	
		if(tempFolder.exists()) {
			org.apache.commons.io.FileUtils.deleteDirectory(tempFolder);
		} 
	}
}
