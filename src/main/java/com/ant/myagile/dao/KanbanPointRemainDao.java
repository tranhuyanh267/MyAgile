package com.ant.myagile.dao;

import java.util.List;

import com.ant.myagile.model.Issue;
import com.ant.myagile.model.KanbanIssue;
import com.ant.myagile.model.KanbanPointRemain;
import com.ant.myagile.model.Status;

public interface KanbanPointRemainDao {
   boolean save(KanbanPointRemain kanbanPointRemain);
   List<KanbanPointRemain> getKanbanPointRemainByKanbanIssue(long idKanbanIssue);
}
