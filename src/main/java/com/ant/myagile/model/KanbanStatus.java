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
@Entity
@Table(name = "KanbanStatus")
public class KanbanStatus {
	private static final long serialVersionUID = 1L;
	public static enum StatusType {
		START, IN_PROGRESS, DONE, ACCEPTED_SHOW, ACCEPTED_HIDE
	}
	public final static int DEFAULT_WEIGHT_IN_PROCESS=1;
	public final static String DEFAULT_COLOR="FFFFFF";
	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	private Long statusId;
	@ManyToOne
	@JoinColumn(name = "teamId")
	Team team;
	@Enumerated(EnumType.STRING)
	private StatusType type;
	private String name;
	private String color;
	private Boolean columnDone;
	private Integer weightPoint;
	@Column(name = "width", columnDefinition="Integer default '25'")
	private Integer width;
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((statusId == null) ? 0 : statusId.hashCode());
		return result;
	}

	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		KanbanStatus other = (KanbanStatus) obj;
		if (statusId == null) {
			if (other.getStatusId() != null)
				return false;
		} else if (!statusId.equals(other.getStatusId()))
			return false;
		return true;
	}
	
	public Long getStatusId() {
		return statusId;
	}

	public void setStatusId(Long statusId) {
		this.statusId = statusId;
	}

	public Team getTeam() {
		return team;
	}


	public void setTeam(Team team) {
		this.team = team;
	}


	public StatusType getType() {
		return type;
	}

	public void setType(StatusType type) {
		this.type = type;
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

	public Boolean getColumnDone() {
		return columnDone;
	}

	public void setColumnDone(Boolean columnDone) {
		this.columnDone = columnDone;
	}

	public Integer getWeightPoint() {
		return weightPoint;
	}

	public void setWeightPoint(Integer weightPoint) {
		this.weightPoint = weightPoint;
	}

	public Integer getWidth() {
		return width;
	}
	
	public void setWidth(Integer width) {
		this.width = width;
	}
}