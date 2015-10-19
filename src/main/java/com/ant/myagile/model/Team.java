package com.ant.myagile.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@Table(name = "Team")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Team implements Serializable {
	private static final long serialVersionUID = 1L;
 
	
	@Id
	@Column(name="id")
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	private Long teamId;

	private String teamName;
	private String logo;
	private String mailGroup;
	private String description;

	private Date establishedDate;
	private Date validTo;
	private String note;
	
	@OneToOne
	@JoinColumn(name="wikiId")
	private Wiki wiki;
	
	@ManyToOne
	@JoinColumn(name = "ownerId")
	private Member owner;

	@OneToMany(cascade = CascadeType.ALL)
	@JoinColumn(name = "teamId")
	private List<TeamMember> members;

	@OneToMany(cascade = CascadeType.ALL)
	@JoinColumn(name = "teamId")
	private List<TeamProject> projects;

	@OneToMany(cascade = CascadeType.ALL)
	@JoinColumn(name = "teamId")
	private List<Sprint> sprints;

	public Member getOwner() {
		return this.owner;
	}

	public List<TeamMember> getMembers() {
		return this.members;
	}

	public List<TeamProject> getProjects() {
		return this.projects;
	}

	public List<Sprint> getSprints() {
		return this.sprints;
	}

	public Long getTeamId() {
		return teamId;
	}

	public void setTeamId(Long teamId) {
		this.teamId = teamId;
	}

	public String getTeamName() {
		return teamName;
	}

	public void setTeamName(String teamName) {
		this.teamName = teamName;
	}

	public String getLogo() {
		return logo;
	}

	public void setLogo(String logo) {
		this.logo = logo;
	}

	public String getMailGroup() {
		return mailGroup;
	}

	public void setMailGroup(String mailGroup) {
		this.mailGroup = mailGroup;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Date getEstablishedDate() {
		return establishedDate;
	}

	public void setEstablishedDate(Date establishedDate) {
		this.establishedDate = establishedDate;
	}

	public void setOwner(Member owner) {
		this.owner = owner;
	}

	public void setMembers(List<TeamMember> teamMembers) {
		this.members = teamMembers;
	}

	public void setProjects(List<TeamProject> projects) {
		this.projects = projects;
	}

	public void setSprints(List<Sprint> sprints) {
		this.sprints = sprints;
	}

	public Wiki getWiki() {
		return wiki;
	}

	public void setWiki(Wiki wiki) {
		this.wiki = wiki;
	}

	@Override
	public String toString() {
		return this.teamName;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((teamId == null) ? 0 : teamId.hashCode());
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
		Team other = (Team) obj;
		if (teamId == null) {
			if (other.teamId != null)
				return false;
		} else if (!teamId.equals(other.teamId))
			return false;
		return true;
	}

	public Date getValidTo() {
		return validTo;
	}

	public void setValidTo(Date validTo) {
		this.validTo = validTo;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

}

	
