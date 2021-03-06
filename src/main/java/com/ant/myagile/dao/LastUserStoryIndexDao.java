package com.ant.myagile.dao;

import com.ant.myagile.model.LastUserStoryIndex;
import com.ant.myagile.model.Project;

public interface LastUserStoryIndexDao {

    /**
     * Find index of last user story in user story list
     * 
     * @param projectId
     * @return <strong>Long</strong> the index of last user story
     */
    Long findLastUserStoryIndex(long projectId);

    /**
     * Update index of user story in project
     * 
     * @param project
     * @param sortId
     * @return <strong>true</strong> if update successful</br>
     *         <strong>false</strong> if update fail
     */
    boolean updateIndex(Project project, Long sortId);
    
    LastUserStoryIndex findLastUserStoryIndexByProjectId(long projectId);
    boolean delete(LastUserStoryIndex lastUserStoryIndex);

}
