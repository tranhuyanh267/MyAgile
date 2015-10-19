package com.ant.myagile.service;

import java.util.List;

import com.ant.myagile.model.FacebookNews;
import com.ant.myagile.viewmodel.FacebookNewsDTO;

public interface FacebookNewsService {

    public void saveFacebookNewsIntoDB();
    public List<FacebookNews> getFacebookNews();
    
}
