package com.ant.myagile.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@Table(name = "MeetingVideo")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class MeetingVideo implements Serializable {

    private static final long serialVersionUID = 1L;

	@Id
	@Column(name="id")
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long meetingVideoId;

    @Column(nullable = false)
    private String videoFileName;

    @Column(nullable = false)
    private Date meetingDate;

    @ManyToOne
    @JoinColumn(name = "sprintId")
    private Sprint sprint;

    public Long getMeetingVideoId() {
        return meetingVideoId;
    }

    public void setMeetingVideoId(Long meetingVideoId) {
        this.meetingVideoId = meetingVideoId;
    }

    public String getVideoFileName() {
        return videoFileName;
    }

    public void setVideoFileName(String videoFileName) {
        this.videoFileName = videoFileName;
    }

    public Date getMeetingDate() {
        return meetingDate;
    }

    public void setMeetingDate(Date meetingDate) {
        this.meetingDate = meetingDate;
    }

    public Sprint getSprint() {
        return sprint;
    }

    public void setSprint(Sprint sprint) {
        this.sprint = sprint;
    }

}
