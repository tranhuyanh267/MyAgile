package com.ant.myagile.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@Table(name = "Holiday")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Holiday implements Serializable {
	private static final long serialVersionUID = 1L;
	
	public static enum StatusType {
    	MORNING,
    	AFTERNOON,
    	FULLDAY
	}
	
	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	private Long holidayId;
	@Column(columnDefinition="TEXT")
	private String reason;
	
	@ManyToOne
	@JoinColumn(name = "memberId")
	private Member member; 
	
	@ManyToOne
    @JoinColumn(name = "sprintId")
    private Sprint sprint;
	
	private Date leaveDate;
	
	@Enumerated(EnumType.STRING)
    private StatusType leaveType;
	
	public Date getLeaveDate() {
		return leaveDate;
	}

	public void setLeaveDate(Date leaveDate) {
		this.leaveDate = leaveDate;
	}

	public Long getHolidayId() {
		return holidayId;
	}

	public void setHolidayId(Long holidayId) {
		this.holidayId = holidayId;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}
	
	public Member getMember() {
		return member;
	}

	public void setMember(Member member) {
		this.member = member;
	}

	public Sprint getSprint() {
		return sprint;
	}

	public void setSprint(Sprint sprint) {
		this.sprint = sprint;
	}
	
    public StatusType getLeaveType() {
		return leaveType;
	}

	public void setLeaveType(StatusType leaveType) {
		this.leaveType = leaveType;
	}
	
}
