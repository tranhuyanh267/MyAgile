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
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@Table(name = "KanbanPointRemain")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class KanbanPointRemain implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	private Long pointRemainId;

	@Column(nullable = false)
	private Date dateUpdate;

	@Column(nullable = false)
	private String pointRemain;

	@ManyToOne
	@JoinColumn(name = "issueId")
	private KanbanIssue kanbanissue;

	public Long getPointRemainId() {
		return pointRemainId;
	}

	public void setPointRemainId(Long pointRemainId) {
		this.pointRemainId = pointRemainId;
	}

	public Date getDateUpdate() {
		return dateUpdate;
	}

	public void setDateUpdate(Date dateUpdate) {
		this.dateUpdate = dateUpdate;
	}

	public String getPointRemain() {
		return pointRemain;
	}

	public void setPointRemain(String pointRemain) {
		this.pointRemain = pointRemain;
	}

	public KanbanIssue getKanbanissue() {
		return kanbanissue;
	}

	public void setKanbanissue(KanbanIssue kanbanissue) {
		this.kanbanissue = kanbanissue;
	}
}
