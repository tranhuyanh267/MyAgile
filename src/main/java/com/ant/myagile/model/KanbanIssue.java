package com.ant.myagile.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.LazyCollection;

@Entity
@Table(name = "KanbanIssue")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class KanbanIssue implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
	@Column(name="id")
    @GenericGenerator(name = "increment", strategy = "increment")
	@GeneratedValue(generator = "increment")
    private Long issueId;

    private String type;

    @Column(nullable = false)
    private String subject;

    @Column(columnDefinition="TEXT")
    private String description;

    @Column(columnDefinition="TEXT")
    private String note;
    
	private String priority;

    private String remain;
    private String estimate;
    
    private Boolean columnDone;
    
    @Column(columnDefinition = "varchar(1) default '1'")
    private String pointFormat;
    

    @ManyToOne(cascade = { CascadeType.DETACH, CascadeType.MERGE,
            CascadeType.PERSIST, CascadeType.REFRESH }, targetEntity = UserStory.class)
    @JoinColumn(name = "userStoryId")
    private UserStory userStory;
    
    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "issueId")
    private List<KanbanPointRemain> pointRemains;
    
    @ManyToOne
    @JoinColumn(name = "kanbanStatusId")
    private KanbanStatus kanbanStatus;
    
    @ManyToOne
    @JoinColumn(name = "kanbanSwimlineId")
    private KanbanSwimline kanbanSwimline;
    
    @ManyToOne
    @JoinColumn(name = "teamId")
    private Team team;
    
    @Column(name="disappearDate")
    private String disappearDate;
    
    @Column(name="isSubIssue")
    private Boolean isSubIssue;
    
    @Column
    private Long issueOfLastSprint;
    
    @Column(name="isVoid")
    private boolean isVoid;
    
	public Long getIssueId() {
		return issueId;
	}

	public void setIssueId(Long issueId) {
		this.issueId = issueId;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
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

	public String getPriority() {
		return priority;
	}

	public void setPriority(String priority) {
		this.priority = priority;
	}

	public String getRemain() {
		return remain;
	}

	public void setRemain(String remain) {
		this.remain = remain;
	}

	public String getEstimate() {
		return estimate;
	}

	public void setEstimate(String estimate) {
		this.estimate = estimate;
	}

	public Boolean getColumnDone() {
		return columnDone;
	}

	public void setColumnDone(Boolean columnDone) {
		this.columnDone = columnDone;
	}

	public String getPointFormat() {
		return pointFormat;
	}

	public void setPointFormat(String pointFormat) {
		this.pointFormat = pointFormat;
	}

	public UserStory getUserStory() {
		return userStory;
	}

	public void setUserStory(UserStory userStory) {
		this.userStory = userStory;
	}

	public List<KanbanPointRemain> getPointRemains() {
		return pointRemains;
	}

	public void setPointRemains(List<KanbanPointRemain> pointRemains) {
		this.pointRemains = pointRemains;
	}

	public KanbanStatus getKanbanStatus() {
		return kanbanStatus;
	}

	public void setKanbanStatus(KanbanStatus kanbanStatus) {
		this.kanbanStatus = kanbanStatus;
	}

	public KanbanSwimline getKanbanSwimline() {
		return kanbanSwimline;
	}

	public void setKanbanSwimline(KanbanSwimline kanbanSwimline) {
		this.kanbanSwimline = kanbanSwimline;
	}

	public Team getTeam() {
		return team;
	}

	public void setTeam(Team team) {
		this.team = team;
	}

	public Long getDisplayIssueId() {
		if(this.userStory != null) {
			return this.userStory.getUserStoryId(); 
		} else {
			return this.issueId;
		}
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((issueId == null) ? 0 : issueId.hashCode());
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
		KanbanIssue other = (KanbanIssue) obj;
		if (issueId == null) {
			if (other.issueId != null)
				return false;
		} else if (!issueId.equals(other.issueId))
			return false;
		return true;
	}

	public String getDisappearDate() {
		return disappearDate;
	}

	public void setDisappearDate(String disappearDate) {
		this.disappearDate = disappearDate;
	}

	public Boolean getIsSubIssue() {
		return isSubIssue;
	}

	public void setIsSubIssue(Boolean isSubIssue) {
		this.isSubIssue = isSubIssue;
	}

	public Long getIssueOfLastSprint() {
		return issueOfLastSprint;
	}

	public void setIssueOfLastSprint(Long issueOfLastSprint) {
		this.issueOfLastSprint = issueOfLastSprint;
	}

	public boolean isVoid() {
		return isVoid;
	}

	public void setVoid(boolean isVoid) {
		this.isVoid = isVoid;
	}
	
	
}
