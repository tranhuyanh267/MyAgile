package com.ant.myagile.dao;

import java.util.List;

import com.ant.myagile.model.FacebookNews;


public interface FacebookNewsDao {

    boolean saveFacebookNews(FacebookNews news);

    boolean deleteFacebookNews(int idFacebookNews);

    List<FacebookNews> getTenLatestFacebookNews();

    boolean checkExistFacebookNewsFromPhotoId(String photoId);
    
    FacebookNews getFacebookNewsById(int idFacebookNews);

}
