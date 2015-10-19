package com.ant.myagile.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;


@Entity
@Table(name = "TeamProject", uniqueConstraints = @UniqueConstraint(columnNames = {
        "projectId", "teamId" }))
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class TeamProject implements Serializable {

    private static final long serialVersionUID = 1L;

	@Id
	@Column(name="id")
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long teamProjectid;

    @ManyToOne(cascade={CascadeType.DETACH, CascadeType.MERGE,
            CascadeType.PERSIST, CascadeType.REFRESH}, targetEntity=Project.class)
    @JoinColumn(name = "projectId")
    private Project project;

    @ManyToOne(cascade={CascadeType.DETACH, CascadeType.MERGE,
            CascadeType.PERSIST, CascadeType.REFRESH}, targetEntity=Team.class)
    @JoinColumn(name = "teamId")
    private Team team;

    private Date startDate;
    private Date endDate;

    public Team getTeam() {
        return this.team;
    }

    public Project getProject() {
        return this.project;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    public Long getTeamProjectid() {
        return teamProjectid;
    }

    public void setTeamProjectid(Long teamProjectid) {
        this.teamProjectid = teamProjectid;
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((project == null) ? 0 : project.hashCode());
		result = prime * result + ((team == null) ? 0 : team.hashCode());
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
		TeamProject other = (TeamProject) obj;
		if (project == null) {
			if (other.project != null)
				return false;
		} else if (!project.equals(other.project))
			return false;
		if (team == null) {
			if (other.team != null)
				return false;
		} else if (!team.equals(other.team))
			return false;
		return true;
	}
}
