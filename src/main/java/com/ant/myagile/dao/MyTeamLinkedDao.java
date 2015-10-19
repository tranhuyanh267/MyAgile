package com.ant.myagile.dao;


import java.util.List;

import com.ant.myagile.model.MyTeamLinked;

public interface MyTeamLinkedDao {

    boolean save(MyTeamLinked myteamlinked);

    boolean delete(MyTeamLinked myteamlinked);

    boolean update(MyTeamLinked myteamlinked);

    String findIdByEmail(String email);
    
    List<MyTeamLinked> findAll();
}
