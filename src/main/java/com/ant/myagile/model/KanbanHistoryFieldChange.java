package com.ant.myagile.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "KanbanHistoryFieldChange")
public class KanbanHistoryFieldChange implements Serializable {

	private static final long serialVersionUID = 1L;
	
	//final field name
	public static final String KANBAN_ISSUE_SUBJECT = "Subject";
	public static final String KANBAN_ISSUE_DESCRIPTION = "Description";
	public static final String KANBAN_ISSUE_NOTE = "Note";
	public static final String KANBAN_ISSUE_STATUS = "Status";
	public static final String KANBAN_ISSUE_PRODUCT_BACKLOG_COLUM = "Product Backlog Column";
	public static final String KANBAN_ISSUE_ESTIMATE = "Estimate";
	public static final String KANBAN_ISSUE_REMAIN = "Remain";
	public static final String KANBAN_ISSUE_ASSIGN = "Assign";
	public static final String KANBAN_ISSUE_UNASSIGN = "Unassign";
	public static final String KANBAN_ISSUE_ATTACHMENT = "Attachment";
	public static final String KANBAN_ISSUE_TYPE = "Type";
	public static final String KANBAN_ISSUE_PRIORITY = "Priority";
	public static final String KANBAN_ISSUE_MEMBER_ISSUE = "Member Issue";

	@Id
	@GenericGenerator(name = "increment", strategy = "increment")
	@GeneratedValue(generator = "increment")
	@Column(name = "id")
	private Long id;

	@ManyToOne
	private KanbanHistory kanbanHistory;

	@Column
	private String fieldName;

	@Column(columnDefinition = "TEXT")
	private String oldValue;

	@Column(columnDefinition = "TEXT")
	private String newValue;
	
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
	
	public KanbanHistoryFieldChange() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public KanbanHistory getKanbanHistory() {
		return kanbanHistory;
	}

	public void setKanbanHistory(KanbanHistory kanbanHistory) {
		this.kanbanHistory = kanbanHistory;
	}

	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	public String getOldValue() {
		return oldValue;
	}

	public void setOldValue(String oldValue) {
		this.oldValue = oldValue;
	}

	public String getNewValue() {
		return newValue;
	}

	public void setNewValue(String newValue) {
		this.newValue = newValue;
	}

}
