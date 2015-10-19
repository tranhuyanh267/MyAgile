package com.ant.myagile.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "MemberIssue")
public class MemberIssue implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	private Long memberissueId;

	@ManyToOne
	@JoinColumn(name = "memberId", nullable = false)
	private Member member;

	@ManyToOne
	@JoinColumn(name = "issueId", nullable = false)
	private KanbanIssue kanbanIssue;

	public MemberIssue(){
	}
	
	public MemberIssue(Member member, KanbanIssue issue) {
		this.member = member;
		this.kanbanIssue = issue;
	}

	public Long getMemberissueId() {
		return memberissueId;
	}

	public void setMemberissueId(Long memberissueId) {
		this.memberissueId = memberissueId;
	}

	public Member getMember() {
		return member;
	}

	public void setMember(Member member) {
		this.member = member;
	}

	public KanbanIssue getKanbanIssue() {
		return kanbanIssue;
	}

	public void setKanbanIssue(KanbanIssue kanbanIssue) {
		this.kanbanIssue = kanbanIssue;
	}
}
