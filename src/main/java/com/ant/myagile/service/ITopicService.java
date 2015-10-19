package com.ant.myagile.service;

import java.util.List;

import com.ant.myagile.model.Topic;
import com.ant.myagile.model.Wiki;

public interface ITopicService {
	public Topic getTopicById(long id);
	public void create(Topic topic);
	public void edit(Topic topic);
	public Boolean delete(Topic topic);
	public Topic getTopicByTitle(String title, long wikiId);
	public boolean isDuplicateTitle(String title, Topic currentTopic);
	List<Topic> getListTopicByWiki(Wiki wiki);
	
	void deleteAllTopicInWiki(Wiki wiki);

}
