package com.ant.myagile.dao;

import java.util.List;

import com.ant.myagile.model.HistoryFieldChange;

public interface HistoryFieldChangeDao {
    boolean save(HistoryFieldChange fieldChange);

    List<HistoryFieldChange> findUpdateHistoryOfPointRemainByIssueId(long issueId);

    List<HistoryFieldChange> findUpdateHistoryOfProject(long projectId, String fieldName);

    List<HistoryFieldChange> findUpdateHistoryOfTeam(long teamId, String fieldName);
}
