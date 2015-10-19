package com.ant.myagile.dao;

import java.util.Date;
import java.util.List;
import com.ant.myagile.model.PointRemain;
import com.ant.myagile.model.Sprint;

public interface PointRemainDao {
    boolean save(PointRemain pointRemain);

    boolean update(PointRemain pointRemain);

    boolean delete(PointRemain pointRemain);

    List<PointRemain> findPointRemainByIssueId(long issueId);

    List<PointRemain> findPointRemainByIssueIdAndSprint(long issueId, Sprint s);

    PointRemain findPointRemainByIssueIdAndNowDate(long issueId);

    List<PointRemain> findAllIssueByDate(Date d);
}
