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
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name="Topic")
public class Topic implements Serializable{

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="id")
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	private long topicId;
	
	@ManyToOne(targetEntity=Wiki.class)
	@JoinColumn(name="wikiId")
	private Wiki wiki;
	
	private String title;
	
	
	@Column(columnDefinition="TEXT")
	private String content;
	
	private Date lastModifyDate;
	
	@OneToOne(targetEntity=Member.class)
	@JoinColumn(name="memberId")
	private Member lastModifyMember;

	public long getTopicId() {
		return topicId;
	}

	public void setTopicId(long topicId) {
		this.topicId = topicId;
	}

	public Wiki getWiki() {
		return wiki;
	}

	public void setWiki(Wiki wiki) {
		this.wiki = wiki;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
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
