package com.ant.myagile.service.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ant.myagile.dao.SprintDao;
import com.ant.myagile.model.Sprint;
import com.ant.myagile.model.SprintStatistic;
import com.ant.myagile.model.Team;
import com.ant.myagile.service.HistoryService;
import com.ant.myagile.service.HolidayService;
import com.ant.myagile.service.IssueService;
import com.ant.myagile.service.MeetingVideoService;
import com.ant.myagile.service.RetrospectiveResultService;
import com.ant.myagile.service.SprintService;
import com.ant.myagile.service.SprintStatisticService;
import com.ant.myagile.service.StatusService;
import com.ant.myagile.service.SwimlineService;
import com.ant.myagile.utils.Utils;

@Service
public class SprintServiceImpl implements SprintService {

	private static final Logger logger = Logger.getLogger(SprintServiceImpl.class);
	
	@Autowired
	private SprintDao sprintDao;
	@Autowired
	private HistoryService historyService;
	@Autowired
	private Utils utils;
	@Autowired
	private SprintStatisticService sprintStatisticService;
	@Autowired
	 private RetrospectiveResultService retrospectiveResultService;
	@Autowired
	private HolidayService holidayService;
	@Autowired
	private IssueService issueService;
	@Autowired
	private MeetingVideoService meetingVideoService;
	@Autowired
	private StatusService statusService;
	@Autowired
	private SwimlineService swimlineService;

	@Override
	@Transactional
	public boolean save(Sprint sprint) {
		if (this.sprintDao.save(sprint)) {
			this.historyService.saveHistoryForSprint(sprint);
			return true;
		}
		return false;
	}

	@Override
	@Transactional
	public boolean update(Sprint sprint) {
		this.utils.evict(sprint);
		Sprint oldSprint = this.sprintDao.findSprintById(sprint.getSprintId());
		this.historyService.saveHistoryForUpdateSprint(oldSprint, sprint);
		this.utils.evict(oldSprint);
		return this.sprintDao.update(sprint);
	}
	
	@Override
	@Transactional
	public void updateWithoutHistory(Sprint sprint) {
		this.utils.evict(sprint);
		this.sprintDao.update(sprint);
	}

	@Override
	@Transactional
	public boolean delete(Sprint sprint) {
		return this.sprintDao.delete(sprint);
	}

	@Override
	@Transactional
	public List<Sprint> findSprintsByTeamId(Long teamId) {
		return this.sprintDao.findSprintsByTeamId(teamId);
	}

	@Override
	@Transactional
	public List<Sprint> findSprintsByTeamIdOrderByAsc(Long teamId) {
		return this.sprintDao.findSprintsByTeamIdOrderByAsc(teamId);
	}

	@Override
	@Transactional
	public List<Sprint> findSprintsAreOpen(Long teamId) {
        return this.sprintDao.findSprintsAreOpen(teamId);
	}

	@Override
	@Transactional
	public Map<String, String> validateSprintDate(Long teamId, Sprint sprint) {
		Map<String, String> validString = new HashMap<String, String>();
		List<Sprint> sprintList = new ArrayList<Sprint>();
		if (sprint.getSprintId() != null) {
			sprintList = findSprintsByTeamIdOrderByAsc(teamId);
			if (sprintList.size() > 0) {
				int prevIndex = 0;
				int nextIndex = 0;
				prevIndex = sprintList.indexOf(sprint) - 1;
				nextIndex = sprintList.indexOf(sprint) + 1;
				if (prevIndex >= 0) {
					Sprint sprintPrev = sprintList.get(prevIndex);
					if (sprint.getDateStart().compareTo(sprintPrev.getDateEnd()) < 0) {
						String endDate = Utils.toShortDate(sprintPrev.getDateEnd());
						Object[] params = { endDate };
						validString.put("dateStartEdit", Utils.getMessage("myagile.sprint.edit.StartDateSmall", params));
					}
				}
				if (sprintList.indexOf(sprint) < sprintList.size() - 1) {
					Sprint sprintNext = sprintList.get(nextIndex);
					if (sprint.getDateEnd().compareTo(sprintNext.getDateStart()) >= 0) {
						String startDate = Utils.toShortDate(sprintNext.getDateStart());
						Object[] params = { startDate };
						validString.put("dateEndEdit", Utils.getMessage("myagile.sprint.edit.EndDateLarge", params));
					}
				}
			}
		} else {
			Sprint lastSprint = this.sprintDao.findLastSprintByTeamId(teamId);
			if (lastSprint != null && lastSprint.getDateEnd().compareTo(sprint.getDateStart()) >= 0) {
				String endDate = Utils.toShortDate(lastSprint.getDateEnd());
				Object[] params = { endDate };
				validString.put("dateStart", Utils.getMessage("myagile.sprint.new.StartDateSmall", params));
			}
		}
		if (sprint.getDateStart().compareTo(sprint.getDateEnd()) >= 0) {
			String startDate = Utils.toShortDate(sprint.getDateStart());
			Object[] params = { startDate };
			validString.put("dateEnd", Utils.getMessage("myagile.sprint.newedit.EndDateSmall", params));
		}
		return validString;
	}

	@Override
	@Transactional
	public Sprint findSprintById(Long sprintId) {
		return this.sprintDao.findSprintById(sprintId);
	}

	@Override
	@Transactional
	public Sprint findLastSprintByTeamId(Long teamId) {
		return this.sprintDao.findLastSprintByTeamId(teamId);
	}

	@Override
	public int countSprintsByTeamId(Long teamId) {
		return this.sprintDao.countSprintsByTeamId(teamId);
	}

	@Override
	public void deleteAllSprintInTeam(Team team) {
		
		SprintStatistic sprintStatistic; 
		
		List<Sprint> sprints = sprintDao.findSprintsByTeamId(team.getTeamId());
		if(sprints.size() >  0){
			for (Sprint sprint : sprints) {
				sprintStatistic =  sprintStatisticService.findSprintStatisticBySprint(sprint);
				if(sprintStatistic != null){
				sprintStatisticService.delete(sprintStatistic);
				}
		        holidayService.deleteAllHolidayInSprint(sprint);
		        issueService.deleteAllIssueInSprint(sprint);
		        statusService.deleteAllStatusInSprint(sprint);
		        meetingVideoService.deleteAllMeetingVideosInSprint(sprint);
		        swimlineService.deleteAllSwimlineInSprint(sprint);
		        retrospectiveResultService.deleteAllRetrospectiveResultsInSprint(sprint);
				sprintDao.delete(sprint);
			}
		}
		
	}

	@Override
	public boolean isPastSprint(Sprint sprint) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(sprint.getDateEnd());
		calendar.add(Calendar.DATE, 2);
		Date toDate = Utils.zeroTime(new Date());
		Date endDateOfSprint = Utils.zeroTime(calendar.getTime());
		return endDateOfSprint.compareTo(toDate) < 0;
	}
}
