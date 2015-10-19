package com.ant.myagile.service;

import java.util.Date;
import java.util.List;

import com.ant.myagile.model.Attachment;
import com.ant.myagile.model.History;
import com.ant.myagile.model.Issue;
import com.ant.myagile.model.Member;
import com.ant.myagile.model.Project;
import com.ant.myagile.model.Sprint;
import com.ant.myagile.model.Team;
import com.ant.myagile.model.TeamMember;
import com.ant.myagile.model.TeamProject;
import com.ant.myagile.model.UserStory;

public interface HistoryService {
	boolean save(History history);
	boolean update(History history);
	boolean delete(History history);
	
	 /**
	  * Find History by Container (Container is issue or project or team)
	  * @param containerType
	  * 				ISSUE_HISTORY,
	  * 				PROJECT_HISTORY,
	  * 				TEAM_HISTORY
	  * @param containerId issueId or projectId or teamId
	  * @return list of History
	 */
	List<History> findHistoryByContainer(String containerType, long containerId);
	
	 /**
	  * Find History by container (Container is issue or project or team) with number row 
	  * @param containerType
	  * 				ISSUE_HISTORY,
	  * 				PROJECT_HISTORY,
	  * 				TEAM_HISTORY
	  * @param containerId issueId or projectId or teamId
	  * @param numRow number row will find in list (e.g 5 rows or 10 rows)
	  * @return list of History
	 */
	List<History> findHistoryByContainerWithNumberRow(String containerType, long containerId,int numRow);
	
	 /**
	  * Find History by sprint with start row, end row, date start and date end
	  * @param sprintId
	  * @param startRow
	  * @param endRow
	  * @param dateStart
	  * @param dateEnd
	  * @return list of object
	 */
	List<Object> findIssueHistoryBySprintWithStartAndEndRow(Long sprintId, int startRow, int endRow, Date dateStart, Date dateEnd);
	
	/**
	  * Find first date and last date that have History by sprintId
	  * @param sprintId
	  * @return list of date (include first date and end date)
	 */
	List<Date> findDateRangeOfIssueHistoryBySprintId(Long sprintId);
	
	int saveHistoryForIssue(Issue oldIssue, Issue newIssue);
	boolean saveHistoryForNewIssue(Issue issue);
    int saveHistoryForNewProject(Project project);
    int saveHistoryForUpdateProject(Project oldProject, Project newProject);
    int saveHistoryForTeamProject(TeamProject teamProject,String type,String editPage);
    int saveHistoryForNewTeam(Team newTeam);
    int saveHistoryForUpdateTeam(Team oldTeam, Team newTeam);
    int saveHistoryForTeamMember(TeamMember teamMember,String actionType);
    int saveHistoryForChangeRole(TeamMember oldTeamMember,String newRole);
    int saveHistoryForProductBL(UserStory userStory, String actionType, Member author);
    int saveHistoryForSprint(Sprint sprint);
	int saveHistoryForUpdateSprint(Sprint oldSprint, Sprint sprint);
	int saveHistoryForUpdateProductBL(UserStory oldUserStory, UserStory newUserStory);	
	int saveHistoryForAttachment(Attachment attachment, String type);
	int saveHistoryForDeleteAttachment(Attachment attachment, String type);

}
