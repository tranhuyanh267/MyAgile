package com.ant.myagile.service.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ant.myagile.dao.WikiDao;
import com.ant.myagile.model.Team;
import com.ant.myagile.model.Topic;
import com.ant.myagile.model.Wiki;
import com.ant.myagile.service.ITopicService;
import com.ant.myagile.service.WikiService;
import com.ant.myagile.utils.Utils;

@Service
@Transactional
public class WikiServiceImpl implements WikiService {

	@Autowired
	WikiDao wikiDao;
	@Autowired
	ITopicService topicService;
	private final static String OPEN_DOUBLE_SQUARE_BRACKET = "[[";
	private final static String CLOSE_DOUBLE_SQUARE_BRACKET = "]]";

	@Override
	public Wiki getByTeamId(long teamId) {
		return this.wikiDao.getByTeamId(teamId);
	}

	@Override
	public int add(Wiki wiki) {
		return this.wikiDao.add(wiki);
	}

	@Override
	public int update(Wiki wiki) {
		return this.wikiDao.update(wiki);
	}

	@Override
	public String convertToHtml(long teamId) {
		Wiki wiki = getByTeamId(teamId);
		if (wiki == null)
			return "";
		
		String content = Utils.createTopicLinkFromContent(wiki.getContent(), wiki.getWikiId(), topicService);
		//String content = wiki.getContent();
//		Map<String, String> topics = new HashMap<String, String>();
//		Pattern pattern = Pattern.compile("\\[\\[(.*?)\\]\\]");
//		Matcher matcher = pattern.matcher(content);
//		while (matcher.find()) {
//			topics.put(matcher.group(), "");
//		}
//
//		for (Entry<String, String> entry : topics.entrySet()) {
//
//			String name = entry.getKey()
//					.replace(this.OPEN_DOUBLE_SQUARE_BRACKET, "")
//					.replace(this.CLOSE_DOUBLE_SQUARE_BRACKET, "").trim();
//			String a = "<a style='color:{color}' href='{link}' data-id-topic ='{topicid}'>{title}</a>";
//			String link = new String("/topic/details");
//			Topic topic = topicService.getTopicByTitle(name, wiki.getWikiId());
//			if (topic == null) {
//				a = a.replace("{color}", "red").replace("{topicid}", "0");
//			} else {
//				a = a.replace("{color}", "blue").replace("{topicid}",
//						String.valueOf(topic.getTopicId()));
//			}
//			a = a.replace("{link}", link).replace("{title}", name);
//			entry.setValue(a);
//
//			content = content.replace(entry.getKey(), entry.getValue());
//		}

		return content;
	}

	@Override
	public boolean checkTeamExistWiki(long teamId) {
		return wikiDao.checkTeamExistWiki(teamId);
	}

	@Override
	public void changeTitle(Wiki wiki, Topic topic, String newTitle) {
		String content = wiki.getContent();
		content = content.replace(
				this.OPEN_DOUBLE_SQUARE_BRACKET + topic.getTitle()
						+ this.CLOSE_DOUBLE_SQUARE_BRACKET,
				this.OPEN_DOUBLE_SQUARE_BRACKET + newTitle
						+ this.CLOSE_DOUBLE_SQUARE_BRACKET);
		wiki.setContent(content);
		this.update(wiki);
	}

	@Override
	public boolean delete(Wiki wiki) {
		topicService.deleteAllTopicInWiki(wiki);
		
		return wikiDao.delete(wiki);
	}

}
