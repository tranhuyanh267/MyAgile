package com.ant.myagile.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "facebooknews")
public class FacebookNews implements Serializable {

    private static final long serialVersionUID = 1L;
    public static final int STRING_COLUMM_LENGTH = 1023;

    @Id
    @GenericGenerator(name = "increment", strategy = "increment")
   	@GeneratedValue(generator = "increment")
    @Column(name = "id")
    private int id;

    @Column(name = "message", length = STRING_COLUMM_LENGTH)
    private String message;

    @Column(name = "photo_id")
    private String photoId;

    @Column(name = "image_name")
    private String imageName;

    @Column(name = "created_date")
    private Date createDate;

    @Column(name = "title", length = STRING_COLUMM_LENGTH)
    private String title;

    @Column(name = "link")
    private String link;

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPhotoId() {
        return photoId;
    }

    public void setPhotoId(String photoId) {
        this.photoId = photoId;
    }

   

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

	public String getImageName() {
		return imageName;
	}

	public void setImageName(String imageName) {
		this.imageName = imageName;
	}
}
