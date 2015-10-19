package com.ant.myagile.dao;

import java.util.Date;
import java.util.List;

import com.ant.myagile.model.MeetingVideo;
import com.ant.myagile.model.Sprint;

public interface MeetingVideoDao {
    MeetingVideo save(MeetingVideo meetingVideo);

    MeetingVideo findMeetingVideoByDateAndSprintId(Date date, Long sprintId);

    MeetingVideo update(MeetingVideo meetingVideo);
    
    List<MeetingVideo> getAllMeetingVideoInSprint(Sprint sprint);
    
    boolean delete(MeetingVideo meetingVideo);
}
