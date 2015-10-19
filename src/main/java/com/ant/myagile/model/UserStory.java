package com.ant.myagile.model;

import java.io.Serializable;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

@Entity
@Table(name = "UserStory")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class UserStory implements Serializable {
    
	private static final long serialVersionUID = 1L;

	public static enum StatusType {
    	TODO,
    	IN_PROGRESS,
    	DONE,
    	VOID
	}
	
	public static enum PriorityType {
    	MUST,
    	SHOULD,
    	COULD,
    	WONT,
    	NONE
	}
	
	@Id
	@Column(name="id")
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long userStoryId;

	@Column(nullable=false)
    private Long sortId;

	@Column(nullable=false)
    private String name;

    @Column(columnDefinition="TEXT")
    private String description;
    
    @Column(columnDefinition="TEXT")
    private String note;
   
	@Column
    private int value = 0;
    
    @Column
    private int risk = 0;
    
    @Enumerated(EnumType.STRING)
    private PriorityType priority;

    @Enumerated(EnumType.STRING)
    private StatusType status;

	@OneToMany(mappedBy="userStory", cascade = CascadeType.ALL, targetEntity = Issue.class)
    private List<Issue> issues;

	@OneToMany(mappedBy="userStory", cascade = CascadeType.ALL, targetEntity = KanbanIssue.class)
    private List<KanbanIssue> kanbanIssues;
	
	@ManyToOne(cascade={CascadeType.DETACH, CascadeType.MERGE,
            CascadeType.PERSIST, CascadeType.REFRESH}, targetEntity=Project.class)
    @JoinColumn(name = "projectId")
    private Project project;
	
    @Enumerated(EnumType.STRING)
    private StatusType previousStatus;

    public Long getUserStoryId() {
        return userStoryId;
    }

    public void setUserStoryId(Long userStoryId) {
        this.userStoryId = userStoryId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}
    
    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public List<Issue> getIssues() {
        return issues;
    }

    public void setIssues(List<Issue> issues) {
        this.issues = issues;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }
    
	public Long getSortId() {
		return sortId;
	}

	public void setSortId(Long sortId) {
		this.sortId = sortId;
	}

	public StatusType getStatus() {
		return status;
	}

	public void setStatus(StatusType status) {
		this.status = status;
	}

	public int getRisk() {
		return risk;
	}

	public void setRisk(int risk) {
		this.risk = risk;
	}

	public PriorityType getPriority() {
		return priority;
	}

	public void setPriority(PriorityType priority) {
		this.priority = priority;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((userStoryId == null) ? 0 : userStoryId.hashCode());
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
		UserStory other = (UserStory) obj;
		if (userStoryId == null) {
			if (other.userStoryId != null)
				return false;
		} else if (!userStoryId.equals(other.userStoryId))
			return false;
		return true;
	}

	public List<KanbanIssue> getKanbanIssues() {
		return kanbanIssues;
	}

	public void setKanbanIssues(List<KanbanIssue> kanbanIssues) {
		this.kanbanIssues = kanbanIssues;
	}

	public StatusType getPreviousStatus() {
		return previousStatus;
	}

	public void setPreviousStatus(StatusType previousStatus) {
		this.previousStatus = previousStatus;
	}
	
}
