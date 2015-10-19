package com.ant.myagile.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

import org.hibernate.annotations.GenericGenerator;

@Entity(name="DisappearTaskConfiguration")
public class DisappearTaskConfiguration implements Serializable {
	
	@Id
	@Column(name="id")
    @GenericGenerator(name = "increment", strategy = "increment")
	@GeneratedValue(generator = "increment")
	private Long id;
	
	@JoinColumn(name="kanbanStatusId", unique=true)
	@OneToOne
	private KanbanStatus kanbanStatus;
	
	@Column(name="week")
	private int week;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public KanbanStatus getKanbanStatus() {
		return kanbanStatus;
	}

	public void setKanbanStatus(KanbanStatus kanbanStatus) {
		this.kanbanStatus = kanbanStatus;
	}

	public int getWeek() {
		return week;
	}

	public void setWeek(int week) {
		this.week = week;
	}	
	
	

}
