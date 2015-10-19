package com.ant.myagile.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

@Entity
@Table(name = "KanbanHistory")
public class KanbanHistory implements Serializable {

	private static final long serialVersionUID = 1L;

	public static enum ContainerType {
		KanbanIssue, UserStory
	}

	public static enum ActionType {
		Create, Update, Delete, Upfile, Deletefile
	}

	@Id
	@GenericGenerator(name = "increment", strategy = "increment")
	@GeneratedValue(generator = "increment")
	@Column(name = "id")
	private Long id;

	@Column
	private Long containerId;

	@Column
	@Enumerated(EnumType.STRING)
	private ContainerType containerType;

	@Column
	@Enumerated(EnumType.STRING)
	private ActionType actionType;

	@Column
	private Date dateCreated;

	@ManyToOne
	@JoinColumn(name = "authorId")
	private Member author;

	@OneToMany(mappedBy = "kanbanHistory", targetEntity = KanbanHistoryFieldChange.class)
	@Cascade(value = { org.hibernate.annotations.CascadeType.ALL,CascadeType.SAVE_UPDATE })
	@LazyCollection(LazyCollectionOption.FALSE)
	private List<KanbanHistoryFieldChange> kanbanHistoryFieldChanges;

	@Override
	public boolean equals(Object o) {
		if (o == null) {
			return false;
		}
		if (!(o instanceof KanbanHistoryFieldChange)) {
			return false;
		}
		KanbanHistoryFieldChange otherKanbanHistoryFieldChange = (KanbanHistoryFieldChange) o;
		return this.getId() == otherKanbanHistoryFieldChange.getId();
	}

	@Override
	public int hashCode() {
		return this.getId().intValue();
	}

	public KanbanHistory() {

	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getContainerId() {
		return containerId;
	}

	public void setContainerId(Long containerId) {
		this.containerId = containerId;
	}

	public ContainerType getContainerType() {
		return containerType;
	}

	public void setContainerType(ContainerType containerType) {
		this.containerType = containerType;
	}

	public ActionType getActionType() {
		return actionType;
	}

	public void setActionType(ActionType actionType) {
		this.actionType = actionType;
	}

	public Date getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	public Member getAuthor() {
		return author;
	}

	public void setAuthor(Member author) {
		this.author = author;
	}

	public List<KanbanHistoryFieldChange> getKanbanHistoryFieldChanges() {
		return kanbanHistoryFieldChanges;
	}

	public void setKanbanHistoryFieldChanges(
			List<KanbanHistoryFieldChange> kanbanHistoryFieldChanges) {
		this.kanbanHistoryFieldChanges = kanbanHistoryFieldChanges;
	}

}
