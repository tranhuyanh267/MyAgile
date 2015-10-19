package com.ant.myagile.service;

import java.util.List;

import com.ant.myagile.model.KanbanIssue;
import com.ant.myagile.model.KanbanPointRemain;

public interface KanbanPointRemainService {
	 boolean save(KanbanPointRemain kanbanPointRemain);
	 List<KanbanPointRemain> getKanbanPointRemainByKanbanIssue(long idKanbanIssue);
}
