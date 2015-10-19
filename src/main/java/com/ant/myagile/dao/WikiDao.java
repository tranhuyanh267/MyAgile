package com.ant.myagile.dao;

import com.ant.myagile.model.Wiki;

public interface WikiDao {
	public Wiki getByTeamId(long teamId);
	public int add(Wiki wiki);
	public int update(Wiki wiki);
	public boolean checkTeamExistWiki(Long teamId);
	boolean delete(Wiki wiki);
	
	
}
