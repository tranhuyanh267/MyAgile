package com.ant.myagile.managedbean;

import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.ant.myagile.model.FacebookNews;
import com.ant.myagile.service.FacebookNewsService;

@Component("aboutBean")
@Scope("session")
public class AboutBean{
	
	@Autowired
	private FacebookNewsService facebookNewsService;
	
	private List<FacebookNews> fbNews;
	
	@PostConstruct
	public void initial(){
		List<FacebookNews> listNews = facebookNewsService.getFacebookNews();
		if(!listNews.isEmpty()){
			this.setFbNews(listNews);
		}
		//facebookNewsService.saveFacebookNewsIntoDB();
	}
	public List<FacebookNews> getFbNews() {
		return fbNews;
	}
	public void setFbNews(List<FacebookNews> fbNews) {
		this.fbNews = fbNews;
	}
}
