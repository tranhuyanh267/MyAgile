package com.ant.myagile.service;

import com.ant.myagile.model.Topic;
import com.ant.myagile.model.Wiki;


public interface WikiService  {
	public Wiki getByTeamId(long teamId);
	public int add(Wiki wiki);
	public int update(Wiki wiki);
	public String convertToHtml(long teamId);
	public boolean checkTeamExistWiki(long teamId);
	public void changeTitle(Wiki wiki, Topic topic, String newTitle);
	public boolean delete(Wiki wiki);
}
