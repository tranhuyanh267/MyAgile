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

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@Table(name = "HistoryFieldChange")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class HistoryFieldChange implements Serializable {

	public HistoryFieldChange(History history, String fieldName, String oldValue, String newValue) {
		super();
		this.history = history;
		this.fieldName = fieldName;
		this.oldValue = oldValue;
		this.newValue = newValue;
	}

	public HistoryFieldChange() {
		super();
		// TODO Auto-generated constructor stub
	}

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	private Long historyFieldChangeId;

	@ManyToOne
	@JoinColumn(name = "historyId")
	private History history;

	private String fieldName;

	@Column(columnDefinition="TEXT")
	private String oldValue;

	@Column(columnDefinition="TEXT")
	private String newValue;

	public Long getHistoryFieldChangeId() {
		return historyFieldChangeId;
	}

	public void setHistoryFieldChangeId(Long historyFieldChangeId) {
		this.historyFieldChangeId = historyFieldChangeId;
	}

	public History getHistory() {
		return history;
	}

	public void setHistory(History history) {
		this.history = history;
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
