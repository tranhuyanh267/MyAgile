package com.ant.myagile.service.impl;

import static ch.lambdaj.Lambda.filter;
import static ch.lambdaj.Lambda.having;
import static ch.lambdaj.Lambda.on;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hibernate.HibernateException;
import org.omnifaces.model.tree.ListTreeModel;
import org.omnifaces.model.tree.TreeModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ant.myagile.dao.IssueDao;
import com.ant.myagile.dao.KanbanPointRemainDao;
import com.ant.myagile.model.Issue;
import com.ant.myagile.model.KanbanPointRemain;
import com.ant.myagile.model.Member;
import com.ant.myagile.model.PointRemain;
import com.ant.myagile.model.Sprint;
import com.ant.myagile.model.Status;
import com.ant.myagile.service.HistoryService;
import com.ant.myagile.service.IssueService;
import com.ant.myagile.service.KanbanPointRemainService;
import com.ant.myagile.service.PointRemainService;
import com.ant.myagile.service.SprintService;
import com.ant.myagile.service.StatusService;
import com.ant.myagile.utils.Utils;

@Service
@Transactional
public class KanbanPointRemainServiceImpl implements KanbanPointRemainService {

	@Autowired
	KanbanPointRemainDao kanbanPointRemainDao;
	@Override
	public boolean save(KanbanPointRemain kanbanPointRemain) {
		return kanbanPointRemainDao.save(kanbanPointRemain);
	}

	@Override
	public List<KanbanPointRemain> getKanbanPointRemainByKanbanIssue(
			long idKanbanIssue) {
		return kanbanPointRemainDao.getKanbanPointRemainByKanbanIssue(idKanbanIssue);
	}
	
	
}
