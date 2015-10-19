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
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@Table(name = "Issue")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Issue implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	private Long issueId;

	@Transient
	private Boolean isParent = false;

	private String type;

	@Column(nullable = false)
	private String subject;

	@Column(columnDefinition = "TEXT")
	private String description;

	@Column(columnDefinition = "TEXT")
	private String note;

	private String priority;

	private String remain;
	private String estimate;
	private Boolean isSwimLine;

	@Column(columnDefinition = "varchar(1) default '1'")
	private String pointFormat;

	@ManyToOne
	@JoinColumn(name = "assignedId")
	private Member assigned;

	@ManyToOne(cascade = { CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH }, targetEntity = UserStory.class)
	@JoinColumn(name = "userStoryId")
	private UserStory userStory;

	@ManyToOne
	@JoinColumn(name = "parentId")
	private Issue parent;

	@OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
	private List<Issue> childs;

	@ManyToOne
	@JoinColumn(name = "sprintId")
	private Sprint sprint;

	@ManyToOne
	@JoinColumn(name = "statusId")
	private Status status;

	@OneToMany(cascade = CascadeType.ALL)
	@JoinColumn(name = "issueId")
	private List<PointRemain> pointRemains;
	
	@Column(name = "oldId")
	private Long oldId;
	
	@Column(name = "createdDate")
	private Date createdDate;
	
	@Column(name = "isVoid")
	private boolean isVoid;
	
	private Integer orderIssue;

	public Long getIssueId() {
		return this.issueId;
	}

	public void setIssueId(Long issueId) {
		this.issueId = issueId;
	}

	public String getType() {
		return this.type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getSubject() {
		return this.subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getPriority() {
		return this.priority;
	}

	public void setPriority(String priority) {
		this.priority = priority;
	}

	public String getRemain() {
		return this.remain;
	}

	public void setRemain(String remain) {
		this.remain = remain;
	}

	public String getEstimate() {
		return this.estimate;
	}

	public void setEstimate(String estimate) {
		this.estimate = estimate;
	}

	public Member getAssigned() {
		return this.assigned;
	}

	public void setAssigned(Member assigned) {
		if (assigned == null) {
			this.assigned = null;
		} else {
			this.assigned = assigned;
		}

	}

	public UserStory getUserStory() {
		return this.userStory;
	}

	public void setUserStory(UserStory userStory) {
		this.userStory = userStory;
	}

	public Issue getParent() {
		return this.parent;
	}

	public void setParent(Issue parent) {
		this.parent = parent;
	}

	public Sprint getSprint() {
		return this.sprint;
	}

	public String getPointFormat() {
		return this.pointFormat;
	}

	public void setPointFormat(String pointFormat) {
		this.pointFormat = pointFormat;
	}

	public void setSprint(Sprint sprint) {
		this.sprint = sprint;
	}

	public Status getStatus() {
		return this.status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public List<PointRemain> getPointRemains() {
		return this.pointRemains;
	}

	public List<Issue> getChilds() {
		return childs;
	}

	public void setChilds(List<Issue> childs) {
		this.childs = childs;
	}

	public void setPointRemains(List<PointRemain> pointRemains) {
		this.pointRemains = pointRemains;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public Boolean getIsSwimLine() {
		return isSwimLine;
	}

	public void setIsSwimLine(Boolean isSwimLine) {
		this.isSwimLine = isSwimLine;
	}

	public Long getDisplayIssueId() {
		if (this.userStory != null) {
			return this.userStory.getUserStoryId();
		} else if (this.oldId == null) {
			return this.issueId;
		} else {
			return this.oldId;
		}
	}

	public Boolean getIsParent() {
		return isParent;
	}

	public void setIsParent(Boolean isParent) {
		this.isParent = isParent;
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
		Issue other = (Issue) obj;
		if (issueId == null) {
			if (other.issueId != null)
				return false;
		} else if (!issueId.equals(other.issueId))
			return false;
		return true;
	}

	public Long getOldId() {
		return oldId;
	}

	public void setOldId(Long oldId) {
		this.oldId = oldId;
	}

	public Date getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}

	public Integer getOrderIssue() {
		return orderIssue;
	}

	public void setOrderIssue(Integer orderIssue) {
		this.orderIssue = orderIssue;
	}

	public boolean isVoid() {
		return isVoid;
	}

	public void setVoid(boolean isVoid) {
		this.isVoid = isVoid;
	}
}
