package com.ant.myagile.dao;

import java.util.Date;
import java.util.List;

import com.ant.myagile.model.Holiday;
import com.ant.myagile.model.Member;
import com.ant.myagile.model.Sprint;

public interface HolidayDao {
    boolean save(Holiday holiday);

    boolean delete(Holiday holiday);
    
    Holiday findHolidayByDateAndMemberInSprint(Date date,Member member,Sprint sprint);

    List<Holiday> findHolidayBySprint(Sprint sprint);

    List<Holiday> findHolidayByMemberInSprint(Member member,Sprint sprint);
}
