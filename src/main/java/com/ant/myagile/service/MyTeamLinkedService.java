package com.ant.myagile.service;
import java.util.List;

import com.ant.myagile.model.MyTeamLinked;
public interface MyTeamLinkedService {
	boolean save(MyTeamLinked myteamlinked);

    boolean delete(MyTeamLinked myteamlinked);

    boolean update(MyTeamLinked myteamlinked);

    String findIdByEmail(String email);
    
    List<MyTeamLinked> findAll();
}
