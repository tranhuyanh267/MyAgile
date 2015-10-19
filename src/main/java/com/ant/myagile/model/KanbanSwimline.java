package com.ant.myagile.model;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
@Entity
@Table(name = "KanbanSwimline")
public class KanbanSwimline {
	private static final long serialVersionUID = 1L;
	public static final String DEFAULT_COLOR="FFFFFF";
	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	private Long swimlineId;
	
	private String name;
	private String color;
	private Integer weightPoint;
	
	@ManyToOne
	@JoinColumn(name = "teamId")
	Team team;

	public Long getSwimlineId() {
		return swimlineId;
	}

	public void setSwimlineId(Long swimlineId) {
		this.swimlineId = swimlineId;
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

	public Team getTeam() {
		return team;
	}

	public void setTeam(Team team) {
		this.team = team;
	}

	public Integer getWeightPoint() {
		if(weightPoint == null){
			return 0;
		}
		return weightPoint;
	}

	public void setWeightPoint(Integer weightPoint) {
		this.weightPoint = weightPoint;
	}
	
}
