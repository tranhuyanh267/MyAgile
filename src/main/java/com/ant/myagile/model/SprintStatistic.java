package com.ant.myagile.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@Table(name = "SprintStatistic")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class SprintStatistic implements Serializable {

    private static final long serialVersionUID = 1L;   

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long sprintStatisticId;

    @OneToOne
	@JoinColumn(name = "sprintId")
    private Sprint sprint;
   
    private int sprintSize;
    
    private String unitSprintSize = "week";
    
    private int teamSize;
    
    private String unitTeamSize = "developer";
    
    private float availableManDay;
    
    private String unitManDay = "day";
    
    private float pointPlan;

    private float pointDelivered;
    
    private String unitPoint = "point";
    
    private int userStoryPlan;
    
    private int userStoryDelivered;
    
    private String unitStory = "story";
    
    private float velocityOfSprintPlan;
    
    private float velocityOfSprintDelivered;
    
    private String unitVelocity = "P/P/D";
    
    @Column(columnDefinition="TEXT")
    private String note;

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public Sprint getSprint() {
		return sprint;
	}

	public void setSprint(Sprint sprint) {
		this.sprint = sprint;
	}

	public int getSprintSize() {
		return sprintSize;
	}

	public void setSprintSize(int sprintSize) {
		this.sprintSize = sprintSize;
	}

	public int getTeamSize() {
		return teamSize;
	}

	public void setTeamSize(int teamSize) {
		this.teamSize = teamSize;
	}

	public float getAvailableManDay() {
		return availableManDay;
	}

	public void setAvailableManDay(float availableManDay) {
		this.availableManDay = availableManDay;
	}

	public float getPointPlan() {
		return pointPlan;
	}

	public void setPointPlan(float pointPlan) {
		this.pointPlan = pointPlan;
	}

	public String getUnitSprintSize() {
		return unitSprintSize;
	}

	public void setUnitSprintSize(String unitSprintSize) {
		this.unitSprintSize = unitSprintSize;
	}

	public String getUnitTeamSize() {
		return unitTeamSize;
	}

	public void setUnitTeamSize(String unitTeamSize) {
		this.unitTeamSize = unitTeamSize;
	}

	public String getUnitManDay() {
		return unitManDay;
	}

	public void setUnitManDay(String unitManDay) {
		this.unitManDay = unitManDay;
	}

	public String getUnitPoint() {
		return unitPoint;
	}

	public void setUnitPoint(String unitPoint) {
		this.unitPoint = unitPoint;
	}

	public String getUnitVelocity() {
		return unitVelocity;
	}

	public void setUnitVelocity(String unitVelocity) {
		this.unitVelocity = unitVelocity;
	}

	public String getUnitStory() {
		return unitStory;
	}

	public void setUnitStory(String unitStory) {
		this.unitStory = unitStory;
	}

    public Long getSprintStatisticId() {
        return sprintStatisticId;
    }

    public void setSprintStatisticId(Long sprintStatisticId) {
        this.sprintStatisticId = sprintStatisticId;
    }

	public float getPointDelivered() {
		return pointDelivered;
	}

	public void setPointDelivered(float pointDelivered) {
		this.pointDelivered = pointDelivered;
	}

	public int getUserStoryPlan() {
		return userStoryPlan;
	}

	public void setUserStoryPlan(int userStoryPlan) {
		this.userStoryPlan = userStoryPlan;
	}

	public int getUserStoryDelivered() {
		return userStoryDelivered;
	}

	public void setUserStoryDelivered(int userStoryDelivered) {
		this.userStoryDelivered = userStoryDelivered;
	}

	public float getVelocityOfSprintPlan() {
		return velocityOfSprintPlan;
	}

	public void setVelocityOfSprintPlan(float velocityOfSprintPlan) {
		this.velocityOfSprintPlan = velocityOfSprintPlan;
	}

	public float getVelocityOfSprintDelivered() {
		return velocityOfSprintDelivered;
	}

	public void setVelocityOfSprintDelivered(float velocityOfSprintDelivered) {
		this.velocityOfSprintDelivered = velocityOfSprintDelivered;
	}
}
