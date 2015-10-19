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
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@Table(name = "Member")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Member implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	private Long memberId;

	private String firstName;

	private String lastName;

	private String gender;

	private String skype;

	private String mobile;

	private String avatar;

	private String company;

	@Column(nullable = false, unique = true)
	private String username;

	private String lDapUsername;

	private boolean isActive = false;

	private String token;

	private String tokenPass;

	private Date tokenDate = new Date();

	private String password;

	private boolean enabled;

	private boolean accountExpired;

	private boolean accountLocked;

	private boolean passwordExpired;

	@OneToMany(cascade = CascadeType.ALL)
	@JoinColumn(name = "memberId")
	private List<TeamMember> teamMembers;

	@OneToMany(cascade = CascadeType.ALL)
	@JoinColumn(name = "ownerId")
	private List<Project> projects;

	@OneToMany(cascade = CascadeType.ALL)
	@JoinColumn(name = "ownerId")
	private List<Team> teams;

	@OneToMany(cascade = CascadeType.ALL)
	@JoinColumn(name = "memberId")
	private List<MemberEmail> emails;

	public List<TeamMember> getTeamMembers() {
		return this.teamMembers;
	}

	public List<Project> getProjects() {
		return this.projects;
	}

	public Long getMemberId() {
		return memberId;
	}

	public void setMemberId(Long memberId) {
		this.memberId = memberId;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public String getSkype() {
		return skype;
	}

	public void setSkype(String skype) {
		this.skype = skype;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getAvatar() {
		return avatar;
	}

	public void setAvatar(String avatar) {
		this.avatar = avatar;
	}

	public String getCompany() {
		return company;
	}

	public void setCompany(String company) {
		this.company = company;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getlDapUsername() {
		return lDapUsername;
	}

	public void setlDapUsername(String lDapUsername) {
		this.lDapUsername = lDapUsername;
	}

	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getTokenPass() {
		return tokenPass;
	}

	public void setTokenPass(String tokenPass) {
		this.tokenPass = tokenPass;
	}

	public Date getTokenDate() {
		return tokenDate;
	}

	public void setTokenDate(Date tokenDate) {
		this.tokenDate = tokenDate;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public boolean isAccountExpired() {
		return accountExpired;
	}

	public void setAccountExpired(boolean accountExpired) {
		this.accountExpired = accountExpired;
	}

	public boolean isAccountLocked() {
		return accountLocked;
	}

	public void setAccountLocked(boolean accountLocked) {
		this.accountLocked = accountLocked;
	}

	public boolean isPasswordExpired() {
		return passwordExpired;
	}

	public void setPasswordExpired(boolean passwordExpired) {
		this.passwordExpired = passwordExpired;
	}

	public void setProjects(List<Project> projects) {
		this.projects = projects;
	}

	public void setTeamMembers(List<TeamMember> teamMembers) {
		this.teamMembers = teamMembers;
	}

	public List<Team> getTeams() {
		return teams;
	}

	public void setTeams(List<Team> teams) {
		this.teams = teams;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return this.firstName + " " + this.lastName;
	}

	public Member(String username, boolean isActive, String password, boolean enabled) {
		this.username = username;
		this.isActive = isActive;
		this.password = password;
		this.enabled = enabled;
	}

	public Member() {

	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((memberId == null) ? 0 : memberId.hashCode());
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
		Member other = (Member) obj;
		if (memberId == null) {
			if (other.memberId != null) {
				return false;
			}
		} else if (!memberId.equals(other.memberId)) {
			return false;
		}
		return true;
	}

	public List<MemberEmail> getEmails() {
		return emails;
	}

	public void setEmails(List<MemberEmail> emails) {
		this.emails = emails;
	}

}
