package com.ant.myagile.service.impl;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ant.myagile.dao.HolidayDao;
import com.ant.myagile.model.Holiday;
import com.ant.myagile.model.Member;
import com.ant.myagile.model.Sprint;
import com.ant.myagile.service.HolidayService;

@Service
@Transactional
public class HolidayServiceImpl implements HolidayService {

	@Autowired
	private HolidayDao holidayDao;

	@Override
	public boolean save(Holiday holiday) {
		return holidayDao.save(holiday);
	}

	@Override
	public boolean delete(Holiday holiday) {
		return holidayDao.delete(holiday);
	}

	@Override
	public Holiday findHolidayByDateAndMemberInSprint(Date date, Member member,
			Sprint sprint) {
		return holidayDao.findHolidayByDateAndMemberInSprint(date, member, sprint);
	}

	@Override
	public List<Holiday> findHolidayBySprint(Sprint sprint) {
		return holidayDao.findHolidayBySprint(sprint);
	}

	@Override
	public List<Holiday> findHolidayByMemberInSprint(Member member,
			Sprint sprint) {
		return holidayDao.findHolidayByMemberInSprint(member, sprint);
	}

	@Override
	public void deleteAllHolidayInSprint(Sprint sprint) {
		List<Holiday> holidays = holidayDao.findHolidayBySprint(sprint);
		if(holidays.size() > 0){
			for (Holiday holiday : holidays) {
				holidayDao.delete(holiday);
			}
		}
		
	}

}