package com.ant.myagile.dao;

import java.util.List;

import com.ant.myagile.model.Topic;
import com.ant.myagile.model.Wiki;

public interface TopicDao {
	public Topic getTopicByName(String name,long wikiId);
	public Topic getTopicById(long id);
	public int add(Topic topic);
	public Boolean delete(Topic id);
	public int update(Topic topic);
	public List<Topic> getListTopicByWiki(Wiki wiki);
}
