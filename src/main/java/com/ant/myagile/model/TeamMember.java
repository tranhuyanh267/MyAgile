package com.ant.myagile.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;


@Entity
@Table(name = "teamMember", uniqueConstraints = @UniqueConstraint(columnNames = {
        "memberId", "teamId" }))
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class TeamMember implements Serializable {

    private static final long serialVersionUID = 1L;

	@Id
	@Column(name="id")
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long teammemberId;

    public static final String DEVELOPER = "DEVELOPER";
    public static final String SCRUM_MASTER = "SCRUM_MASTER";
    public static final String STAKEHOLDER = "STAKEHOLDER";
    public static final String PRODUCT_OWNER = "PRODUCT_OWNER";

    @ManyToOne
    @JoinColumn(name = "teamId", nullable = false)
    private Team team;

    @ManyToOne
    @JoinColumn(name = "memberId", nullable = false)
    private Member member;

    private String position = TeamMember.DEVELOPER;
    private boolean isAccepted = false;
    private String token = "";
    private Date tokenDate = new Date();

    public Member getMember() {
        return this.member;
    }

	public Team getTeam() {
        return this.team;
    }

    public Long getTeammemberId() {
        return teammemberId;
    }

    public void setTeammemberId(Long teammemberId) {
        this.teammemberId = teammemberId;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public boolean getIsAccepted() {
        return isAccepted;
    }

    public void setIsAccepted(boolean isAccepted) {
        this.isAccepted = isAccepted;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Date getTokenDate() {
        return tokenDate;
    }

    public void setTokenDate(Date tokenDate) {
        this.tokenDate = tokenDate;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    public void setMember(Member member) {
        this.member = member;
    }
    
    public TeamMember() {
	}
    
    public TeamMember(Team team, Member member,String token) {
		this.team = team;
		this.member = member;
		this.token = token;
	}
}
