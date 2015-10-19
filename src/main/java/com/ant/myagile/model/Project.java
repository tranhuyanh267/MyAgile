package com.ant.myagile.model;

import java.io.Serializable;
import java.util.Comparator;
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
import org.hibernate.annotations.Type;

@Entity
@Table(name = "Project")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Project implements Serializable{

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	private Long projectId;

	@Column(nullable = false)
	private String projectName;

	@Type(type = "text")
	private String description;
	private String imagePath;
	private Boolean isPublic = false;

	@Column(name = "is_archived")
	private Boolean isArchived = false;
	private Date dateCreate = new Date();

	@ManyToOne
	@JoinColumn(name = "ownerId")
	private Member owner;
	
	@Column(name = "product_owner_ids")
	private String productOwnerIds;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "project", targetEntity = TeamProject.class)
	private List<TeamProject> teams;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "project", targetEntity = UserStory.class)
	private List<UserStory> userStories;

	@Column(columnDefinition = "TEXT")
	private String jsonOrder;

	public String getJsonOrder() {
		return jsonOrder;
	}

	public void setJsonOrder(String jsonOrder) {
		this.jsonOrder = jsonOrder;
	}

	public Member getOwner() {
		return this.owner;
	}

	public List<TeamProject> getTeams() {
		return this.teams;
	}

	public Long getProjectId() {
		return this.projectId;
	}

	public void setProjectId(Long projectId) {
		this.projectId = projectId;
	}

	public String getProjectName() {
		return this.projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getImagePath() {
		return this.imagePath;
	}

	public void setImagePath(String imagePath) {
		this.imagePath = imagePath;
	}

	public Boolean getIsPublic() {
		return this.isPublic;
	}

	public void setIsPublic(Boolean isPublic) {
		this.isPublic = isPublic;
	}

	public void setOwner(Member owner) {
		this.owner = owner;
	}

	public void setTeams(List<TeamProject> teams) {
		this.teams = teams;
	}

	public Date getDateCreate() {
		return dateCreate;
	}

	public void setDateCreate(Date dateCreate) {
		this.dateCreate = dateCreate;
	}
	

	public String getProductOwnerIds() {
	    return productOwnerIds;
	}

	public void setProductOwnerIds(String productOwnerIds) {
	    this.productOwnerIds = productOwnerIds;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return this.projectName;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((this.projectId == null) ? 0 : this.projectId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Project other = (Project) obj;
		if (this.projectId == null) {
			if (other.projectId != null) {
				return false;
			}
		} else if (!this.projectId.equals(other.projectId)) {
			return false;
		}
		return true;
	}

	public List<UserStory> getUserStories() {
		return userStories;
	}

	public void setUserStories(List<UserStory> userStories) {
		this.userStories = userStories;
	}

	public boolean getIsArchived() {
		return isArchived;
	}

	public void setIsArchived(boolean isArchived) {
		this.isArchived = isArchived;
	}


	public static Comparator<Project> ProjectNameComparator = new Comparator<Project>() {

		public int compare(Project project1, Project project2) {

			return project1.getProjectName().compareToIgnoreCase(project2.getProjectName());
		}

	};

}
