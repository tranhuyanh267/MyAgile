package com.ant.myagile.model;

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
@Table(name = "LastUserStoryIndex")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class LastUserStoryIndex {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
	
	@ManyToOne
    @JoinColumn(name = "projectId", nullable=false)
    private Project project;

	@Column(name="lastUserStoryIndex", nullable=false)
    private Long lastUserStorySortId;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	public Long getLastUserStorySortId() {
		return lastUserStorySortId;
	}

	public void setLastUserStorySortId(Long lastUserStorySortId) {
		this.lastUserStorySortId = lastUserStorySortId;
	}

}
