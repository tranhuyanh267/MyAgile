package com.ant.myagile.model;

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
@Table(name = "Swimline")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Swimline {
	
	private static final long serialVersionUID = 1L;


	@Id
	@Column(name="id")
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long swinlineId;

	private String name;
    private String color;
    
    @ManyToOne
    @JoinColumn(name = "sprintId")
    Sprint sprint;

	public Swimline() {
	}

	public Long getSwinlineId() {
		return swinlineId;
	}

	public void setSwinlineId(Long swinlineId) {
		this.swinlineId = swinlineId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public Sprint getSprint() {
		return sprint;
	}

	public void setSprint(Sprint sprint) {
		this.sprint = sprint;
	}
}
