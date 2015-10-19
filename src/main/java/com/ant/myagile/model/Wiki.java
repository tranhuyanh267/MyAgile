package com.ant.myagile.model;

import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonSubTypes.Type;

@Entity
@Table(name="Wiki")
public class Wiki {
	
	@Id
	@Column(name="id")
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	private long wikiId;
	
	@OneToOne(targetEntity=Team.class)
	@JoinColumn(name="teamId")
	private Team team;
	
	@Column(columnDefinition="TEXT")
	private String content;
	
	private String status;
	
	@OneToMany
	@JoinColumn(name="wiki")
	private List<Topic> topics;
	
	private Date lastModifyDate;
	
	@OneToOne
	@JoinColumn(name="memberId")
	private Member lastModifyMember;

	public long getWikiId() {
		return wikiId;
	}

	public void setWikiId(long wikiId) {
		this.wikiId = wikiId;
	}

	public Team getTeam() {
		return team;
	}

	public void setTeam(Team team) {
		this.team = team;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public List<Topic> getTopics() {
		return topics;
	}

	public void setTopics(List<Topic> topics) {
		this.topics = topics;
	}

	public Date getLastModifyDate() {
		return lastModifyDate;
	}

	public void setLastModifyDate(Date lastModifyDate) {
		this.lastModifyDate = lastModifyDate;
	}

	public Member getLastModifyMember() {
		return lastModifyMember;
	}

	public void setLastModifyMember(Member lastModifyMember) {
		this.lastModifyMember = lastModifyMember;
	}
	
	
	
}
