package com.ant.myagile.dao;

import java.util.List;

import com.ant.myagile.model.Sprint;
import com.ant.myagile.model.UserStory;

public interface SprintDao {
    boolean save(Sprint sprint);

    boolean update(Sprint sprint);

    boolean delete(Sprint sprint);

    /**
     * @param sprintId
     *            id of sprint
     * @return Sprint object
     */
    Sprint findSprintById(Long sprintId);

    /**
     * Return latest sprint of team
     * 
     * @param teamId
     *            Team to get
     * @return latest sprint
     */
    Sprint findLastSprintByTeamId(Long teamId);

    /**
     * Return list of sprint of teamId and order decrease by sprintId
     * 
     * @param teamId
     *            Team to get
     * @return List of sprint
     */
    List<Sprint> findSprintsByTeamId(Long teamId);

    /**
     * Return list of sprint of teamId and order increase by dateStart
     * 
     * @param teamId
     *            Team to get
     * @return List of sprint
     */
    List<Sprint> findSprintsByTeamIdOrderByAsc(Long teamId);

    /**
     * @param teamId
     *            id of team
     * @return number of sprint of team
     */
    int countSprintsByTeamId(Long teamId);

    /**
	 * List all sprint that are open for team
	 * @param teamId id of team
	 * @return list of sprint
	 */
	List<Sprint> findSprintsAreOpen(Long teamId);
	
	boolean checkUserstoryInSprint(long userstoryId,Sprint sprint);
}
