package com.ant.myagile.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@Table(name = "History")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class History implements Serializable {

    public History() {
		super();
		// TODO Auto-generated constructor stub
	}

	public History(Long containerId, String containerType, String actionType, Date createdOn, Member author) {
		super();
		this.containerId = containerId;
		this.containerType = containerType;
		this.actionType = actionType;
		this.createdOn = createdOn;
		this.author = author;
	}

	private static final long serialVersionUID = 1L;

    public static final String ISSUE_HISTORY = "Issue";
    public static final String PROJECT_HISTORY = "Project";
    public static final String TEAM_HISTORY = "Team";
    public static final String CREATE_ACTION = "Create";
    public static final String UPDATE_ACTION = "Update";
    public static final String DELETE_ACTION = "Delete";
    public static final String REMOVE_ACTION = "Remove";
    public static final String CREATE_SPRINT = "CreateSprint";
    public static final String ADD_ACTION = "Add";
    public static final String ADD_US_ACTION = "AddUs";
    public static final String DELETE_US_ACTION = "DeleteUs";
    public static final String ADD_FILE_US_ACTION = "AddFile";
    public static final String DELETE_FILE_US_ACTION = "DeleteFile";
    public static final String ADD_MEMBER_ACTION = "AddMember";   
    public static final String REMOVE_MEMBER_ACTION = "RemoveMember";
    public static final String ROLE_MEMBER_ACTION = "RoleMember";
    public static final String ASSIGN_ACTION = "Assign";
    public static final String REASSIGN_ACTION = "ReAssign";    
   
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long historyId;

    private Long containerId;

    private String containerType;

    /**
     * Defile what type of action: "Create", "Update", "Delete"
     */
    private String actionType;

    private Date createdOn;

    @ManyToOne
    @JoinColumn(name = "authorId")
    private Member author;
    
    @OneToMany(cascade = CascadeType.ALL,fetch = FetchType.EAGER)
    @JoinColumn(name = "historyId")
    private List<HistoryFieldChange> historyFieldChanges;

    public List<HistoryFieldChange> getHistoryFieldChanges() {
		return this.historyFieldChanges;
	}

	public void setHistoryFieldChanges(List<HistoryFieldChange> historyFieldChanges) {
		this.historyFieldChanges = historyFieldChanges;
	}

	public Long getContainerId() {
        return this.containerId;
    }

    public void setContainerId(Long containerId) {
        this.containerId = containerId;
    }

    public String getContainerType() {
        return this.containerType;
    }

    public void setContainerType(String containerType) {
        this.containerType = containerType;
    }

    public Date getCreatedOn() {
        return this.createdOn;
    }

    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }

    public Member getAuthor() {
        return this.author;
    }

    public void setAuthor(Member author) {
        this.author = author;
    }

    public String getActionType() {
        return this.actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public Long getHistoryId() {
        return this.historyId;
    }

    public void setHistoryId(Long historyId) {
        this.historyId = historyId;
    }
}
